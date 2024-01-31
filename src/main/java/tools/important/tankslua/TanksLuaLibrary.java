package tools.important.tankslua;

import party.iroiro.luajava.Lua;
import party.iroiro.luajava.LuaException;
import party.iroiro.luajava.value.LuaValue;
import tanks.Game;
import tanks.tank.TankAIControlled;

public class TanksLuaLibrary {
    private TanksLuaLibrary(){}

    /**
     * Load the tanks library to the given lua state
     * @param luaState The lua state to load the tanks library onto
     */
    public static void loadTanksLib(Lua luaState) {
        luaState.createTable(0,0);
        int tanksLibStackIndex = luaState.getTop();

        luaState.push("version");
        luaState.push(Game.version);
        luaState.setTable(tanksLibStackIndex);

        luaState.push("newAITank");
        luaState.push((l) -> {
            int args = luaState.getTop();
            if (args != 1) {
                throw new LuaException("Incorrect number of arguments for tanks.newAITank!");
            }

            LuaValue tTankInfo = luaState.get();

            if (tTankInfo.type() != Lua.LuaType.TABLE) {
                throw new LuaException("Incorrect argument type for tanks.newAITank!");
            }

            TankAIControlled newTankClass = new TankAIControlled(
                    (String) tTankInfo.get("name").toJavaObject(), 0, 0, 0, 0, 0, 0, 0, TankAIControlled.ShootAI.none
            ){};

            luaState.pushJavaClass(newTankClass.getClass());
            return 1;
        });
        luaState.setTable(tanksLibStackIndex);



        luaState.push("lua");
        luaState.createTable(0, 0);
        int luaTableStackIndex = luaState.getTop();

        luaState.push("version");
        luaState.push(TanksLua.version);
        luaState.setTable(luaTableStackIndex);

        luaState.push("fullScriptPath");
        luaState.push(TanksLua.fullScriptPath);
        luaState.setTable(luaTableStackIndex);

        luaState.push("getOptions");
        luaState.push(L -> {
            TanksLua.tanksLua.options.toLuaTable(luaState);
            return 1;
        });
        luaState.setTable(luaTableStackIndex);

        luaState.push("registerExtension");
        luaState.push((ignored) -> {
            int args = luaState.getTop();
            if (args != 1) {
                throw new LuaException("Incorrect number of arguments for tanks.lua.registerExtension");
            }

            LuaValue tExtension = luaState.get();

            if (tExtension.type() != Lua.LuaType.TABLE) {
                throw new LuaException("Incorrect argument type for tanks.lua.registerExtension!");
            }
            // now that we know it's a table, we're going to assume the rest of it is valid

            TanksLua.tanksLua.loadedLuaExtensions.add(new LuaExtension(
                    (String) tExtension.get("filename").toJavaObject(),
                    (String) tExtension.get("name").toJavaObject(),
                    (String) tExtension.get("author").toJavaObject(),
                    tExtension
                )
            );

            return 0;
        });
        luaState.setTable(luaTableStackIndex);

        luaState.setTable(tanksLibStackIndex);
        luaState.setGlobal("tanks");
    }
}
