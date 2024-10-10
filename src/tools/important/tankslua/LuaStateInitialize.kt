package tools.important.tankslua

import party.iroiro.luajava.JFunction
import party.iroiro.luajava.Lua
import party.iroiro.luajava.LuaException
import tools.important.tankslua.gui.Notification
import tools.important.tankslua.gui.NotificationType

fun luaError(message: String): Nothing = throw LuaException(LuaException.LuaError.RUNTIME, message)


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

    loadTanksLib(this)
}

fun loadTanksLib(luaState: Lua) {
    luaState.createTable(0, 0)
    luaState.get().let { tanksLib ->
        tanksLib.set("notify", JFunction(fun(state): Int {
            val argCount = state.top

            val message: String = if (argCount >= 1) {
                state.toString(1) ?: luaError("Message is wrong type!")
            } else luaError("Not enough arguments!")

            val duration: Double? = if (argCount >= 2) {
                if (!state.isNumber(2)) luaError("Duration is wrong type!")
                state.toNumber(2)
            } else null

            Notification(message, NotificationType.INFO, duration ?: 200.0)
            return 0
        }))

        luaState.set("tanks", tanksLib)
    }
}