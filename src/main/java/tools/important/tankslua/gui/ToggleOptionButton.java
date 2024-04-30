package tools.important.tankslua.gui;

import tools.important.tankslua.TanksLua;

public class ToggleOptionButton extends ToggleButton {
    public ToggleOptionButton(double x, double y, double sX, double sY, String optionName, Runnable onEnable, Runnable onDisable, boolean initialState) {
        this(x, y, sX, sY, optionName, onEnable, onDisable, initialState, null);
    }

    public ToggleOptionButton(double x, double y, double sX, double sY, String optionName, Runnable onEnable, Runnable onDisable, boolean initialState, String hoverText) {
        super(x, y, sX, sY, optionName, onEnable, onDisable, initialState, hoverText);

        Runnable baseFunction = this.function;

        this.function = () -> {
            baseFunction.run();
            TanksLua.tanksLua.saveOptions();
        };
    }
}
