package tools.important.tankslua.gui;

import tanks.gui.Button;
import tanks.gui.screen.ScreenOptions;

public class ToggleButton extends Button {
    private final String onStr;
    private final String offStr;
    private final Runnable onEnable;
    private final Runnable onDisable;
    private final String prefix;

    private boolean optionEnabled;

    public void setOptionEnabled(boolean enabled) {
        optionEnabled = enabled;

        if (optionEnabled) {
            setText(onStr);
            onEnable.run();
        } else {
            setText(offStr);
            onDisable.run();
        }
    }

    public boolean getOptionEnabled() {
        return optionEnabled;
    }

    public ToggleButton(double x, double y, double sX, double sY, String optionName, Runnable onEnable, Runnable onDisable, boolean initialState) {
        this(x,y,sX,sY,optionName,onEnable,onDisable,initialState,null);
    }
    public ToggleButton(double x, double y, double sX, double sY, String optionName, Runnable onEnable, Runnable onDisable, boolean initialState, String hoverText) {
        super(x, y, sX, sY, "");
        prefix = optionName+": ";

        onStr = prefix+ScreenOptions.onText;
        offStr = prefix+ScreenOptions.offText;
        this.onEnable = onEnable;
        this.onDisable = onDisable;

        setOptionEnabled(initialState);

        if (hoverText != null) {
            setHoverText(hoverText);
            this.enableHover = true;
        }

        this.function = () -> setOptionEnabled(!optionEnabled);

        this.enabled = true;
    }
}
