package tools.important.tankslua

import party.iroiro.luajava.Lua

fun openTanksLib(luaState: Lua) {
    luaState.run(TanksLua.extension.getFileText("/lua/tankslib.lua"))
}