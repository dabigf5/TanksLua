package tools.important.tankslua.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.screen.Screen;
import tools.important.javalkv.LKVType;
import tools.important.tankslua.Notification;
import tools.important.tankslua.Option;
import tools.important.tankslua.gui.ToggleButton;
import tools.important.tankslua.gui.extensionoption.LXOEToggleButton;
import tools.important.tankslua.gui.extensionoption.LuaExtensionOptionElement;
import tools.important.tankslua.luapackage.LuaExtension;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class ScreenOptionsLuaInspectExtension extends Screen {
    private static final HashMap<LKVType, Class<? extends LuaExtensionOptionElement>> ELEMENT_TYPES = new HashMap<>();
    static {
        ELEMENT_TYPES.put(LKVType.BOOLEAN, LXOEToggleButton.class);
    }
    private final HashMap<String, LuaExtensionOptionElement> optionElements = new HashMap<>();


    public final LuaExtension extension;


    public Button backButton;
    public Button toggleEnabled;


    private final double optionsTextX = centerX-objWidth*1.5;
    private final double optionsTextY = centerY-objHeight*2;
    public ScreenOptionsLuaInspectExtension(LuaExtension extension) {
        this.extension = extension;
        this.music = "menu_options.ogg";
        this.musicID = "menu";

        double yPositionMultiplier = 3;

        for (Option option : extension.options) {
            double thisEntryY = optionsTextY+(objHeight*yPositionMultiplier);

            LKVType lkvType = option.type;
            Class<? extends LuaExtensionOptionElement> uiElementClassForOption = ELEMENT_TYPES.get(lkvType);

            if (uiElementClassForOption == null) {
                new Notification(Notification.NotificationType.WARN, 5, lkvType.typeName+" is not usable as an option type!");
                return;
            }

            LuaExtensionOptionElement newExtensionOptionElement;

            try {
                Constructor<? extends LuaExtensionOptionElement> constructor = uiElementClassForOption.getConstructor(

                );
                newExtensionOptionElement = constructor.newInstance();
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }

            optionElements.put(option.displayName, newExtensionOptionElement);

            yPositionMultiplier += 1;
        }

        this.backButton = new Button(
                centerX,
                centerY+objHeight*7,
                objWidth,
                objHeight,
                "Back",
                () -> Game.screen = new ScreenOptionsLuaExtensionList()
        );

        this.toggleEnabled = new ToggleButton(centerX, centerY+objHeight*5, objWidth, objHeight, "Enabled",
                () -> extension.enabled = true,
                () -> extension.enabled = false,
                extension.enabled
        );
    }
    @Override
    public void update() {
        backButton.update();
        toggleEnabled.update();
        for (LuaExtensionOptionElement e: optionElements.values()) {
            e.update();
        }
    }

    @Override
    public void draw() {
        drawDefaultBackground();

        backButton.draw();
        toggleEnabled.draw();

        Drawing.drawing.setColor(0,0,0);


        Drawing.drawing.setInterfaceFontSize(titleSize);
        Drawing.drawing.displayInterfaceText(centerX, centerY-objHeight*5, extension.name);

        Drawing.drawing.setInterfaceFontSize(titleSize*0.8);
        Drawing.drawing.displayInterfaceText(centerX, centerY-objHeight*4, "by "+extension.authorName);

        Drawing.drawing.setInterfaceFontSize(titleSize*0.7);
        Drawing.drawing.displayInterfaceText(centerX, centerY-objHeight*3, extension.description);

        Drawing.drawing.setInterfaceFontSize(titleSize*0.6);
        Drawing.drawing.displayInterfaceText(centerX, centerY-objHeight*2, extension.version.toVersionString());

        if (extension.options.isEmpty()) return;

        Drawing.drawing.setFontSize(titleSize);
        Drawing.drawing.drawText(optionsTextX, optionsTextY, "Options");
        Drawing.drawing.setFontSize(titleSize*0.7);

        for (Map.Entry<String, LuaExtensionOptionElement> entry: optionElements.entrySet()) {
            Drawing.drawing.setInterfaceFontSize(titleSize);

            LuaExtensionOptionElement ui = entry.getValue();

            ui.draw();
        }
    }
}
