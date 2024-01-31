package tools.important.tankslua;

import party.iroiro.luajava.Lua;
import party.iroiro.luajava.LuaException;
import party.iroiro.luajava.value.LuaValue;
import tanks.Game;

public final class TanksLib {
    private TanksLib(){}

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

        luaState.push("sendNotification");
        luaState.push((ignored) -> {
            int args = luaState.getTop();
            if (args != 1) {
                throw new LuaException("incorrect number of arguments for sendNotification");
            }
            LuaValue notifT = luaState.get();

            if (notifT.type() != Lua.LuaType.TABLE) {
                throw new LuaException("non-table value supplied to sendNotification");
            }

            String text;
            LuaValue textval = notifT.get("text");
            if (textval.type() != Lua.LuaType.STRING) {
                throw new LuaException("wrong type for key 'text' in table supplied to sendNotification");
            }
            text = (String) textval.toJavaObject();

            LuaValue secondsval = notifT.get("seconds");
            double seconds;
            if (secondsval.type() == Lua.LuaType.NUMBER) {
                //noinspection DataFlowIssue
                seconds = (double)secondsval.toJavaObject();
            } else {
                seconds = 5;
            }

            if (seconds > 20) {
                throw new LuaException("amount of seconds in notification cannot exceed 20");
            }

            new Notification(Notification.NotificationType.INFO, seconds, text);

            return 0;
        });
        luaState.setTable(tanksLibStackIndex);

        luaState.setGlobal("tanks");
    }
}
