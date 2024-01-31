package tools.important.tankslua;

import party.iroiro.luajava.Lua;
import tanks.Game;

public final class TanksLuaLibrary {
    private TanksLuaLibrary(){}

    /**
     * Load the tanks library to the given lua state
     * @param luaState The lua state to load the tanks library onto
     */
    public static void loadTanksLibrary(Lua luaState) {
        luaState.createTable(0,0);
        int tanksLibStackIndex = luaState.getTop();

        luaState.push("version");
        luaState.push(Game.version);
        luaState.setTable(tanksLibStackIndex);

        luaState.setGlobal("tanks");
    }
}
