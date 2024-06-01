package tools.important.tankslua;

import tools.important.javalkv.LKVType;

public class Option {
    public String displayName;
    public String name;
    public LKVType type;
    public Object value;
    public Object defaultValue;


    public Option(String name, String displayName, LKVType type, Object defaultValue) {
        this.name = name;
        this.displayName = displayName;
        this.type = type;
        this.defaultValue = defaultValue;
    }
}