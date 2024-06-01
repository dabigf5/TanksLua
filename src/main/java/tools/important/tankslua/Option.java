package tools.important.tankslua;

import tools.important.javalkv.LKVType;

public class Option {
    public String name;
    public LKVType type;
    public Object value;
    public Object defaultValue;
    public Option(String name, LKVType type, Object defaultValue) {
        this.name = name;
        this.type = type;
        this.defaultValue = defaultValue;
    }
}