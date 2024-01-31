package tools.important.tankslua;

import party.iroiro.luajava.Lua;
import party.iroiro.luajava.LuaException;
import party.iroiro.luajava.value.LuaValue;

import java.util.HashMap;
import java.util.Map;

public abstract class LuaScript {
    public String fileName;

    protected LuaScript() {} // for debugging purposes
    public void onVerificationError(String fileName, String problem) {
        System.out.println(fileName+": "+problem);
    }

    /**
     * Initializes a LuaScript, and properly checks its table.
     * @param fileName The name of the file you're loading the LuaScript from
     * @param table The table returned by the file.
     * @param tableTypes The proper types of the table.
     */
    public LuaScript(String fileName, LuaValue table, HashMap<String, TableType> tableTypes) {
        this.fileName = fileName;

        VerificationResult verificationResult = verifyTable(table, tableTypes);

        if (!verificationResult.verified()) {
            onVerificationError(fileName, verificationResult.message());
            throw new LuaException(fileName+": "+verificationResult.message());
        }
    }

    /**
     * Verify that a table, for one, is a table, and for two, has all of the proper values the table should have.
     * @param tableToVerify The table you wish to verify.
     * @param tableTypes A HashMap describing what types the table's keys should be.
     * @return A record that describes how the verification went. If verified is true, then message will be null.
     */
    private static VerificationResult verifyTable(LuaValue tableToVerify, HashMap<String, TableType> tableTypes) {
        if (tableToVerify.type() != Lua.LuaType.TABLE) {
            return new VerificationResult(false, "Table given is of wrong type");
        }

        for (Map.Entry<String, TableType> typeEntry : tableTypes.entrySet()) {
            String name = typeEntry.getKey();
            TableType type = typeEntry.getValue();
            Lua.LuaType luatype = type.type();
            boolean isOptional = type.optional();

            LuaValue entry = tableToVerify.get(name);
            Lua.LuaType entryType = entry.type();
            if (entryType == Lua.LuaType.NIL) {
                if (!isOptional) {
                    return new VerificationResult(false, "Missing required key "+name);
                }

                continue;
            }

            if (entryType != luatype) {
                return new VerificationResult(false, "Key "+name+" is of the wrong type (expected "+luatype+", got "+entryType+")");
            }
        }

        return new VerificationResult(true);
    }

    public record TableType(Lua.LuaType type, boolean optional) {}

    public record VerificationResult(boolean verified, String message) {
        private VerificationResult(boolean verified) {this(verified, null);}
    }
}
