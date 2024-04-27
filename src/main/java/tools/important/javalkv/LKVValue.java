package tools.important.javalkv;

public class LKVValue {
    public final LKVType type;
    public final Object value;

    LKVValue(LKVType type, Object value) {
        if (!type.expectedClass.isAssignableFrom(value.getClass()))
            throw new IllegalStateException("Attempt to create LKVValue with invalid value type!");

        this.type = type;
        this.value = value;
    }

    @Override
    public String toString() {
        return "[LKVValue, type="+type+", value="+value+"]";
    }
}
