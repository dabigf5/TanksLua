package tools.important.javalkv;

import java.util.function.Function;

public enum LKVType {
    STRING("string", str -> {
        if (!(str.startsWith("\"") && str.endsWith("\"")))
            throw new LKVParseException("String is not quoted");

        if (str.matches("\\\\[^ntbrf\"\\\\]"))
            throw new LKVParseException("String contains unfinished escapes");

        int strLength = str.length();

        if (strLength < 2)
            throw new LKVParseException("String is too short");

        return str
                .substring(1, str.length() - 1)
                .replace("\\n", "\n")
                .replace("\\t", "\t")
                .replace("\\b", "\b")
                .replace("\\r", "\r")
                .replace("\\f", "\f")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    },
            String.class),
    INT("int", Integer::parseInt, Integer.class),
    FLOAT("float", Float::parseFloat, Float.class),
    BOOLEAN("boolean", str -> {
        boolean isTrue = str.equals("true");
        boolean isFalse = str.equals("false");

        if (!(isTrue || isFalse)) throw new LKVParseException("Invalid boolean");

        return isTrue;
    }, Boolean.class),



    VERSION("version", SemanticVersion::fromVersionString, SemanticVersion.class),
    TYPE("type", str -> {
        LKVType type = findType(str);
        if (type == null) throw new LKVParseException("Invalid type");
        return type;
    }, LKVType.class)

//    CLASS("class", str -> {
//        try {
//            return Class.forName(str);
//        } catch (ClassNotFoundException ignored) {
//            throw new LKVParseException("Class could not be found");
//        }
//    }, Class.class),
    ;

    public final String typeName;
    public final Function<String, Object> conversion;
    public final Class<?> expectedClass;
    LKVType(String typeName, Function<String, Object> conversion, Class<?> expectedClass) {
        this.typeName = typeName;
        this.conversion = conversion;
        this.expectedClass = expectedClass;
    }



    public static LKVType findType(String typeName) {
        LKVType pairType = null;
        for (LKVType type : LKVType.values()) {
            if (type.typeName.equals(typeName)) {
                pairType = type;
                break;
            }
        }
        return pairType;
    }
}