package tools.important.tankslua.gui;

import party.iroiro.luajava.Lua;
import party.iroiro.luajava.lua54.Lua54;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.TextBox;
import tanks.gui.screen.Screen;
import tools.important.tankslua.Notification;
import tools.important.tankslua.SafeLuaRunner;
import tools.important.tankslua.TanksLua;

public class EvalBox {
    public Lua luaState;
    public TextBox evalCodeBox;
    public Button evalRunButton;

    public void draw() {
        evalCodeBox.draw();
        evalRunButton.draw();
    }
    public void update() {
        evalCodeBox.update();
        evalRunButton.update();
    }
    public EvalBox() {
        Screen screen = Game.screen;
        luaState = new Lua54();
        luaState.openLibraries();
        TanksLua.initializeState(luaState);

        evalCodeBox = new TextBox(screen.centerX, screen.objYSpace, screen.objWidth * 4, screen.objHeight, "Lua Code", () -> {
        }, "");
        evalCodeBox.allowLetters = true;
        evalCodeBox.allowSpaces = true;
        evalCodeBox.enableCaps = true;
        evalCodeBox.enablePunctuation = true;

        evalCodeBox.maxChars = 1000; // who's gonna write lua code longer than 1000 characters

        SafeLuaRunner runner = TanksLua.tanksLua.runner;

        evalRunButton = new Button(screen.centerX - screen.objXSpace * 1.37, screen.objYSpace * 2, screen.objWidth, screen.objHeight, "Evaluate", () -> {
            String code = evalCodeBox.inputText;
            SafeLuaRunner.LuaResult loadResult = runner.safeLoadString(luaState, code, "evalbox");
            final double youFuckedUpSecondsPerCharacter = 0.1;

            if (loadResult.status != Lua.LuaError.OK) {
                String error = loadResult.errorMessage;
                new Notification(Notification.NotificationType.WARN, youFuckedUpSecondsPerCharacter * error.length(), "Your code failed to load! " + error);
                return;
            }

            SafeLuaRunner.LuaResult callResult = runner.safeCall(loadResult.returns[0]);

            if (callResult.status != Lua.LuaError.OK) {
                String error = callResult.errorMessage;
                new Notification(Notification.NotificationType.WARN, youFuckedUpSecondsPerCharacter * error.length(), "Your code failed to run! " + error);
                return;
            }

            new Notification(Notification.NotificationType.INFO, 1, "Successfully ran your code!");
        });
    }
}
