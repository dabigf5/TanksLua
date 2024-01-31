package tools.important.tankslua;

import party.iroiro.luajava.Lua;
import party.iroiro.luajava.LuaException;
import party.iroiro.luajava.value.LuaValue;

import java.util.HashMap;
import java.util.Map;

public abstract class LuaScript {
    public String fileName;
    public LuaValue fOnLoad;
    public LuaValue fOnUpdate;
    public LuaValue fOnDraw;

    protected LuaScript() {} // for debugging purposes
    public String formatVerifyErrorMessage(String fileName, String problem) {
        return fileName+": "+problem;
    }
    public LuaScript(String fileName, LuaValue table, HashMap<String, TableType> tableTypes) {
        this.fileName = fileName;

        VerificationResult verificationResult = verifyTable(table, tableTypes);

        if (!verificationResult.verified()) {
            throw new LuaException(formatVerifyErrorMessage(fileName, verificationResult.message()));
        }
    }

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
