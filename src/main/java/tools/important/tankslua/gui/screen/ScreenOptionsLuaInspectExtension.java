package tools.important.tankslua.gui.screen;

import party.iroiro.luajava.Lua;
import party.iroiro.luajava.value.LuaValue;
import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.screen.Screen;
import tools.important.javalkv.LKVType;
import tools.important.tankslua.SafeLuaRunner;
import tools.important.tankslua.TanksLua;
import tools.important.tankslua.gui.ToggleOptionButton;
import tools.important.tankslua.gui.extensionoption.Checkbox;
import tools.important.tankslua.gui.extensionoption.ExtensionOptionElement;
import tools.important.tankslua.luapackage.LuaExtension;

import java.util.HashMap;
import java.util.Map;

public class ScreenOptionsLuaInspectExtension extends Screen {
    private static final double beginningOptionYPositionMultiplier = 1;
    private static final double yPositionMultiplierAdded = 2;
    private static final HashMap<LKVType, Class<? extends ExtensionOptionElement>> elementTypes = new HashMap<>();
    static {
        elementTypes.put(LKVType.BOOLEAN, Checkbox.class);
    }
    public final LuaExtension extension;
    private final HashMap<String, ExtensionOptionElement> optionElements = new HashMap<>();
    public Button backButton;
    public Button toggleEnabled;

    private final double optionsTextX = centerX-objWidth*1.5;
    private final double optionsTextY = centerY-objHeight*2;
    public ScreenOptionsLuaInspectExtension(LuaExtension extension) {
        this.extension = extension;
        this.music = "menu_options.ogg";
        this.musicID = "menu";

        double yPositionMultiplier = beginningOptionYPositionMultiplier;

        for (Map.Entry<String, LKVType> optionType : extension.optionTypes.entrySet()) {
            double thisEntryY = optionsTextY+(objHeight*yPositionMultiplier);
            String optionName = optionType.getKey();
            LKVType lkvType = optionType.getValue();
            Class<? extends ExtensionOptionElement> uiElementClassForOption = elementTypes.get(lkvType);

            ExtensionOptionElement newExtensionOptionElement;

            try {
                newExtensionOptionElement = uiElementClassForOption.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            optionElements.put(optionName, newExtensionOptionElement);
            newExtensionOptionElement.setPosition(optionsTextX+centerX/3, thisEntryY);
            newExtensionOptionElement.setInitialState(extension.optionValues.get(optionName));
            newExtensionOptionElement.setOnValueChanged((Object newValue) -> {
                extension.optionValues.put(optionName, newValue);

                LuaValue fOnNewOptions = extension.callbacks.get("onNewOptions");
                if (fOnNewOptions.type() == Lua.LuaType.NIL) return;

                SafeLuaRunner.safeCall(fOnNewOptions, extension.optionValues.getLuaTable(TanksLua.tanksLua.internalLuaState));
            });
            yPositionMultiplier += yPositionMultiplierAdded;
        }

        this.backButton = new Button(
                centerX,
                centerY+objHeight*7,
                objWidth,
                objHeight,
                "Back",
                () -> Game.screen = new ScreenOptionsLuaExtensionList()
        );

        this.toggleEnabled = new ToggleOptionButton(centerX, centerY+objHeight*5, objWidth, objHeight, "Enabled",
                () -> extension.enabled = true,
                () -> extension.enabled = false,
                extension.enabled
        );
    }
    @Override
    public void update() {
        backButton.update();
        toggleEnabled.update();
        for (ExtensionOptionElement e: optionElements.values()) {
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

        if (extension.optionTypes.isEmpty()) return;


        Drawing.drawing.setFontSize(titleSize);
        Drawing.drawing.drawText(optionsTextX, optionsTextY, "Options");
        Drawing.drawing.setFontSize(titleSize*0.7);
        double yPositionMultiplier = beginningOptionYPositionMultiplier;

        for (Map.Entry<String, ExtensionOptionElement> entry: optionElements.entrySet()) {
            double thisEntryY = optionsTextY+(objHeight*yPositionMultiplier);
            Drawing.drawing.setInterfaceFontSize(titleSize);
            String textDrawn = entry.getKey()+":";

            Drawing.drawing.drawText(optionsTextX, thisEntryY, textDrawn);
            ExtensionOptionElement ui = entry.getValue();

            ui.draw();
            yPositionMultiplier += yPositionMultiplierAdded;
        }
    }
}
