package tools.important.javalkv;

@FunctionalInterface
public interface LKVValueDecoder {
    Object decode(String lkv) throws LKVParseException;
}
