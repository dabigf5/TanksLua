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
        throw new LuaException(fileName+": "+problem);
    }

    public LuaScript(String fileName) {
        this.fileName = fileName;
    }

    protected boolean quickVerify(LuaValue table, HashMap<String, TableType> tableTypes) {
        VerificationResult verificationResult = verifyTable(table, tableTypes);

        if (!verificationResult.verified) {
            onVerificationError(fileName, verificationResult.message);
            return false;
        }
        return true;
    }

    /**
     * Verify that a table, for one, is a table, and for two, has all of the proper values the table should have.
     * @param tableToVerify The table you wish to verify.
     * @param tableTypes A HashMap describing what types the table's keys should be.
     * @return A record that describes how the verification went. If verified is true, then message will be null.
     */
    protected static VerificationResult verifyTable(LuaValue tableToVerify, HashMap<String, TableType> tableTypes) {
        if (tableToVerify.type() != Lua.LuaType.TABLE) {
            return new VerificationResult(false, "Table given is of wrong type");
        }

        for (Map.Entry<String, TableType> typeEntry : tableTypes.entrySet()) {
            String name = typeEntry.getKey();
            TableType type = typeEntry.getValue();
            Lua.LuaType luatype = type.type;
            boolean isOptional = type.optional;

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

    public static class TableType {
        public Lua.LuaType type;
        public boolean optional;
        public TableType(Lua.LuaType type, boolean optional) {
            this.type = type;
            this.optional = optional;
        }
        public static TableType fromString(String typestr) {
            boolean optional = typestr.endsWith("?");
            String typeName = typestr.replace("?","").toUpperCase();
            Lua.LuaType luaType = Lua.LuaType.valueOf(typeName);

            return new TableType(luaType, optional);
        }
    }

    static class VerificationResult {
        public boolean verified;
        public String message;
        private VerificationResult(boolean verified, String message) {
            this.verified = verified;
            this.message = message;
        }
        private VerificationResult(boolean verified) {this(verified, null);}
    }
}