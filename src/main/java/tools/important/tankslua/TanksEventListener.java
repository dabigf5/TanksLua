package tools.important.tankslua;

import org.apache.commons.io.FilenameUtils;
import party.iroiro.luajava.Lua;
import party.iroiro.luajava.LuaException;
import party.iroiro.luajava.value.LuaValue;
import tanks.Game;
import tanks.gui.screen.Screen;
import tanks.gui.screen.ScreenGame;
import tools.important.tankslua.gui.screen.ScreenOptionsLuaInspectExtension;
import tools.important.tankslua.luapackage.LevelPack;
import tools.important.tankslua.luapackage.LuaExtension;

public class TanksEventListener {
    private Screen lastScreen;
    public TanksEventListener() {}
    public void onUpdate() {
        if (lastScreen != Game.screen) {
            onScreenChanged(lastScreen, Game.screen);
        }
        LevelPack currentLevelPack = TanksLua.tanksLua.currentLevelPack;
        if (currentLevelPack != null) {
            LuaValue fOnUpdate = currentLevelPack.callbacks.get("onUpdate");
            if (fOnUpdate == null || fOnUpdate.type() != Lua.LuaType.NIL) TanksLua.tanksLua.runner.safeCall(fOnUpdate);
        }

        lastScreen = Game.screen;

        for (LuaExtension luaext: TanksLua.tanksLua.loadedLuaExtensions) {
            if (!luaext.enabled) continue;
            LuaValue fOnUpdate = luaext.callbacks.get("onUpdate");
            if (fOnUpdate == null || fOnUpdate.type() == Lua.LuaType.NIL) continue;
            TanksLua.tanksLua.runner.safeCall(fOnUpdate);
        }
    }

    public void onDraw() {
        LevelPack currentLevelScript = TanksLua.tanksLua.currentLevelPack;
        if (currentLevelScript != null) {
            LuaValue fOnDraw = currentLevelScript.callbacks.get("onDraw");
            if (fOnDraw == null || fOnDraw.type() != Lua.LuaType.NIL) TanksLua.tanksLua.runner.safeCall(fOnDraw);
        }

        for (LuaExtension luaext: TanksLua.tanksLua.loadedLuaExtensions) {
            if (!luaext.enabled) continue;
            LuaValue fOnDraw = luaext.callbacks.get("onDraw");
            if (fOnDraw == null || fOnDraw.type() == Lua.LuaType.NIL) continue;
            TanksLua.tanksLua.runner.safeCall(fOnDraw);
        }
    }

    private void onScreenChanged(Screen oldScreen, Screen newScreen) {
        if (oldScreen instanceof ScreenOptionsLuaInspectExtension) {
            ScreenOptionsLuaInspectExtension inspectionScreen = (ScreenOptionsLuaInspectExtension) oldScreen;
            LuaExtension inspectedExtension = inspectionScreen.extension;
            inspectedExtension.saveOptions();

            if (inspectedExtension.enabled) {
                inspectedExtension.loadCallbacksIfNone();
                inspectedExtension.onNewOptions();
            }
        }



        if (newScreen instanceof ScreenGame) {
            onLevelLoaded((ScreenGame) newScreen);
            return;
        }

        if (oldScreen instanceof ScreenGame) TanksLua.tanksLua.currentLevelPack = null;
    }

    private void onLevelLoaded(ScreenGame sg) {
        String levelName = sg.name;

        if (levelName != null) levelName = FilenameUtils.removeExtension(levelName);
        boolean levelScriptsEnabled = (boolean) TanksLua.tanksLua.getOptionValue("enableLevelScripts");

        if (levelScriptsEnabled) {
            try {
                TanksLua.tanksLua.currentLevelPack = LevelPack.fromLevelName(levelName);
            } catch (LuaException luaException) {
                new Notification(Notification.NotificationType.WARN, 5, luaException.getMessage());
            }
        }

        for (LuaExtension luaext: TanksLua.tanksLua.loadedLuaExtensions) {
            if (!luaext.enabled) continue;
            LuaValue fOnLevelLoad = luaext.callbacks.getOrDefault("onLevelLoad", TanksLua.tanksLua.internalLuaState.fromNull());
            if (fOnLevelLoad == null || fOnLevelLoad.type() == Lua.LuaType.NIL) continue;

            TanksLua.tanksLua.runner.safeCall(fOnLevelLoad, levelName);
        }
    }
}
