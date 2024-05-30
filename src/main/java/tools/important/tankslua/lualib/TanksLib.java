package tools.important.tankslua.lualib;

import party.iroiro.luajava.Lua;
import party.iroiro.luajava.LuaException;
import party.iroiro.luajava.value.LuaValue;
import tanks.Game;
import tanks.ModAPI;
import tools.important.tankslua.Notification;

public final class TanksLib implements LuaLib {
    /**
     * Load the tanks library to the given lua state
     * @param luaState The lua state to load the tanks library onto
     */
    public void open(Lua luaState) {
        luaState.createTable(0,0);
        int tanksLibStackIndex = luaState.getTop();

        luaState.push("version");
        luaState.push(Game.version);
        luaState.setTable(tanksLibStackIndex);

        luaState.push("sendNotification");
        luaState.push(state -> {
            int args = state.getTop();
            if (args != 1) {
                throw new LuaException("incorrect number of arguments for sendNotification");
            }
            LuaValue notifT = state.get();

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

            new Notification(Notification.NotificationType.INFO, seconds, text);

            return 0;
        });
        luaState.setTable(tanksLibStackIndex);

        boolean isModApi;
        try {
            //noinspection JavaReflectionMemberAccess
            ModAPI.class.getDeclaredField("version"); // this field exists in the newer versions of modapi, but in the crusty dusty ancient version vanilla tanks uses, it doesn't
            isModApi = true;
        } catch (NoSuchFieldException ignored) {
            isModApi = false;
        }

        luaState.push("isModApi");
        luaState.push(isModApi);
        luaState.setTable(tanksLibStackIndex);

        luaState.setGlobal("tanks");
    }
}
