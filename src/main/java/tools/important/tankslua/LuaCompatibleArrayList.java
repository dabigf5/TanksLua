package tools.important.tankslua;

import party.iroiro.luajava.Lua;
import party.iroiro.luajava.value.LuaValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LuaCompatibleArrayList<E> extends ArrayList<E> implements LuaCompatible {
    public void toLuaTable(Lua luaState) {
        int size = size();
        luaState.createTable(size,0);
        int tableIndex = luaState.getTop();

        for (int i = 0; i < size; i++) {
            E value = get(i);
            luaState.push(i);
            luaState.push(value, Lua.Conversion.SEMI);
            luaState.setTable(tableIndex);
        }
    }

    public LuaValue getLuaTable(Lua luaState) {
        toLuaTable(luaState);
        return luaState.get();
    }

    public void clearAndCopyLuaTable(LuaValue tTableToCopy) {
        clear();

        HashMap<Object, Object> tableHashMap = (HashMap<Object, Object>) tTableToCopy.toJavaObject();

        assert tableHashMap != null;
        for (Map.Entry<Object, Object> entry: tableHashMap.entrySet()) {
            set((int) entry.getKey(), (E) entry.getValue());
        }
    }
}
