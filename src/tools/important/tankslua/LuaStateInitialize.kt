package tools.important.tankslua

import party.iroiro.luajava.Lua

/*
this function does not nil out the `io` and `os` libraries as it did in previous versions;

this is because it is possible to bypass these restrictions using the `java` library
to (quite painfully) use Java's io and os-related facilities
*/
/**
 * Initialize a Lua state that TanksLua user code will be running on.
 */
fun Lua.initialize() {
    openLibraries()

    run("io.stdout:setvbuf'no'")

    openTanksLib(this)
}

fun openTanksLib(luaState: Lua) {
    luaState.run(TanksLua.extension.getFileText("/lua/tankslib.lua")!!)
}