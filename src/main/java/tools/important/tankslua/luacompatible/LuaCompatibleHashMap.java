package tools.important.tankslua.luacompatible;

import party.iroiro.luajava.Lua;
import party.iroiro.luajava.value.LuaValue;

import java.util.HashMap;
import java.util.Map;

public class LuaCompatibleHashMap<K, V> extends HashMap<K, V> implements LuaCompatible {
     public LuaValue getLuaTable(Lua luaState) {
         toLuaTable(luaState);
         return luaState.get();
    }

    public void clearAndCopyLuaTable(LuaValue tTableToCopy) {
        clear();

        HashMap<Object, Object> tableHashMap = (HashMap<Object, Object>) tTableToCopy.toJavaObject();

        assert tableHashMap != null;
        for (Map.Entry<Object, Object> entry: tableHashMap.entrySet()) {
            put((K) entry.getKey(), (V) entry.getValue());
        }
    }

    public void toLuaTable(Lua luaState) {
        luaState.createTable(0, 0);
        int tStackIndex = luaState.getTop();

        for (Entry<K, V> entry: entrySet()) {
            luaState.push(entry.getKey(), Lua.Conversion.SEMI);
            luaState.push(entry.getValue(), Lua.Conversion.SEMI);
            luaState.setTable(tStackIndex);
        }
    }
}
