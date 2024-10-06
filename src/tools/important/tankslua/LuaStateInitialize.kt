package tools.important.tankslua

import party.iroiro.luajava.Lua

fun Lua.initialize() {
    openLibraries()
    load("io.stdout:setvbuf'no'")
    get().call()
}