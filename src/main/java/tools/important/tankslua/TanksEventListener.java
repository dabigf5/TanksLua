package tools.important.tankslua;

import party.iroiro.luajava.Lua;
import party.iroiro.luajava.value.LuaValue;
import tanks.Game;
import tanks.gui.screen.Screen;
import tanks.gui.screen.ScreenGame;

public class TanksEventListener {
    private Screen lastScreen;
    public TanksEventListener() {}
    public void onUpdate() {
        if (lastScreen != Game.screen) {
            onScreenChanged(lastScreen, Game.screen);
        }
        LevelScript currentLevelScript = LevelScript.currentLevelScript;
        if (currentLevelScript != null) {
            LuaValue fOnUpdate = currentLevelScript.fOnUpdate;
            if (fOnUpdate.type() != Lua.LuaType.NIL) SafeLuaRunner.safeCall(fOnUpdate);
        }

        lastScreen = Game.screen;

        for (LuaExtension luaext: TanksLua.tanksLua.loadedLuaExtensions) {
            LuaValue fOnUpdate = luaext.fOnUpdate;
            if (fOnUpdate.type() == Lua.LuaType.NIL) return;
            SafeLuaRunner.safeCall(fOnUpdate);
        }
    }

    public void onDraw() {
        LevelScript currentLevelScript = LevelScript.currentLevelScript;
        if (currentLevelScript != null) {
            LuaValue fOnDraw = currentLevelScript.fOnDraw;
            if (fOnDraw.type() != Lua.LuaType.NIL) SafeLuaRunner.safeCall(fOnDraw);
        }

        for (LuaExtension luaext: TanksLua.tanksLua.loadedLuaExtensions) {
            LuaValue fOnDraw = luaext.fOnDraw;
            if (fOnDraw.type() == Lua.LuaType.NIL) return;
            SafeLuaRunner.safeCall(fOnDraw);
        }
    }

    private void onScreenChanged(Screen oldScreen, Screen newScreen) {
        if ((boolean) TanksLua.tanksLua.options.get("enableLevelScripts") && newScreen instanceof ScreenGame) {
            ScreenGame sg = ((ScreenGame) newScreen);
            LevelScript.tryLoadingLevelScript(sg.name);
        }

        if (oldScreen instanceof ScreenGame && (!(newScreen instanceof ScreenGame))) LevelScript.currentLevelScript = null;
    }
}
