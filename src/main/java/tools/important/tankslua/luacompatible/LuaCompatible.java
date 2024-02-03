package tools.important.tankslua.luacompatible;

import party.iroiro.luajava.Lua;
import party.iroiro.luajava.value.LuaValue;

public interface LuaCompatible {
    /**
     * Creates a lua table, copies all of the lua-compatible object's values to it, and then pushes that table onto the lua stack.
     * @param luaState The lua state to use for creating the table.
     */
    void toLuaTable(Lua luaState);

    /**
     * Call toLuaTable, but pop the value from the stack and return it for convenience.
     *
     * @param luaState The lua state to use for creating the table.
     * @return The lua table made from this lua-compatible object.
     */
    LuaValue getLuaTable(Lua luaState);

    /**
     * Clear the lua-compatible object and copy everything in the given lua table to it.
     *
     * @param tTableToCopy The table you wish to copy
     */
    void clearAndCopyLuaTable(LuaValue tTableToCopy);

    /**
     * Convert the lua-compatible object to a valid table literal in Lua.
     * @return This lua-compatible object represented as a table literal
     */
    String getTableLiteral();
}
