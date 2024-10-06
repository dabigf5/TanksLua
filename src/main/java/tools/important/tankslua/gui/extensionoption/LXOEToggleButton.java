package tools.important.tankslua.gui.extensionoption;

import tools.important.tankslua.gui.ToggleButton;


public class Checkbox extends ToggleButton {
    public static final double CHECKBOX_SIZE = 75;
    private boolean isChecked;

    public Checkbox() {
        super(0,0, CHECKBOX_SIZE, CHECKBOX_SIZE, "");
    }
}
