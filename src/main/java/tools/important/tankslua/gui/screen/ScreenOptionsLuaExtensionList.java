package tools.important.tankslua.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.ButtonList;
import tanks.gui.screen.Screen;
import tools.important.tankslua.Notification;
import tools.important.tankslua.TanksLua;
import tools.important.tankslua.luapackage.LuaExtension;

import java.util.ArrayList;
public class ScreenOptionsLuaExtensionList extends Screen {
    public Button backButton;
    public Button reloadExtensionsButton;
    private ButtonList extensionButtonList;

    public ScreenOptionsLuaExtensionList() {
        this.music = "menu_options.ogg";
        this.musicID = "menu";

        backButton = new Button(centerX, centerY + objYSpace * 5.4, objWidth, objHeight, "Back", () -> Game.screen = new ScreenOptionsLua());
        reloadExtensionsButton = new Button(centerX, centerY+objHeight*6.4, objWidth, objHeight, "Reload Lua Extensions", () -> {
            TanksLua.tanksLua.loadedLuaExtensions.clear();
            LuaExtension.loadExtensionsTo(TanksLua.tanksLua.loadedLuaExtensions);
            updateExtensionButtonList();

            new Notification(Notification.NotificationType.INFO, 1, "Extensions reloaded!");
        });

        updateExtensionButtonList();
    }

    private void updateExtensionButtonList() {
        ArrayList<LuaExtension> extList = TanksLua.tanksLua.loadedLuaExtensions;
        ArrayList<Button> buttons = new ArrayList<>();
        for (LuaExtension extension: extList) {
            String disabledString = "";

            if (!extension.enabled) {
                disabledString = "[disabled]";
            }

            buttons.add(new Button(0,0,0,0,
                    extension.name,
                    () -> Game.screen = new ScreenOptionsLuaInspectExtension(extension),
                    "by "+extension.authorName + "---" + extension.description + "---" + disabledString
            ));
        }

        extensionButtonList = new ButtonList(buttons, 0, 0, -this.objYSpace*1.5);
        extensionButtonList.objWidth *= 3;
        extensionButtonList.columns = 1;

        extensionButtonList.sortButtons();
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
