package tools.important.tankslua.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.screen.Screen;
import tools.important.tankslua.LuaExtension;
import tools.important.tankslua.SafeLuaRunner;
import tools.important.tankslua.gui.ToggleOptionButton;

public class ScreenOptionsLuaExtensionAbout extends Screen {
    private final LuaExtension extension;
    public ToggleOptionButton enabledToggle;
    public Button backButton;
    public ScreenOptionsLuaExtensionAbout(LuaExtension extension) {
        this.extension = extension;

        this.backButton = new Button(centerX, centerY+objHeight*7, objWidth, objHeight, "Back", () -> Game.screen = new ScreenOptionsLuaExtensionList());

    }
    @Override
    public void update() {
        backButton.update();
    }

    @Override
    public void draw() {
        drawDefaultBackground();

        backButton.draw();

        Drawing.drawing.setColor(0,0,0);


        Drawing.drawing.setInterfaceFontSize(titleSize);
        Drawing.drawing.displayInterfaceText(centerX, centerY-objHeight*5, extension.name);

        Drawing.drawing.setInterfaceFontSize(titleSize*0.8);
        Drawing.drawing.displayInterfaceText(centerX, centerY-objHeight*4, "by "+extension.authorName);

        Drawing.drawing.setInterfaceFontSize(titleSize*0.7);
        Drawing.drawing.displayInterfaceText(centerX, centerY-objHeight*3, extension.description);

        Drawing.drawing.setInterfaceFontSize(titleSize*0.6);
        Drawing.drawing.displayInterfaceText(centerX, centerY-objHeight*2, extension.getVersionString());
    }
}
