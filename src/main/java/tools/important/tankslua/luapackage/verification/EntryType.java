package tools.important.tankslua.luapackage.verification;

import party.iroiro.luajava.Lua;

public class EntryType {
    public Lua.LuaType type;
    public boolean optional;

    public EntryType(Lua.LuaType type, boolean optional) {
        this.type = type;
        this.optional = optional;
    }
    @SuppressWarnings("unused")
    public static EntryType fromString(String typestr) {
        boolean optional = typestr.endsWith("?");
        String typeName = typestr.replace("?", "").toUpperCase();
        Lua.LuaType luaType = Lua.LuaType.valueOf(typeName);

        return new EntryType(luaType, optional);
    }
}
