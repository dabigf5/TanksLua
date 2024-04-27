package tools.important.javalkv;

public class LKVParseException extends RuntimeException {
    public LKVParseException(String message) {
        super(message);
    }
    public LKVParseException(Throwable t) {
        super(t);
    }
}
