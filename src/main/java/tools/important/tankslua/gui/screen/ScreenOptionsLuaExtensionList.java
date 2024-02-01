package tools.important.tankslua.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.ButtonList;
import tanks.gui.screen.Screen;
import tools.important.tankslua.LuaExtension;
import tools.important.tankslua.TanksLua;

import java.util.ArrayList;
public class ScreenOptionsLuaExtensionList extends Screen {
    public Button backButton;
    public Button reloadExtensionsButton;
    private ButtonList extensionButtonList;

    public ScreenOptionsLuaExtensionList() {
        backButton = new Button(centerX, centerY + objYSpace * 5.4, objWidth, objHeight, "Back", () -> Game.screen = new ScreenOptionsLua());
        reloadExtensionsButton = new Button(centerX, centerY+objHeight*6.4, objWidth, objHeight, "Reload Lua Extensions", () -> {
            TanksLua.tanksLua.loadedLuaExtensions.clear();
            LuaExtension.registerExtensionsFromDir();
            updateExtensionButtonList();
        });

        updateExtensionButtonList();
    }

    private void updateExtensionButtonList() {
        ArrayList<LuaExtension> extList = TanksLua.tanksLua.loadedLuaExtensions;
        ArrayList<Button> buttons = new ArrayList<>();
        for (LuaExtension extension: extList) {
            buttons.add(new Button(0,0,0,0,
                    extension.name,
                    () -> Game.screen = new ScreenOptionsLuaInspectExtension(extension),
                    "created by "+extension.authorName+"---["+extension.fileName+"]"
            ));
        }

        extensionButtonList = new ButtonList(buttons, 1, 0, -this.objYSpace*1.5);
    }

    @Override
    public void update() {
        backButton.update();
        reloadExtensionsButton.update();
        extensionButtonList.update();
    }

    @Override
    public void draw() {
        drawDefaultBackground();
        Drawing.drawing.setColor(0,0,0);
        Drawing.drawing.setInterfaceFontSize(titleSize);
        Drawing.drawing.displayInterfaceText(centerX, centerY-objHeight*8, "Loaded Lua Extensions");
        backButton.draw();
        reloadExtensionsButton.draw();
        extensionButtonList.draw();
    }
}
