package tools.important.tankslua.gui.extensionoption;

public interface ExtensionOptionElement {
    void draw();
    void update();
    void setPosition(double x, double y);
    void setInitialState(Object initialState);
}
