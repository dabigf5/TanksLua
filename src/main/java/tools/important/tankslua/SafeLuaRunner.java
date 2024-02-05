package tools.important.tankslua;

import party.iroiro.luajava.Consts;
import party.iroiro.luajava.Lua;
import party.iroiro.luajava.value.LuaValue;

/**
 * A class purely with static members which is for running lua functions
 * if it's unknown whether the function will throw a lua error.
 */
public final class SafeLuaRunner {
    private SafeLuaRunner(){}

    /**
     * Automatically log errors to the console?
     */
    public static boolean autoLogErrors = true;

    /**
     * Calls the given lua function and properly handles any errors that happen with it.
     * @param userFunction The function to call
     * @param parameters The parameters to give that function
     * @return A record which describes if the operation succeeded or failed, its return values, or the amount of values it pushed onto the stack. The last two are mutually exclusive.
     */
    public static LuaResult safeCall(LuaValue userFunction, Object... parameters) {
        return safeCall(true, userFunction, parameters);
    }

    /**
     * Calls the given lua function and properly handles any errors that happen with it.
     * @param popReturns Do we want to pop the returns of the function off the stack and put them in the record, or leave them on the stack and put the amount of values added to the stack in the record?
     * @param userFunction The function to call
     * @param parameters The parameters to give that function
     * @return A UserCallResult record describing the result of the function call
     */
    public static LuaResult safeCall(boolean popReturns, LuaValue userFunction, Object... parameters) {
        Lua luaState = userFunction.state();

        int top = luaState.getTop();
        userFunction.push();
        for (Object o : parameters) {
            luaState.push(o, Lua.Conversion.SEMI);
        }

        Lua.LuaError result = luaState.pCall(parameters.length, Consts.LUA_MULTRET);

        if (result != Lua.LuaError.OK) {
            String error = luaState.toString(luaState.getTop());
            if (autoLogErrors) {
                System.out.println("Lua error: "+result+"; "+error);
            }
            luaState.pop(1);
            return new LuaResult(result, error);
        }


        int returnCount = luaState.getTop() - top;

        if (!popReturns) {
            return new LuaResult(Lua.LuaError.OK, returnCount);
        }

        LuaValue[] values = new LuaValue[returnCount];
        for (int i = 0; i < returnCount; i++) {
            values[returnCount - i - 1] = luaState.get();
        }
        return new LuaResult(Lua.LuaError.OK, values);
    }

    /**
     * A static class which describes the result of a Lua operation.
     */
    static class LuaResult {
        /**
         * The LuaError gotten by the call
         */
        public Lua.LuaError status;

        /**
         * The error message for if the call failed
         */
        public String errorMessage;

        /**
         * The returns of the call. Will be null in the event there are unpopped returns on the stack.
         */
        public LuaValue[] returns;
        /**
         * The amount of unpopped returns that are still on the stack. This will be 0 in the event that returns has anything in it.
         */
        public int unpoppedReturns;
        public LuaResult(Lua.LuaError status, LuaValue[] returns, int unpoppedReturns) {
            this.status = status;
            this.returns = returns;
            this.unpoppedReturns = unpoppedReturns;
        }
        public LuaResult(Lua.LuaError status, LuaValue[] returns) {this(status,returns,0);}
        public LuaResult(Lua.LuaError status, int unpoppedReturns) {this(status,null,unpoppedReturns);}
        public LuaResult(Lua.LuaError status, String errorMessage) {
            this(status, null, 0);
            this.errorMessage = errorMessage;
        }
    }

    /**
     * Loads a Lua file, calls it, and properly handles any loading errors that may occur.
     * This method uses a set of defaults to fill in the missing other parameters.
     *
     * @param luaState The lua state to use to load the file.
     * @param filePathToLoad The path to the lua file you wish to load.
     * @return The function loaded from the lua file, or null if the load failed
     */
    public static LuaResult safeLoadFile(Lua luaState, String filePathToLoad) {
        return safeLoadFile(luaState, filePathToLoad, null);
    }

    /**
     * Loads a Lua file, calls it, and properly handles any syntax errors that may occur.
     * @param luaState The lua state to load the lua file with.
     * @param filePathToLoad The path to the lua file you wish to load.
     * @param tEnv The environment to load the file in. Can be null.
     * @return The function loaded from the lua file, or null if the load failed
     */
    public static LuaResult safeLoadFile(Lua luaState, String filePathToLoad, LuaValue tEnv) {
        return safeCallLoadFunc(luaState, "loadfile", filePathToLoad, tEnv);
    }

    /**
     * Loads a string and properly handles any syntax errors that may occur.
     * @param luaState The lua state to load the string with
     * @param stringToLoad The string you wish to load
     * @return That string loaded as a function, or null if the load failed
     */
    public static LuaResult safeLoadString(Lua luaState, String stringToLoad) {
        return safeCallLoadFunc(luaState, "load", stringToLoad, null);
    }

    /**
     * A private utility function that will simply call a function with one argument, and in the given lua state.
     * @param luaState The lua state to call the function with.
     * @param funcName The name of the function
     * @param argument The one argument to pass to the function
     * @param tEnv The environment to call the function inside.
     * @return Either the function, or null if it failed.
     */
    private static LuaResult safeCallLoadFunc(Lua luaState, String funcName, String argument, LuaValue tEnv) {
        LuaValue[] loadResult;

        if (tEnv != null) {
            loadResult = luaState.get(funcName).call(argument, null, tEnv);
        } else {
            loadResult = luaState.get(funcName).call(argument, null);
        }

        LuaValue fLoadedString = loadResult[0];
        if (fLoadedString.type() == Lua.LuaType.NIL) {
            String errorMessage = (String) loadResult[1].toJavaObject();
            if (autoLogErrors) System.out.println("Lua: Error loading! "+errorMessage);
            return new LuaResult(Lua.LuaError.SYNTAX, errorMessage); // i'll just assume it's a syntax error
        }
        return new LuaResult(Lua.LuaError.OK, loadResult);
    }
}
