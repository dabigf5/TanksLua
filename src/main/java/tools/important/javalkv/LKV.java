package tools.important.javalkv;

import java.util.HashMap;

/**
 * This is a utility class made for the purpose of parsing LKV ("Lazy Keys and Values"), a format that I have invented for the TanksLua project.<br><br>
 */
public class LKV {
    private LKV() {
    }

    private static LKVValue convert(String typeName, String rawValue) throws LKVParseException {
        LKVType pairType = LKVType.findType(typeName);

        if (pairType == null) throw new LKVParseException("Type " + typeName + " is not a valid type!");
        Object converted;

        try {
            converted = pairType.conversion.apply(rawValue.replace("\r", ""));
        } catch (Exception e) {
            throw new LKVParseException(e);
        }


        return new LKVValue(pairType, converted);
    }

    public static HashMap<String, LKVValue> parse(String lkv) throws LKVParseException {
        HashMap<String, LKVValue> pairs = new HashMap<>();
        String[] lkvFileSplit = lkv.split("\n");
        for (String line : lkvFileSplit) {
            if (line.trim().isEmpty()) continue; // blank line
            if (line.charAt(0) == '#') continue; // comment line

            String[] splitLine = line.split(" ", 4);
            if (splitLine.length < 3) throw new LKVParseException("Incorrect amount of data");

            String equalSign = splitLine[2];

            if (!equalSign.equals("=")) throw new LKVParseException("Expected equal sign");

            String typeName = splitLine[0];
            String keyName = splitLine[1];
            String rawValue = splitLine[3];

            //System.out.println("Type name: "+typeName+", Key name: "+keyName+", Raw value: "+rawValue);

            if (pairs.get(keyName) != null) throw new LKVParseException("Duplicate entry");

            LKVValue convertedValue = convert(typeName, rawValue);

            pairs.put(keyName, convertedValue);
        }

        return pairs;
    }

    /**
     * method to make sure the parser isn't busted
     *
     * @param args what do you think
     * @throws LKVParseException when the lkv is sus!
     */
    public static void main(String[] args) throws LKVParseException {
        HashMap<String, LKVValue> converted = parse(
                "version version = 1.0.0"
        );

        System.out.println(converted);
    }
}
