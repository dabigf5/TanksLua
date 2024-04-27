package tools.important.tankslua.luapackage;

import party.iroiro.luajava.Lua;
import party.iroiro.luajava.LuaException;
import party.iroiro.luajava.lua54.Lua54;
import party.iroiro.luajava.value.LuaValue;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import tools.important.tankslua.SafeLuaRunner;
import tools.important.tankslua.TanksLua;
import tools.important.tankslua.luapackage.packsource.DirectoryPackSource;
import tools.important.tankslua.luapackage.packsource.PackSource;
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
            //todo: zip support
            throw new NotImplementedException();
        }

        throw new IllegalArgumentException("Unable to recognize packFile as a valid package!");
    }

    protected LuaValue getAndVerifyTableFrom(File file, HashMap<String, EntryType> types) {
        SafeLuaRunner.LuaResult loadFileResult = SafeLuaRunner.safeLoadFile(luaState, file);

        if (loadFileResult.status != Lua.LuaError.OK) {
            throw new LuaException("An error occurred while loading the file!");
        }

        SafeLuaRunner.LuaResult runResult = SafeLuaRunner.safeCall(loadFileResult.returns[0]);

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
