package tools.important.tankslua

import party.iroiro.luajava.Lua
import party.iroiro.luajava.LuaException
import party.iroiro.luajava.luajit.LuaJit
import party.iroiro.luajava.value.LuaValue

var currentLevelScript: LevelScript? = null
    private set

fun clearCurrentLevelScript() {
    currentLevelScript?.luaState?.close()
    currentLevelScript = null
}

/**
 * It is rather important to note that the name of this class is misleading.
 * A 'level script' can actually store multiple scripts, or files in general, including directories.
 *
 * It may be changed in future versions, if I can think of anything better.
 */
class LevelScript(
    val repo: FileRepository,
    val luaState: Lua,
    var loadedFunction: LuaValue?,
    var updateFunction: LuaValue?,
    var drawFunction: LuaValue?
)

class LevelScriptLoadException(message: String) : Exception(message)
private fun loadFail(message: String, luaState: Lua? = null): Nothing {
    luaState?.close()
    throw LevelScriptLoadException(message)
}

fun LuaValue.getOptionalOfType(key: String, type: Lua.LuaType): LuaValue? {
    val value = get(key)

    if (value.type() == Lua.LuaType.NIL) return null
    if (value.type() != type) return null

    return value
}

private const val LEVELSCRIPT_MAIN_SCRIPT_NAME = "level.lua"

fun tryLoadingLevelScript(name: String) {
    verifyDirectoryStructure()

    // note: the nameWithoutExtension won't work for some formats like .tar.gz,
    // might need to replace if the need introduces itself
    val file = levelDir.listFiles()!!.find { it.nameWithoutExtension == name } ?: return

    val repo = try { openAsRepository(file) } catch (_: IllegalArgumentException) {
        loadFail("Unable to open level script file as a repository!")
    }

    val mainScript = repo.readFile(LEVELSCRIPT_MAIN_SCRIPT_NAME) ?: loadFail("Level script does not have $LEVELSCRIPT_MAIN_SCRIPT_NAME file!")

    val luaState = LuaJit().apply { initialize(repo) }

    try {
        luaState.load(mainScript)
    } catch (e: LuaException) {
        loadFail("level.lua failed to load: ${e.message}", luaState)
    }

    try {
        val results = luaState.get().call()
        if (results.size != 1) {
            loadFail("$LEVELSCRIPT_MAIN_SCRIPT_NAME failed to return exactly one value", luaState)
        }
        val result = results[0]
        if (result.type() != Lua.LuaType.TABLE) {
            loadFail("$LEVELSCRIPT_MAIN_SCRIPT_NAME failed to return a table", luaState)
        }

        val loadedFunction = result.getOptionalOfType("loaded", Lua.LuaType.FUNCTION)
        val updateFunction = result.getOptionalOfType("update", Lua.LuaType.FUNCTION)
        val drawFunction = result.getOptionalOfType("draw", Lua.LuaType.FUNCTION)

        try {
            loadedFunction?.call()
        } catch (e: LuaException) {
            loadFail("$LEVELSCRIPT_MAIN_SCRIPT_NAME's load function ran into an issue: ${e.message}", luaState)
        }

        currentLevelScript = LevelScript(
            repo,
            luaState,
            loadedFunction,
            updateFunction,
            drawFunction
        )
    } catch (e: LuaException) {
        loadFail("$LEVELSCRIPT_MAIN_SCRIPT_NAME failed to run: ${e.message}", luaState)
    }
}