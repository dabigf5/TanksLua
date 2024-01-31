package tools.important.tankslua;

import party.iroiro.luajava.Lua;
import party.iroiro.luajava.LuaException;
import party.iroiro.luajava.value.LuaValue;

import java.io.File;
import java.util.HashMap;

public final class LuaExtension {
    public String name;
    public String authorName;


    public String fileName;

    public LuaValue fOnLoad;

    public LuaExtension(LuaValue tExtension, String fileName) {
        SafeLuaRunner.VerificationResult result = SafeLuaRunner.verifyTable(tExtension, EXTENSION_TABLE_TYPES);

        if (!result.verified()) {
            throw new LuaException("extension "+fileName+" failed to verify: "+result.message());
        }

        this.name = (String) tExtension.get("name").toJavaObject();
        this.authorName = (String) tExtension.get("authorName").toJavaObject();
        this.fileName = fileName;

        this.fOnLoad = tExtension.get("onLoad");

        System.out.println("successfully loaded extension \""+name+"\" by "+authorName+" ["+fileName+"]");
    }



    public static void registerExtensionsFromDir() {
        LuaValue fLoadFile = SafeLuaRunner.defaultState.get("loadfile");

        File extensionsDirectory = new File(TanksLua.fullScriptPath+"/extensions");

        File[] extensionLuaFiles = extensionsDirectory.listFiles();

        assert extensionLuaFiles != null;
        try {
            for (File file : extensionLuaFiles) {
                String fileName = file.getName();
                if (!fileName.endsWith(".lua")) {
                    continue;
                }

                LuaValue[] loadFileResult = fLoadFile.call(file.getAbsolutePath());

                if (loadFileResult.length == 2) {
                    throw new LuaException("error loading extension " + fileName + ": " + loadFileResult[1].toJavaObject());
                }

                LuaValue fLoadedFile = loadFileResult[0];
                SafeLuaRunner.UserCallResult result = SafeLuaRunner.safeCall(fLoadedFile);

                if (result.status() != Lua.LuaError.OK) {
                    throw new LuaException("error running extension " + fileName + ": " + loadFileResult[1]);
                }

                LuaValue[] returns = result.returns();
                if (returns.length != 1) {
                    throw new LuaException("extension " + fileName + " did not return exactly one value");
                }

                LuaExtension extension = new LuaExtension(returns[0], fileName);

                TanksLua.tanksLua.loadedLuaExtensions.add(extension);
            }
        } catch (LuaException exception) {
            System.out.println(exception.getMessage());
        }
    }

    private static final HashMap<String, SafeLuaRunner.TableType> EXTENSION_TABLE_TYPES = new HashMap<>();
    static {
        EXTENSION_TABLE_TYPES.put("name",           new SafeLuaRunner.TableType(Lua.LuaType.STRING,      false));
        EXTENSION_TABLE_TYPES.put("authorName",     new SafeLuaRunner.TableType(Lua.LuaType.STRING,      false));

        EXTENSION_TABLE_TYPES.put("onLoad",         new SafeLuaRunner.TableType(Lua.LuaType.FUNCTION,    false));
    }
}
