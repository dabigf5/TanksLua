package tools.important.tankslua.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.screen.Screen;
import tanks.gui.screen.ScreenOptions;
import tools.important.tankslua.LuaExtension;
import tools.important.tankslua.TanksLua;
import tools.important.tankslua.gui.ToggleOptionButton;

import java.util.ArrayList;

public class ScreenOptionsLua extends Screen {
    public Button backButton;
    public Button extensionListButton;

    public Button enableLevelScriptsButton;

    public ScreenOptionsLua() {
        backButton = new Button(centerX, centerY + objYSpace * 3.5, objWidth, objHeight, "Back", () -> Game.screen = new ScreenOptions());

        enableLevelScriptsButton = new ToggleOptionButton(centerX, centerY-objHeight*3, objWidth, objHeight, "Level scripts",
                () -> TanksLua.tanksLua.options.put("enableLevelScripts", true),
                () -> TanksLua.tanksLua.options.put("enableLevelScripts", false),
                (boolean)TanksLua.tanksLua.options.get("enableLevelScripts"),
                "Whether or not the extension should run scripts for specific levels------Please note that turning this off may cause problems with some levels---that depend on scripts to function!");

        extensionListButton = new Button(centerX, centerY+objHeight*4, objWidth, objHeight, "Loaded extension list", () -> {
            Game.screen = new ScreenOptionsLuaExtensionList();
        });
    }

    @Override
    public void update() {
        backButton.update();
        enableLevelScriptsButton.update();
        extensionListButton.update();
    }

    @Override
    public void draw() {
        drawDefaultBackground();
        backButton.draw();
        enableLevelScriptsButton.draw();
        extensionListButton.draw();
        Drawing.drawing.setColor(0,0,0);
        Drawing.drawing.setInterfaceFontSize(titleSize);
        Drawing.drawing.displayInterfaceText(centerX, centerY - objYSpace * 3.5, "Lua Options");
    }
}
