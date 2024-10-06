package tools.important.javalkv;

@FunctionalInterface
public interface LKVValueEncoder {
    String encode(Object value);
}
