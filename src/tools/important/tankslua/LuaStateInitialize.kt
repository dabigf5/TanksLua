package tools.important.tankslua

import party.iroiro.luajava.JFunction
import party.iroiro.luajava.Lua

/*
this function does not nil out the `io` and `os` libraries as it did in previous versions;

this is because it is possible to bypass these restrictions using the `java` library
to (quite painfully) use Java's io and os-related facilities
*/
/**
 * Initialize a Lua state that TanksLua user code will be running on.
 */
fun Lua.initialize(repo: FileRepository? = null) {
    openLibraries()

    run("io.stdout:setvbuf'no'")

    runLuaFileInJar("/lua/ensureType.lua")

    if (repo != null) {
        setupReadFileFunction(this, repo)
    }
    runLuaFileInJar("/lua/initializeLoaders.lua")

    runLuaFileInJar("/lua/tankslib.lua")
}

fun Lua.runLuaFileInJar(path: String) {
    run(TanksLua.extension.getFileText(path)!!)
}

fun setupReadFileFunction(luaState: Lua, repo: FileRepository) {
    luaState.set("readFile", JFunction reader@{ state ->
        if (state.top != 1) {
            luaState.error("Expected string")
            return@reader 0
        }

        val arg = state.get()
        if (arg.type() != Lua.LuaType.STRING) {
            luaState.error("Expected string")
            return@reader 0
        }

        val filepath = arg.toString()

        val content = repo.readFile(filepath)
        if (content == null) {
            luaState.pushNil()
            return@reader 1
        }

        luaState.push(content)
        return@reader 1
    })
}