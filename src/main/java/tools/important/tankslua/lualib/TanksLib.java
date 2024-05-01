package tools.important.tankslua.lualib;

import party.iroiro.luajava.Lua;
import party.iroiro.luajava.LuaException;
import party.iroiro.luajava.value.LuaValue;
import tanks.Drawing;
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

        luaState.push("playSound");
        luaState.push((ignored) -> {
            int argnum = luaState.getTop();
            if (argnum == 0 || argnum > 3) {
                throw new LuaException("incorrect number of arguments for playSound");
            }
            float volume = 1f;
            if (argnum == 3) {
                LuaValue volumeLuaVal = luaState.get();
                if (volumeLuaVal.type() != Lua.LuaType.NUMBER) {
                    throw new LuaException("incorrect type for volume supplied to playSound");
                }
                //noinspection DataFlowIssue
                volume = ((Double) volumeLuaVal.toJavaObject()).floatValue();
            }

            float pitch = 1f;
            if (argnum >= 2) {
                LuaValue pitchLuaVal = luaState.get();
                if (pitchLuaVal.type() != Lua.LuaType.NUMBER) {
                    throw new LuaException("incorrect type for pitch supplied to playSound");
                }
                //noinspection DataFlowIssue
                pitch = ((Double)pitchLuaVal.toJavaObject()).floatValue();
            }

            LuaValue soundNameLuaVal = luaState.get();
            if (soundNameLuaVal.type() != Lua.LuaType.STRING) {
                throw new LuaException("incorrect type for sound name supplied to playSound");
            }
            String soundName = (String) soundNameLuaVal.toJavaObject();

            Drawing.drawing.playSound(soundName, pitch, volume);

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

        // tanks library is finished, now it's time for java library enhancements
    }
}
