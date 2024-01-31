package tools.important.tankslua;

import party.iroiro.luajava.value.LuaValue;

public record LuaExtension(String fileName, String name, String authorName, LuaValue table) {
/*    public String getFilePath() {
        return TanksLua.fullScriptPath+"/"+fileName+".lua";
    }*/
}
