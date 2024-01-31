package tools.important.tankslua;

import party.iroiro.luajava.Lua;
import party.iroiro.luajava.value.LuaValue;
import tanks.Game;
import tanks.gui.screen.Screen;
import tanks.gui.screen.ScreenGame;

public class TanksEventListener {
    private Screen lastScreen;

    public TanksEventListener() {

    }
    public void onUpdate() {
        if (lastScreen != Game.screen) {
            onScreenChanged(lastScreen, Game.screen);
        }

        lastScreen = Game.screen;
        Lua defaultState = SafeLuaRunner.defaultState;

        LuaValue tTanksLibrary = defaultState.get("tanks");
        LuaValue fOnUpdate = tTanksLibrary.get("onUpdate");
        if (fOnUpdate.type() != Lua.LuaType.NIL) SafeLuaRunner.safeCall(fOnUpdate);
    }

    private void onScreenChanged(Screen oldScreen, Screen newScreen) {
        Lua defaultState = SafeLuaRunner.defaultState;

        LuaValue tTanksLibrary = defaultState.get("tanks");
        LuaValue fOnScreenChange = tTanksLibrary.get("onScreenChange");

        if (fOnScreenChange.type() != Lua.LuaType.NIL) {
            SafeLuaRunner.safeCall(fOnScreenChange, oldScreen, newScreen);
        }

        if (newScreen instanceof ScreenGame screenGame) {
            LuaValue fOnLevelStart = tTanksLibrary.get("onLevelStart");
            if (fOnLevelStart.type() == Lua.LuaType.NIL) return;

            if (screenGame.name == null) {
                SafeLuaRunner.safeCall(fOnLevelStart, "__UNKNOWN__");
                return;
            }

            SafeLuaRunner.safeCall(fOnLevelStart, screenGame.name.replace(".tanks", ""));
            return;
        }

        if (oldScreen instanceof ScreenGame) {
            LuaValue fOnLevelEnd = tTanksLibrary.get("onLevelEnd");
            if (fOnLevelEnd.type() != Lua.LuaType.NIL) SafeLuaRunner.safeCall(fOnLevelEnd);
        }
    }
}
