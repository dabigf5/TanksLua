package tools.important.tankslua;

import party.iroiro.luajava.Consts;
import party.iroiro.luajava.Lua;
import party.iroiro.luajava.value.LuaValue;

public final class SafeLuaRunner {

    /**
     * A class which describes the result of a Lua function call.<br>
     * This includes loading, as the function called is <code>load</code>.<br>
     */
    public static class LuaResult {
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
     * Calls the given lua function and properly handles any errors that happen with it.
     * @param userFunction The function to call
     * @param parameters The parameters to give that function
     * @return A record which describes if the operation succeeded or failed, its return values, or the amount of values it pushed onto the stack. The last two are mutually exclusive.
     */
    public LuaResult safeCall(LuaValue userFunction, Object... parameters) {
        return safeCall(true, userFunction, parameters);
    }

    /**
     * Calls the given lua function and properly handles any errors that happen with it.
     * @param popReturns Do we want to pop the returns of the function off the stack and put them in the record, or leave them on the stack and put the amount of values added to the stack in the record?
     * @param userFunction The function to call
     * @param parameters The parameters to give that function
     * @return A UserCallResult record describing the result of the function call
     */
    public LuaResult safeCall(boolean popReturns, LuaValue userFunction, Object... parameters) {
        Lua luaState = userFunction.state();

        int top = luaState.getTop();
        userFunction.push();
        for (Object param : parameters) {
            if (param instanceof LuaValue) {
                LuaValue paramLuaValue = (LuaValue) param;

                paramLuaValue.push();
                continue;
            }

            luaState.push(param, Lua.Conversion.SEMI);
        }

        Lua.LuaError result = luaState.pCall(parameters.length, Consts.LUA_MULTRET);

        if (result != Lua.LuaError.OK) {
            String error = luaState.toString(luaState.getTop());

            System.err.println("Lua error: "+result+"; "+error);
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
     * Loads a string and properly handles any syntax errors that may occur.
     * @param luaState The lua state to load the string with
     * @param code The string you wish to load
     * @return That string loaded as a function, or null if the load failed
     */
    public LuaResult safeLoadString(Lua luaState, String code, String chunkName) {
        LuaValue[] loadResult;

        loadResult = luaState.get("load").call(code, chunkName);

        LuaValue fLoadedString = loadResult[0];
        if (fLoadedString.type() == Lua.LuaType.NIL) {
            String errorMessage = (String) loadResult[1].toJavaObject();
            System.err.println("Lua: Load error: "+errorMessage);
            return new LuaResult(Lua.LuaError.SYNTAX, errorMessage); // i'll just assume it's a syntax error
        }
        return new LuaResult(Lua.LuaError.OK, loadResult);
    }
}
