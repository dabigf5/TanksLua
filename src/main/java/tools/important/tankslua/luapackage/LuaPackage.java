package tools.important.tankslua.luapackage;

import party.iroiro.luajava.Lua;
import party.iroiro.luajava.LuaException;
import party.iroiro.luajava.lua54.Lua54;
import party.iroiro.luajava.value.LuaValue;
import tools.important.tankslua.SafeLuaRunner;
import tools.important.tankslua.TanksLua;
import tools.important.tankslua.luapackage.packsource.DecoyPackSource;
import tools.important.tankslua.luapackage.packsource.DirectoryPackSource;
import tools.important.tankslua.luapackage.packsource.PackSource;
import tools.important.tankslua.luapackage.packsource.ZipFilePackSource;
import tools.important.tankslua.luapackage.verification.EntryType;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public abstract class LuaPackage {
    protected final PackSource packSource;
    protected final Lua luaState = new Lua54();
    public LuaPackage(File packFile) {
        luaState.openLibraries();
        TanksLua.openCustomLibs(luaState);

        if (packFile.isDirectory()) {
            this.packSource = new DirectoryPackSource(packFile);
            return;
        }

        if (packFile.isFile()) {
            this.packSource = new ZipFilePackSource(packFile);
            return;
        }

        throw new IllegalArgumentException("Unable to recognize packFile as a valid package!");
    }
    protected LuaPackage() { // for the purpose of decoys
        packSource = new DecoyPackSource();
    }

    protected LuaValue getAndVerifyTableFrom(String code, String chunkName, HashMap<String, EntryType> types) {
        SafeLuaRunner.LuaResult loadResult = SafeLuaRunner.safeLoadString(luaState, code, chunkName);

        if (loadResult.status != Lua.LuaError.OK) {
            throw new LuaException("An error occurred while loading the file!");
        }

        SafeLuaRunner.LuaResult runResult = SafeLuaRunner.safeCall(loadResult.returns[0]);

        if (runResult.status != Lua.LuaError.OK) {
            throw new LuaException("An error occurred while running the file!");
        }

        if (runResult.returns.length != 1) {
            throw new LuaException("The file did not return exactly one value!");
        }

        return verifyTable(types, runResult);
    }

    private static LuaValue verifyTable(HashMap<String, EntryType> types, SafeLuaRunner.LuaResult runResult) {
        LuaValue table = runResult.returns[0];

        if (table.type() != Lua.LuaType.TABLE) {
            throw new LuaException("The file did not return a table!");
        }

        for (Map.Entry<String, EntryType> type : types.entrySet()) {
            String entryName = type.getKey();
            EntryType entryType = type.getValue();

            LuaValue tableValue = table.get(entryName);
            Lua.LuaType tableValueType = tableValue.type();
            if (tableValueType == Lua.LuaType.NIL) {
                if (entryType.optional) continue;
                throw new LuaException("The file's table has a missing required key ("+entryName+")!");
            }

            if (tableValueType != entryType.type) throw new LuaException("The file's table has a key of wrong type! ("+entryName+" expects "+entryType.type+" but got "+entryType+")");
        }
        return table;
    }
}
