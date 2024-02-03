package tools.important.tankslua.luacompatible;

import party.iroiro.luajava.Lua;
import party.iroiro.luajava.value.LuaValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LuaCompatibleArrayList<E> extends ArrayList<E> implements LuaCompatible {
    @Override
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
            set((int) entry.getKey(), (E) entry.getValue());
        }
    }

    @Override
    public String getTableLiteral() {
        StringBuilder tableBuilder = new StringBuilder("{");

        for (E element: this) {
            if (element instanceof String) tableBuilder.append("\"");

            tableBuilder.append(element.toString());

            if (element instanceof String) tableBuilder.append("\"");

            tableBuilder.append(",");
        }

        tableBuilder.append("}");

        return tableBuilder.toString();
    }
}
