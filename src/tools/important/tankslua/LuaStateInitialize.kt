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
fun Lua.initialize(repo: FileRepository? = null) {
    openLibraries()

    run("io.stdout:setvbuf'no'")

    openTanksLib(this)

    val loaders = get("package").get("loaders")
    loaders.clear()
    if (repo != null) {
        setupRequire(loaders, repo)
        setupReadFileFunction(this, repo)
    }
}

fun openTanksLib(luaState: Lua) {
    luaState.run(TanksLua.extension.getFileText("/lua/tankslib.lua")!!)
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

        val searchPath = "${arg.toString().replace('.','/')}.lua"
        val content = requireRepo.readFile(searchPath)

        if (content == null) {
            state.push("No $searchPath in repository")
            return@loader 1
        }

        try {
            state.load(content)
        } catch (e: LuaException) {
            state.push("$searchPath ran into an error loading: ${e.message}")
            return@loader 1
        }
        return@loader 1 // loaded function is on top of the stack
    })
}