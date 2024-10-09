package tools.important.tankslua

import party.iroiro.luajava.Lua
import party.iroiro.luajava.LuaException
import party.iroiro.luajava.luajit.LuaJit
import party.iroiro.luajava.value.LuaValue
import tools.important.tankslua.gui.Notification
import tools.important.tankslua.gui.NotificationType
import tools.important.tankslua.tkv.*
import java.io.File
import java.io.IOException

private const val USE_DECOYS = false
val loadedExtensions: MutableList<LuaExtension> = mutableListOf()

class LuaExtensionLoadException(message: String) : Exception(message)
private fun loadFail(message: String, luaState: Lua? = null): Nothing {
    luaState?.close()
    throw LuaExtensionLoadException(message)
}

fun initializeLuaState(extension: RealLuaExtension) {
    val luaState = LuaJit().apply { initialize() }

    val source = extension.repo.readFile("extension.lua") ?: loadFail("Extension repository is missing an extension.lua!")

    try {
        luaState.load(source)
    } catch (e: LuaException) {
        loadFail("extension.lua failed to load: ${e.message}", luaState)
    }

    val result = try {
        val results = luaState.get().call()
        if (results.size != 1) loadFail("extension.lua failed to return exactly one value", luaState)
        results[0]
    } catch (e: LuaException) {
        loadFail("extension.lua failed to run: ${e.message}", luaState)
    } .also {
        if (it.type() != Lua.LuaType.TABLE) loadFail("extension.lua failed to return a table")
    }

    val loaded = result.getOptionalOfType("loaded", Lua.LuaType.FUNCTION)
    val update = result.getOptionalOfType("update", Lua.LuaType.FUNCTION)
    val draw = result.getOptionalOfType("draw", Lua.LuaType.FUNCTION)

    try {
        loaded?.call()
    } catch (e: LuaException) {
        loadFail("extension.lua's loaded ran into an error: ${e.message}", luaState)
    }

    extension.luaState = luaState
    extension.loadedFunction = loaded
    extension.updateFunction = update
    extension.drawFunction = draw
}

private fun tryLoadExtension(file: File) {
    // note: suffers from the same potential tar.gz problem as LevelScripts.kt
    val name = file.nameWithoutExtension

    val options = File(extensionOptionsDir.path + "/" + name).let { optionsFile ->
        if (optionsFile.exists()) return@let optionsFile.bufferedReader().use {
            it.readText()
        }
        null
    } .let { encoded ->
        if (encoded == null) return@let mapOf("enabled" to TKVValue(TKVType.BOOLEAN, false))

        try {
            decodeTKV(encoded)
        } catch (e: TKVDecodeException) {
            loadFail("Extension's metadata failed to parse: ${e.message}")
        }
    }

    val enabled = options["enabled"].let {
        if (it == null || it.type != TKVType.BOOLEAN)
            loadFail("Extension's options exist, but do not contain a valid 'enabled' key! This is probably the fault of TanksLua.")
        it.value as Boolean
    }

    val repo = try { openAsRepository(file) } catch (_: IllegalArgumentException) {
        loadFail("Unable to open extension file as a repository!")
    }

    val meta = try { decodeTKV(
        repo.readFile("meta.tkv") ?: loadFail("Extension repository is missing a meta.tkv!")
    ) } catch (e: TKVDecodeException) {
        loadFail("Extension's metadata failed to decode: ${e.message}")
    }

    val displayName = meta["displayName"].let {
        if (it == null || it.type != TKVType.STRING) loadFail("Extension's metadata is missing a valid displayname!")
        it.value as String
    }

    val description = meta["description"].let {
        if (it != null && it.type == TKVType.STRING) return@let it.value as String
        null
    }

    val version = meta["version"].let {
        if (it == null || it.type != TKVType.VERSION) loadFail("Extension's metadata is missing a valid version!")
        it.value as SemanticVersion
    }

    val extension = RealLuaExtension(
        name = name,

        displayName = displayName,
        description = description,
        version = version,

        enabled = enabled,

        repo = repo,
    )

    if (enabled) {
        initializeLuaState(extension)
    }

    loadedExtensions.add(extension)
}

fun loadLuaExtensions() {
    if (USE_DECOYS) {
        repeat(100) {
            loadedExtensions.add(DecoyLuaExtension())
        }

        return
    }

    verifyDirectoryStructure()

    for (file in extensionsDir.listFiles()!!) {
        try {
            tryLoadExtension(file)
        } catch (e: LuaExtensionLoadException) {
            Notification("Extension ${file.name} failed to load: ${e.message!!}", NotificationType.ERR, 500.0)
        }
    }
}

fun clearLoadedExtensions() {
    loadedExtensions.forEach { it.whenCleared() }
    loadedExtensions.clear()
}

interface LuaExtension {
    val name: String
    val displayName: String
    val description: String?
    val version: SemanticVersion
    fun whenCleared() {}
    fun whenLoaded() {}
}

class DecoyLuaExtension : LuaExtension {
    val index: Int
        get() = loadedExtensions.indexOf(this)

    val decoyNumber: Int
        get() = index+1

    override val name: String
        get() = "decoy/$decoyNumber"

    override val displayName: String
        get() = "Decoy #$decoyNumber"

    override fun whenCleared() {}
    override fun whenLoaded() {}

    override val description: String
        get() = "This extension is a fake decoy extension,---" +
                "and you can tell because its name is $name.---" +
                "The / in the name is only available to decoys---" +
                "due to the way filesystems work."

    override val version: SemanticVersion = SemanticVersion(1, 0, 0)
}

class RealLuaExtension(
    /**
     * Should be the filename of whatever directory or file the extension was loaded from, minus the extension.
     *
     * This means cross-platform, no `/`, and additionally on Windows, none of `<>:\|?*`.
     *
     * Lua Extension developers running linux or mac are discouraged from naming their extensions
     * with any of the forbidden characters on Windows, as it makes distribution more difficult.
     */
    override val name: String,
    override val displayName: String,
    override val description: String?,
    override val version: SemanticVersion,

    var enabled: Boolean = false,

    val repo: FileRepository,
    var luaState: Lua? = null,
    var loadedFunction: LuaValue? = null,
    var updateFunction: LuaValue? = null,
    var drawFunction: LuaValue? = null
) : LuaExtension {
    // todo: options besides enabled

    private val optionsFile = File(extensionOptionsDir.path + "/" + name)

    /**
     * The caller of this method is expected to handle any [IOException]s that it may throw.
     */
    fun saveOptions() {
        verifyDirectoryStructure()

        optionsFile.bufferedWriter().use {
            it.write(encodeTKV(mapOf("enabled" to TKVValue(TKVType.BOOLEAN, enabled))))
        }
    }

    override fun whenCleared() {
        saveOptions()
    }
}