package tools.important.tankslua.gui.extensionoption;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;

public class Checkbox extends Button implements ExtensionOptionElement {
    public static final double CHECKBOX_SIZE = 75;
    private boolean isChecked;
    public Checkbox() {
        super(0,0, CHECKBOX_SIZE, CHECKBOX_SIZE, "");
        this.enabled = true;
        this.function = () -> setChecked(!isChecked);
    }

    public void setChecked(boolean checked) {
        this.isChecked = checked;
    }

    @Override
    public void draw() {
        Drawing.drawing.setColor(255/2d,255/2d,255/2d);
        Drawing.drawing.fillRect(posX, posY, sizeX*1.1, sizeY*1.1);
        Drawing.drawing.setColor(255,255,255);
        Drawing.drawing.fillRect(posX, posY, sizeX, sizeY);
        Drawing.drawing.setColor(0,0,0);
        Drawing.drawing.setInterfaceFontSize(Game.screen.titleSize*1.5);
        if (!isChecked) {
            Drawing.drawing.displayInterfaceText(posX, posY, "X");
            return;
        }

        Drawing.drawing.displayInterfaceText(posX, posY, "v");
        Drawing.drawing.displayInterfaceText(posX+10, posY, "/");
    }

    @Override
    public void setInitialState(Object initialState) {
        setChecked((boolean) initialState);
    }
}
