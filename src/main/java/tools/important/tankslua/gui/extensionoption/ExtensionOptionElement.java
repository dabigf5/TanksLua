package tools.important.tankslua.gui.extensionoption;

import java.util.function.Consumer;

public interface ExtensionOptionElement {
    void draw();
    void update();


    void setPosition(double x, double y);

    double getPosX();
    double getPosY();

    double getWidth();
    double getHeight();


    void setInitialState(Object initialState);
    void setOnValueChanged(Consumer<Object> onValueChanged);
}
