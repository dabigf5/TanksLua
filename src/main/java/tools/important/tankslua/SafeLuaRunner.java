package tools.important.tankslua;

import party.iroiro.luajava.Consts;
import party.iroiro.luajava.Lua;
import party.iroiro.luajava.value.LuaValue;

import java.util.HashMap;
import java.util.Map;

public class SafeLuaRunner {
    private SafeLuaRunner(){}
    public static boolean autoLogErrors = true;
    public static Lua defaultState;

    /**
     * Calls the given lua function and properly handles any errors that happen with it.
     * @param userFunction The function to call
     * @param parameters The parameters to give that function
     * @return A record which describes if the operation succeeded or failed, its return values, or the amount of values it pushed onto the stack. The last two are mutually exclusive.
     */
    public static UserCallResult safeCall(LuaValue userFunction, Object... parameters) {
        return safeCall(true, userFunction, parameters);
    }

    /**
     * Calls the given lua function and properly handles any errors that happen with it.
     * @param popReturns Do we want to pop the returns of the function off the stack and put them in the record, or leave them on the stack and put the amount of values added to the stack in the record?
     * @param userFunction The function to call
     * @param parameters The parameters to give that function
     * @return A UserCallResult record describing the result of the function call
     */
    public static UserCallResult safeCall(boolean popReturns, LuaValue userFunction, Object... parameters) {
        Lua luaState = userFunction.state();

        int top = luaState.getTop();
        userFunction.push();
        for (Object o : parameters) {
            luaState.push(o, Lua.Conversion.SEMI);
        }

        Lua.LuaError result = luaState.pCall(parameters.length, Consts.LUA_MULTRET);

        if (result != Lua.LuaError.OK) {
            if (autoLogErrors) {
                System.out.println("Lua error: "+result+"; "+ luaState.toString(luaState.getTop()));
            }
            luaState.pop(1);
            return new UserCallResult(result);
        }


        int returnCount = luaState.getTop() - top;

        if (!popReturns) {
            return new UserCallResult(Lua.LuaError.OK, returnCount);
        }

        LuaValue[] values = new LuaValue[returnCount];
        for (int i = 0; i < returnCount; i++) {
            values[returnCount - i - 1] = luaState.get();
        }
        return new UserCallResult(Lua.LuaError.OK, values);
    }

    /**
     * A record which describes the result of a function call performed with safeCall.
     * The last two entries in this record are mutually exclusive. This is because a call cannot return both
     * the values it returned and leave values on the stack.
     * @param status If the operation succeeded or failed
     * @param returns The return values given by the function
     * @param unpoppedReturns The returns we've pushed onto the lua stack
     */
    record UserCallResult(Lua.LuaError status, LuaValue[] returns, Integer unpoppedReturns){
        public UserCallResult(Lua.LuaError status) {this(status,null,null);}
        public UserCallResult(Lua.LuaError status, LuaValue[] returns) {this(status,returns,null);}
        public UserCallResult(Lua.LuaError status, Integer unpoppedReturns) {this(status,null,unpoppedReturns);}
    }
    /**
     * Loads a Lua file, calls it, and properly handles any syntax errors that may occur.
     * This method uses a set of defaults to fill in the missing other parameters.
     *
     * @param filePathToLoad The path to the lua file you wish to load.
     * @return The function loaded from the lua file, or null if the load failed
     */
    public static LuaValue safeLoadFile(String filePathToLoad) {
        return safeLoadFile(defaultState, filePathToLoad);
    }
    private static LuaValue safeLoadFile(Lua luaState, String filePathToLoad) {
        return safeLoadFile(luaState, filePathToLoad, null);
    }

    /**
     * Loads a Lua file, calls it, and properly handles any syntax errors that may occur.
     * @param luaState The lua state to load the lua file with.
     * @param filePathToLoad The path to the lua file you wish to load.
     * @param tEnv The environment to load the file in. Can be null.
     * @return The function loaded from the lua file, or null if the load failed
     */
    public static LuaValue safeLoadFile(Lua luaState, String filePathToLoad, LuaValue tEnv) {
        return safeCallLoadFunc(luaState, "loadfile", filePathToLoad, tEnv);
    }

    /**
     * Loads a string and properly handles any syntax errors that may occur.
     * @param stringToLoad The string you wish to load
     * @return That string loaded as a function, or null if the load failed
     */
    public static LuaValue loadStringAndHandleSyntaxErrors(String stringToLoad) {
        return loadStringAndHandleSyntaxErrors(defaultState, stringToLoad);
    }
    /**
     * Loads a string and properly handles any syntax errors that may occur.
     * @param luaState The lua state to load the string with
     * @param stringToLoad The string you wish to load
     * @return That string loaded as a function, or null if the load failed
     */
    public static LuaValue loadStringAndHandleSyntaxErrors(Lua luaState, String stringToLoad) {
        return safeCallLoadFunc(luaState, "load", stringToLoad, null);
    }

    private static LuaValue safeCallLoadFunc(Lua luaState, String funcName, String argument, LuaValue tEnv) {
        LuaValue[] loadResult;

        if (tEnv != null) {
            loadResult = luaState.get(funcName).call(argument, null, tEnv);
        } else {
            loadResult = luaState.get(funcName).call(argument, null);
        }

        LuaValue fLoadedString = loadResult[0];
        if (fLoadedString.type() == Lua.LuaType.NIL) {
            System.out.println("Lua: Error loading! "+loadResult[1].toJavaObject());
            return null;
        }
        return fLoadedString;
    }

    public record TableType(Lua.LuaType type, boolean optional) {}
    public record VerificationResult(boolean verified, String message) {
        private VerificationResult(boolean verified) {this(verified, null);}
    }
    public static VerificationResult verifyTable(LuaValue tableToVerify, HashMap<String, TableType> tableTypes) {
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
}
