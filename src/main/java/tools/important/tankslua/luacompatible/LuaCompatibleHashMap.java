package tools.important.tankslua.luacompatible;

import party.iroiro.luajava.Lua;
import party.iroiro.luajava.value.LuaValue;

import java.util.HashMap;
import java.util.Map;

public class LuaCompatibleHashMap<K, V> extends HashMap<K, V> implements LuaCompatible {
    @Override
     public LuaValue getLuaTable(Lua luaState) {
         toLuaTable(luaState);
         return luaState.get();
    }

    @Override
    public void clearAndCopyLuaTable(LuaValue tTableToCopy) {
        clear();

        HashMap<Object, Object> tableHashMap = (HashMap<Object, Object>) tTableToCopy.toJavaObject();

        assert tableHashMap != null;
        for (Map.Entry<Object, Object> entry: tableHashMap.entrySet()) {
            put((K) entry.getKey(), (V) entry.getValue());
        }
    }

    @Override
    public void toLuaTable(Lua luaState) {
        luaState.createTable(0, 0);
        int tStackIndex = luaState.getTop();

        for (Entry<K, V> entry: entrySet()) {
            luaState.push(entry.getKey(), Lua.Conversion.SEMI);
            luaState.push(entry.getValue(), Lua.Conversion.SEMI);
            luaState.setTable(tStackIndex);
        }
    }

    @Override
    public String getTableLiteral() {
        StringBuilder tableBuilder = new StringBuilder("{");

        for (Entry<K, V> entry: entrySet()) {
            K key = entry.getKey();
            tableBuilder.append("[");
            if (key instanceof String) tableBuilder.append("\"");
            tableBuilder.append(key.toString());
            if (key instanceof String) tableBuilder.append("\"");
            tableBuilder.append("]=");

            V value = entry.getValue();
            if (value instanceof String) tableBuilder.append("\"");
            tableBuilder.append(value.toString());
            if (value instanceof String) tableBuilder.append("\"");
            tableBuilder.append(",");
        }
        tableBuilder.append("}");
        return tableBuilder.toString();
    }
}
