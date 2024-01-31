package tools.important.tankslua;

import party.iroiro.luajava.Lua;
import party.iroiro.luajava.value.LuaValue;
import tanks.Game;
import tanks.gui.screen.Screen;
import tanks.gui.screen.ScreenGame;

import java.io.File;
import java.util.HashMap;

public class TanksEventListener {
    private Screen lastScreen;

    public TanksEventListener() {
        LEVEL_TABLE_TYPES.put("onLoad", new SafeLuaRunner.TableType(Lua.LuaType.FUNCTION, false));
    }
    public void onUpdate() {
        if (lastScreen != Game.screen) {
            onScreenChanged(lastScreen, Game.screen);
        }

        lastScreen = Game.screen;

        for (LuaExtension luaext: TanksLua.tanksLua.loadedLuaExtensions) {
            LuaValue fOnUpdate = luaext.fOnUpdate;
            if (fOnUpdate.type() == Lua.LuaType.NIL) return;
            SafeLuaRunner.safeCall(fOnUpdate);
        }
    }

    public void onDraw() {
        for (LuaExtension luaext: TanksLua.tanksLua.loadedLuaExtensions) {
            LuaValue fOnDraw = luaext.fOnDraw;
            if (fOnDraw.type() == Lua.LuaType.NIL) return;
            SafeLuaRunner.safeCall(fOnDraw);
        }
    }

    private final HashMap<String, SafeLuaRunner.TableType> LEVEL_TABLE_TYPES = new HashMap<>();
    private void onScreenChanged(Screen oldScreen, Screen newScreen) {
        if (!(newScreen instanceof ScreenGame sg)) return;
        String rawName = sg.name;
        if (rawName == null) return;

        String nameNoExt = rawName.substring(0, rawName.length()-6);

        String levelLuaFileName = nameNoExt+".lua";
        String levelLuaFilePath = TanksLua.fullScriptPath+"/level/"+levelLuaFileName;
        File levelLuaFile = new File(levelLuaFilePath);
        if (!levelLuaFile.exists()) return;
        final int errNotifTime = 5;
        LuaValue loadedFile = SafeLuaRunner.safeLoadFile(levelLuaFilePath);
        if (loadedFile == null) {
            new Notification(Notification.NotificationType.WARN, errNotifTime, levelLuaFileName+" failed to load");
            return;
        }

        SafeLuaRunner.UserCallResult result = SafeLuaRunner.safeCall(loadedFile);

        if (result.status() != Lua.LuaError.OK) {
            new Notification(Notification.NotificationType.WARN, errNotifTime, levelLuaFileName+" ran into an error when running");
            return;
        }

        LuaValue[] returns = result.returns();

        if (returns.length != 1) {
            new Notification(Notification.NotificationType.WARN, errNotifTime, levelLuaFileName+" did not return exactly one value!");
            return;
        }

        LuaValue tLevel = returns[0];

        if (tLevel.type() != Lua.LuaType.TABLE) {
            new Notification(Notification.NotificationType.WARN, errNotifTime, levelLuaFileName+" did not return a table!");
            return;
        }

        SafeLuaRunner.VerificationResult verificationResult = SafeLuaRunner.verifyTable(tLevel, LEVEL_TABLE_TYPES);

        if (!verificationResult.verified()) {
            new Notification(Notification.NotificationType.WARN, errNotifTime, levelLuaFileName+"'s table failed to verify: "+verificationResult.message());
            return;
        }

        SafeLuaRunner.safeCall(tLevel.get("onLoad"));
    }
}
