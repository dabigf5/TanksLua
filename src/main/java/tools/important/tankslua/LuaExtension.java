package tools.important.tankslua;

import party.iroiro.luajava.Lua;
import party.iroiro.luajava.LuaException;
import party.iroiro.luajava.value.LuaValue;

import java.io.File;
import java.util.HashMap;

public final class LuaExtension extends LuaScript {
    public String name;
    public String authorName;
    public String description;

    public int versionMajor;
    public int versionMinor;
    public int versionPatch;


    public String fileName;
    @LuaNillable
    public LuaValue fOnLoad;
    @LuaNillable
    public LuaValue fOnUpdate;
    @LuaNillable
    public LuaValue fOnDraw;
    public boolean enabled;

    @Override
    public void onVerificationError(String fileName, String problem) {
        new Notification(Notification.NotificationType.WARN, 5, "Extension " + fileName + " failed to verify! See log for details");
        System.out.println("Extension " + fileName + " failed to verify: " + problem);
    }

    public LuaExtension(LuaValue tExtension, String fileName) {
        super(fileName, tExtension, EXTENSION_TABLE_TYPES);

        this.name = (String) tExtension.get("name").toJavaObject();
        this.authorName = (String) tExtension.get("authorName").toJavaObject();
        this.fileName = fileName;

        LuaValue desc = tExtension.get("description");
        if (desc.type() != Lua.LuaType.NIL) {
            this.description = (String) desc.toJavaObject();
        } else {
            this.description = "<no description>";
        }

        try {
            //noinspection DataFlowIssue
            this.versionMajor = verifyAndConvertVersionNumber((double) tExtension.get("versionMajor").toJavaObject());
            //noinspection DataFlowIssue
            this.versionMinor = verifyAndConvertVersionNumber((double) tExtension.get("versionMinor").toJavaObject());
            //noinspection DataFlowIssue
            this.versionPatch = verifyAndConvertVersionNumber((double) tExtension.get("versionPatch").toJavaObject());
        } catch (LuaException e) {
            throw new LuaException("extension " + fileName + " failed to verify: " + e.getMessage());
        }
        this.fOnLoad = tExtension.get("onLoad");
        this.fOnUpdate = tExtension.get("onUpdate");
        this.fOnDraw = tExtension.get("onDraw");

        System.out.println("successfully loaded extension \"" + name + "\" by " + authorName + " [" + fileName + "]");
    }

    public String getVersionString() {
        return versionMajor + "." + versionMinor + "." + versionPatch;
    }

    public String getFullPath() {
        return TanksLua.fullScriptPath + "/extensions/" + fileName;
    }

    private static int verifyAndConvertVersionNumber(double versionNumber) {
        if (versionNumber % 1 != 0) {
            throw new LuaException("non-integer version number provided");
        }
        if (versionNumber < 0) {
            throw new LuaException("negative version number provided");
        }

        return (int) versionNumber;
    }

    @SuppressWarnings("unused")
    private LuaExtension(String name, String authorName, String fileName) { // for testing purposes only
        super();
        this.name = name;
        this.authorName = authorName;
        this.fileName = fileName;
        this.description = "nil";

        this.versionMajor = 0;
        this.versionMinor = 1;
        this.versionPatch = 0;
    }


    @SuppressWarnings({"CommentedOutCode", "RedundantSuppression"})
    public static void registerExtensionsFromDir() {
        // only uncomment this when you must test having an absurd amount of extensions
        /*for (int i = 0; i < 40; i++) {
            TanksLua.tanksLua.loadedLuaExtensions.add(
                new LuaExtension("testing extension", "testingAuthor", "fake.lua")
            );
        }
        if (true) return;*/
        LuaValue fLoadFile = SafeLuaRunner.defaultState.get("loadfile");

        File extensionsDirectory = new File(TanksLua.fullScriptPath + "/extensions/");
        File[] extensionLuaFiles = extensionsDirectory.listFiles();

        assert extensionLuaFiles != null;
        for (File file : extensionLuaFiles) {
            try {
                String fileName = file.getName();

                if (!fileName.endsWith(".lua")) {
                    System.out.println("cont1");
                    continue;
                }

                LuaValue[] loadFileResult = fLoadFile.call(file.getAbsolutePath());

                if (loadFileResult.length == 2) {
                    new Notification(Notification.NotificationType.WARN, 5, "Error loading extension " + fileName + "! See log for details");
                    throw new LuaException("error loading extension " + fileName + ": " + loadFileResult[1].toJavaObject());
                }

                LuaValue fLoadedFile = loadFileResult[0];
                SafeLuaRunner.UserCallResult result = SafeLuaRunner.safeCall(fLoadedFile);

                if (result.status() != Lua.LuaError.OK) {
                    new Notification(Notification.NotificationType.WARN, 5, "Error running extension " + fileName + "! See logs for more info");
                    throw new LuaException("error running extension " + fileName + ": " + loadFileResult[0]);
                }

                LuaValue[] returns = result.returns();
                if (returns.length != 1) {
                    new Notification(Notification.NotificationType.WARN, 5, "Extension " + fileName + " did not return exactly one value!");
                    throw new LuaException("extension " + fileName + " did not return exactly one value");
                }

                LuaExtension extension = new LuaExtension(returns[0], fileName);

                TanksLua.tanksLua.loadedLuaExtensions.add(extension);

                LuaValue fOnLoad = extension.fOnLoad;
                if (fOnLoad.type() == Lua.LuaType.NIL) continue;

                SafeLuaRunner.UserCallResult onLoadResult = SafeLuaRunner.safeCall(fOnLoad);

                if (onLoadResult.status() != Lua.LuaError.OK) {
                    System.out.println("extension " + extension.fileName + ": onLoad ran into an error ");
                }
            } catch (LuaException luaException) {
                System.out.println(luaException.getMessage());
            }
        }
    }

    private static final HashMap<String, LuaScript.TableType> EXTENSION_TABLE_TYPES = new HashMap<>();

    static {
        EXTENSION_TABLE_TYPES.put("name", new LuaScript.TableType(Lua.LuaType.STRING, false));
        EXTENSION_TABLE_TYPES.put("authorName", new LuaScript.TableType(Lua.LuaType.STRING, false));
        EXTENSION_TABLE_TYPES.put("description", new LuaScript.TableType(Lua.LuaType.STRING, true));

        EXTENSION_TABLE_TYPES.put("versionMajor", new LuaScript.TableType(Lua.LuaType.NUMBER, false));
        EXTENSION_TABLE_TYPES.put("versionMinor", new LuaScript.TableType(Lua.LuaType.NUMBER, false));
        EXTENSION_TABLE_TYPES.put("versionPatch", new LuaScript.TableType(Lua.LuaType.NUMBER, false));

        EXTENSION_TABLE_TYPES.put("onLoad", new LuaScript.TableType(Lua.LuaType.FUNCTION, true));
        EXTENSION_TABLE_TYPES.put("onUpdate", new LuaScript.TableType(Lua.LuaType.FUNCTION, true));
        EXTENSION_TABLE_TYPES.put("onDraw", new LuaScript.TableType(Lua.LuaType.FUNCTION, true));
    }
}
