package tools.important.tankslua.lualib;

import party.iroiro.luajava.Lua;
import party.iroiro.luajava.LuaException;

public class JavaLibExtras implements LuaLib {
    @Override
    public void open(Lua luaState) {
        luaState.getGlobal("java");
        int javaLibStackIndex = luaState.getTop();

        luaState.push("instanceOf");
        luaState.push(
                ignored -> {
                    int argnum = luaState.getTop();

                    if (argnum != 2) throw new LuaException("incorrect number of arguments for instanceOf");

                    Object theUnverifiedClassInQuestion = luaState.toJavaObject(2);
                    Object object = luaState.toJavaObject(1);

                    if (object == null) throw new LuaException("object supplied to instanceOf is not a __jobject__");

                    if (theUnverifiedClassInQuestion == null) throw new LuaException("class supplied to instanceOf is not a __jclass__");
                    if (!(theUnverifiedClassInQuestion instanceof Class<?>)) throw new LuaException("class supplied to instanceOf is not a __jclass__");

                    Class<?> theClassInQuestion = (Class<?>) theUnverifiedClassInQuestion; // screw 'clazz', this is better

                    luaState.push(theClassInQuestion.isInstance(object));

                    return 1;
                }
        );
        luaState.setTable(javaLibStackIndex);
    }
}
