package tools.important.tankslua

import party.iroiro.luajava.JFunction
import party.iroiro.luajava.Lua
import party.iroiro.luajava.LuaException
import party.iroiro.luajava.value.LuaValue

/*
this function does not nil out the `io` and `os` libraries as it did in previous versions;

this is because it is possible to bypass these restrictions using the `java` library
to (quite painfully) use Java's io and os-related facilities
*/
/**
 * Initialize a Lua state that TanksLua user code will be running on.
 */
fun Lua.initialize(requireRepo: FileRepository? = null) {
    openLibraries()

    run("io.stdout:setvbuf'no'")

    openTanksLib(this)

    val loaders = get("package").get("loaders")
    loaders.clear()
    if (requireRepo != null) {
        setupRequire(loaders, requireRepo)
    }
}

fun openTanksLib(luaState: Lua) {
    luaState.run(TanksLua.extension.getFileText("/lua/tankslib.lua")!!)
}

fun setupRequire(loaders: LuaValue, requireRepo: FileRepository) {
    loaders.set(1, JFunction loader@{ state ->
        if (state.top != 1) {
            state.push("Expected string")
            return@loader 1
        }

        val arg = state.get()
        if (arg.type() != Lua.LuaType.STRING) {
            state.push("Expected string")
            return@loader 1
        }

        val fileToSearch = "$arg.lua"
        val content = requireRepo.readFile(fileToSearch)

        if (content == null) {
            state.push("No $fileToSearch in repository")
            return@loader 1
        }

        try {
            state.load(content)
        } catch (e: LuaException) {
            state.push("$fileToSearch ran into an error loading: ${e.message}")
            return@loader 1
        }
        return@loader 1 // loaded function is on top of the stack
    })
}