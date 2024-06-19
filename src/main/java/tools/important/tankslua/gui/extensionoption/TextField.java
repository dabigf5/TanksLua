package tools.important.tankslua.gui.extensionoption;

import tanks.gui.TextBox;

import java.util.function.Consumer;

public class TextField extends TextBox implements ExtensionOptionElement {
    public TextField() {
        super(0, 0, 0, 0, "", ()->{}, null);
    }

    @Override
    public void draw() {
        super.draw();
    }

    @Override
    public void update() {
        super.update();
    }

    @Override
    public void setPosition(double x, double y) {
        posX = x;
        posY = y;
    }

    @Override
    public double getPosX() {
        return posX;
    }

    @Override
    public double getPosY() {
        return posY;
    }

    @Override
    public double getWidth() {
        return sizeX;
    }

    @Override
    public double getHeight() {
        return sizeY;
    }

    @Override
    public void setInitialState(Object initialState) {
        setText((String) initialState);
    }


    public Consumer<Object> onValueChanged;
    @Override
    public void setOnValueChanged(Consumer<Object> onValueChanged) {
        this.onValueChanged = onValueChanged;
    }


    @Override
    public void submit() {
        this.onValueChanged.accept(this.inputText);
        super.submit();
    }
}
