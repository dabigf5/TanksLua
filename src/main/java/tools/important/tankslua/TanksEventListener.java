package tools.important.tankslua;

import party.iroiro.luajava.Lua;
import party.iroiro.luajava.value.LuaValue;
import tanks.Game;
import tanks.gui.screen.Screen;

public class TanksEventListener {
    private Screen lastScreen;

    public TanksEventListener() {

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

    private void onScreenChanged(Screen oldScreen, Screen newScreen) {
    }
}
