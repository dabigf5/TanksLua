package tools.important.javalkv;

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
            obj -> {
                String string = (String) obj;

                return '"'+
                        string
                                .replace("\\", "\\\\")
                                .replace("\"", "\\\"")
                                .replace("\f", "\\f")
                                .replace("\r", "\\r")
                                .replace("\b", "\\b")
                                .replace("\t", "\\t")
                                .replace("\n", "\\n")
                        +'"';
            },
            String.class
    ),
    INT("int", Integer::parseInt, String::valueOf, Integer.class),
    FLOAT("float", Float::parseFloat, String::valueOf, Float.class),
    BOOLEAN("boolean", str -> {
        boolean isTrue = str.equals("true");
        boolean isFalse = str.equals("false");

        if (!(isTrue || isFalse)) throw new LKVParseException("Invalid boolean");

        return isTrue;
    },
            String::valueOf,
            Boolean.class),



    VERSION(
            "version",
            SemanticVersion::fromVersionString,
            version -> ((SemanticVersion)version).toVersionString(),
            SemanticVersion.class
    ),
    TYPE(
            "type",
            str -> {
                LKVType type = findType(str);
                if (type == null) throw new LKVParseException("Invalid type");
                return type;
            },
            type -> ((LKVType) type).typeName,
            LKVType.class
    ),

//    CLASS("class", str -> {
//        try {
//            return Class.forName(str);
//        } catch (ClassNotFoundException ignored) {
//            throw new LKVParseException("Class could not be found");
//        }
//    }, Class.class),
    ;

    public final String typeName;
    final LKVValueDecoder decoder;
    final LKVValueEncoder encoder;
    final Class<?> expectedClass;
    LKVType(String typeName, LKVValueDecoder decoder, LKVValueEncoder encoder, Class<?> expectedClass) {
        this.typeName = typeName;
        this.decoder = decoder;
        this.encoder = encoder;
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