package tools.important.tankslua.luapackage;

import org.apache.commons.io.FilenameUtils;
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

    private PackSource getPackSource(File packFile) {
        if (packFile.isDirectory()) {
            return new DirectoryPackSource(packFile);
        }

        String fileExtension = FilenameUtils.getExtension(packFile.getName());

        if (fileExtension.equals("zip"))
            return new ZipFilePackSource(packFile);


        throw new IllegalArgumentException("Attempt to construct LuaPackage with unsupported filetype");
    }


    public LuaPackage(File packFile) {
        packSource = getPackSource(packFile);

        luaState.openLibraries();
        TanksLua.initializeState(luaState);

        luaState.push((state) -> {
            int args = state.getTop();
            if (args != 1) throw new LuaException("Incorrect amount of arguments for readFile");
            Object arg = state.get().toJavaObject();
            if (!(arg instanceof String)) {
                throw new LuaException("readFile expects a string");
            }
            String path = (String) arg;

            String content = packSource.readPlaintextFile(path);

            if (content != null) {
                state.push(content);
            } else {
                state.pushNil();
            }

            return 1;
        });
        luaState.setGlobal("readFile");
    }
    protected LuaPackage() { // for the purpose of decoys
        packSource = new DecoyPackSource();
    }

    protected LuaValue getAndVerifyTableFrom(String code, String chunkName, HashMap<String, EntryType> types) {
        SafeLuaRunner.LuaResult loadResult = TanksLua.tanksLua.runner.safeLoadString(luaState, code, chunkName);

        if (loadResult.status != Lua.LuaError.OK) {
            throw new LuaException("An error occurred while loading the file!");
        }

        SafeLuaRunner.LuaResult runResult = TanksLua.tanksLua.runner.safeCall(loadResult.returns[0]);

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
