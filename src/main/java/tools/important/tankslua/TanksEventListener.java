package tools.important.tankslua;

import party.iroiro.luajava.Lua;
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
        Lua defaultState = SafeLuaRunner.defaultState;
    }

    private void onScreenChanged(Screen oldScreen, Screen newScreen) {
    }
}
