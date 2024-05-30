package tools.important.tankslua.luapackage;

import party.iroiro.luajava.Lua;
import party.iroiro.luajava.value.LuaValue;
import tools.important.javalkv.LKVParseException;
import tools.important.tankslua.SafeLuaRunner;
import tools.important.tankslua.TanksLua;
import tools.important.tankslua.luapackage.verification.EntryType;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;

public class LevelPack extends LuaPackage {
    public LevelPack(File levelPackFile) throws LKVParseException, FileNotFoundException {
        super(levelPackFile);

        loadCallbacksFrom(packSource.readPlaintextFile("level.lua"));

        LuaValue fOnLoad = callbacks.get("onLoad");
        if (fOnLoad.type() == Lua.LuaType.NIL) return;

        SafeLuaRunner.safeCall(fOnLoad);
    }
    public static LevelPack fromLevelName(String levelName) {
        if (levelName == null) return null;

        File[] matches = new File(TanksLua.FULL_SCRIPT_PATH + "/level/").listFiles(
                (dir, name) -> name.startsWith(levelName.toLowerCase())
        );

        if (matches == null) return null;
        if (matches.length != 1) return null;

        File levelPackFile = matches[0];

        try {
            return new LevelPack(levelPackFile);
        } catch (FileNotFoundException e) {
            return null;
        }
    }


    public HashMap<String, LuaValue> callbacks = new HashMap<>();
    private static final HashMap<String, EntryType> CALLBACK_TYPES = new HashMap<>();static {
        CALLBACK_TYPES.put("onLoad", new EntryType(Lua.LuaType.FUNCTION, true));
        CALLBACK_TYPES.put("onDraw", new EntryType(Lua.LuaType.FUNCTION, true));
        CALLBACK_TYPES.put("onUpdate", new EntryType(Lua.LuaType.FUNCTION, true));
    }
    private void loadCallbacksFrom(String code) {
        LuaValue table = getAndVerifyTableFrom(code, CALLBACK_TYPES);

        for (String callbackName : CALLBACK_TYPES.keySet()) {
            callbacks.put(callbackName, table.get(callbackName));
        }
    }
}
