package tools.important.tankslua.gui;

import tanks.gui.Button;
import tanks.gui.screen.ScreenOptions;

public class ToggleButton extends Button {
    private boolean optionEnabled;

    public ToggleButton(double x, double y, double sX, double sY, String optionName, Runnable onEnable, Runnable onDisable, boolean initialState) {
        this(x,y,sX,sY,optionName,onEnable,onDisable,initialState,null);
    }
    public ToggleButton(double x, double y, double sX, double sY, String optionName, Runnable onEnable, Runnable onDisable, boolean initialState, String hoverText) {
        super(x, y, sX, sY, optionName+": ");
        final String onStr = text+ ScreenOptions.onText;
        final String offStr = text+ScreenOptions.offText;
        optionEnabled = initialState;

        if (hoverText != null) {
            setHoverText(hoverText);
            this.enableHover = true;
        }

        if (optionEnabled) {
            setText(onStr);
        } else {
            setText(offStr);
        }

        this.function = () -> {
            optionEnabled = !optionEnabled;

            if (optionEnabled) {
                setText(onStr);
                onEnable.run();
            } else {
                setText(offStr);
                onDisable.run();
            }
        };

        this.enabled = true;
    }
}
