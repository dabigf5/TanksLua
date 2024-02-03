package tools.important.tankslua;

import party.iroiro.luajava.Lua;
import party.iroiro.luajava.LuaException;
import party.iroiro.luajava.value.LuaValue;
import tools.important.tankslua.luacompatible.LuaCompatibleHashMap;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class LuaExtension extends LuaScript {
    public String name;
    public String authorName;
    public String description;

    public int versionMajor;
    public int versionMinor;
    public int versionPatch;
    @LuaNillable
    public LuaValue fOnLoad;
    @LuaNillable
    public LuaValue fOnUpdate;
    @LuaNillable
    public LuaValue fOnDraw;
    @LuaNillable
    public LuaValue fOnNewOptions;

    public LuaCompatibleHashMap<String, Object> options;
    public final HashMap<String, TableType> optionTypes = new HashMap<>();
    public final HashMap<String, Object> defaultOptionValues = new HashMap<>();

    @Override
    public void onVerificationError(String fileName, String problem) {
        new Notification(Notification.NotificationType.WARN, 5, "Extension " + fileName + " failed to verify! See log for details");
        throw new LuaException("Extension " + fileName + " failed to verify: " + problem);
    }

    public LuaExtension(LuaValue tExtension, String fileName) {
        super(fileName);
        quickVerify(tExtension, EXTENSION_TABLE_TYPES);

        name = (String) tExtension.get("name").toJavaObject();
        authorName = (String) tExtension.get("authorName").toJavaObject();

        LuaValue desc = tExtension.get("description");
        if (desc.type() != Lua.LuaType.NIL) {
            this.description = (String) desc.toJavaObject();
        } else {
            this.description = "<no description>";
        }

        try {
            //noinspection DataFlowIssue
            versionMajor = verifyAndConvertVersionNumber((double) tExtension.get("versionMajor").toJavaObject());
            //noinspection DataFlowIssue
            versionMinor = verifyAndConvertVersionNumber((double) tExtension.get("versionMinor").toJavaObject());
            //noinspection DataFlowIssue
            versionPatch = verifyAndConvertVersionNumber((double) tExtension.get("versionPatch").toJavaObject());
        } catch (LuaException e) {
            throw new LuaException("extension " + fileName + " failed to verify: " + e.getMessage());
        }
        fOnLoad = tExtension.get("onLoad");
        fOnUpdate = tExtension.get("onUpdate");
        fOnDraw = tExtension.get("onDraw");
        fOnNewOptions = tExtension.get("onNewOptions");

        LuaValue tOptionTypes = tExtension.get("options");
        if (tOptionTypes.type() == Lua.LuaType.NIL) {
            return;
        }

        HashMap<Object, Object> optionTypesConverted = (HashMap<Object, Object>) tOptionTypes.toJavaObject();
        assert optionTypesConverted != null;

        for (Map.Entry<Object, Object> pair: optionTypesConverted.entrySet()) {
            Object key = pair.getKey();
            if (!(key instanceof String)) {
                onVerificationError(fileName,"non-string key in options type table");
                return;
            }
            Object value = pair.getValue();
            if (!(value instanceof HashMap)) {
                onVerificationError(fileName,"non-table value in options type table");
                return;
            }

            String optionName = (String) key;
            HashMap<Object, Object> optionTable = (HashMap<Object, Object>) value;
            Object sTypeName = optionTable.get("type");
            if (!(sTypeName instanceof String)) {
                onVerificationError(fileName,"non-string value used as type name in option table");
                return;
            }

            String optionType = (String) sTypeName;
            TableType tableType;
            try {
                tableType = TableType.fromString(optionType);
            } catch (IllegalArgumentException ignored) {
                onVerificationError(fileName, "non-lua type as value in options type table");
                return;
            }

            optionTypes.put(optionName, tableType);

            Lua.LuaType defaultValueType = tOptionTypes.get(optionName).get("default").type();

            if (defaultValueType == Lua.LuaType.NIL) {
                onVerificationError(fileName, "default value is missing from option "+optionName);
                return;
            }

            if (defaultValueType != tableType.type) {
                onVerificationError(fileName, "default value for "+optionName+" is of the wrong type");
                return;
            }

            Object defaultValue = optionTable.get("default");
            defaultOptionValues.put(optionName, defaultValue);
        }
        loadExtensionOptions();
        System.out.println("successfully loaded extension \"" + name + "\" by " + authorName + " [" + fileName + "]");
    }
    public void loadExtensionOptions() {
        LuaValue tExtensionsOptions = loadExtensionOptionsTable();
        LuaValue tOurOptions = tExtensionsOptions.get(this.fileName);

        options = new LuaCompatibleHashMap<>();
        if (tOurOptions.type() == Lua.LuaType.NIL) {
            assignDefaultsToOptions(); // silently assign the defaults because this can happen under normal circumstances
            return;
        }


        VerificationResult result = verifyTable(tOurOptions, optionTypes);
        if (!result.verified) {
            new Notification(Notification.NotificationType.WARN, 5, "Extension "+fileName+"'s options failed to verify! As a result, the defaults have been loaded instead.");
            assignDefaultsToOptions();
            return;
        }
        options.clearAndCopyLuaTable(tOurOptions);
    }
    private void assignDefaultsToOptions() {
        for (Map.Entry<String, Object> entry: defaultOptionValues.entrySet()) {
            String optionName = entry.getKey();
            Object defaultValue = entry.getValue();

            options.put(optionName, defaultValue);
        }
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
    public static void saveExtensionOptions() {
        StringBuilder optionsFileBuilder = new StringBuilder("return {");
        for (LuaExtension luaExtension: TanksLua.tanksLua.loadedLuaExtensions) {
            optionsFileBuilder.append("\n[\"").append(luaExtension.fileName).append("\"]=");
            optionsFileBuilder.append(luaExtension.options.getTableLiteral()).append(",");
        }
        optionsFileBuilder.append("}");
        String newOptionsFile = optionsFileBuilder.toString();
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(TanksLua.fullScriptPath + "/extensionOptions.lua"));
            writer.write(newOptionsFile);
            writer.close();
        } catch(IOException e) {
            e.printStackTrace();
            new Notification(Notification.NotificationType.WARN, 5, "An error occurred saving extension settings! See log for details");
        }

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
    private static LuaValue loadExtensionOptionsTable() {
        LuaValue loaded = SafeLuaRunner.safeLoadFile(TanksLua.fullScriptPath+"/extensionOptions.lua");

        if (loaded == null)
            throw new LuaException("extension options file failed to load");

        SafeLuaRunner.UserCallResult result = SafeLuaRunner.safeCall(loaded);

        if (result.status != Lua.LuaError.OK)
            throw new LuaException("extension options file failed to run");

        LuaValue[] returns = result.returns;

        if (returns.length != 1)
            throw new LuaException("extension options file did not return exactly one value");

        LuaValue table = result.returns[0];

        if (table.type() != Lua.LuaType.TABLE)
            throw new LuaException("extension options file did not return a table");

        return table;
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
                    continue;
                }

                LuaValue[] loadFileResult = fLoadFile.call(file.getAbsolutePath());

                if (loadFileResult.length == 2) {
                    new Notification(Notification.NotificationType.WARN, 5, "Error loading extension " + fileName + "! See log for details");
                    throw new LuaException("error loading extension " + fileName + ": " + loadFileResult[1].toJavaObject());
                }

                LuaValue fLoadedFile = loadFileResult[0];
                SafeLuaRunner.UserCallResult result = SafeLuaRunner.safeCall(fLoadedFile);

                if (result.status != Lua.LuaError.OK) {
                    new Notification(Notification.NotificationType.WARN, 5, "Error running extension " + fileName + "! See logs for more info");
                    throw new LuaException("error running extension " + fileName + ": " + loadFileResult[0]);
                }

                LuaValue[] returns = result.returns;
                if (returns.length != 1) {
                    new Notification(Notification.NotificationType.WARN, 5, "Extension " + fileName + " did not return exactly one value!");
                    throw new LuaException("extension " + fileName + " did not return exactly one value");
                }

                LuaExtension extension = new LuaExtension(returns[0], fileName);

                TanksLua.tanksLua.loadedLuaExtensions.add(extension);

                LuaValue fOnLoad = extension.fOnLoad;
                LuaValue fOnNewOptions = extension.fOnNewOptions;

                if (fOnLoad.type() != Lua.LuaType.NIL) {
                    SafeLuaRunner.UserCallResult onLoadResult = SafeLuaRunner.safeCall(fOnLoad);

                    if (onLoadResult.status != Lua.LuaError.OK) {
                        System.out.println("extension " + extension.fileName + ": onLoad ran into an error ");
                        new Notification(Notification.NotificationType.WARN, 5, "Extension "+extension.fileName+" ran into an issue in onLoad!");
                    }
                }
                if (fOnNewOptions.type() != Lua.LuaType.NIL) {
                    SafeLuaRunner.UserCallResult onLoadResult = SafeLuaRunner.safeCall(fOnNewOptions, extension.options.getLuaTable(SafeLuaRunner.defaultState));

                    if (onLoadResult.status != Lua.LuaError.OK) {
                        System.out.println("extension " + extension.fileName + ": onNewOptions ran into an error ");
                        new Notification(Notification.NotificationType.WARN, 5, "Extension "+extension.fileName+" ran into an issue processing options!");
                    }
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
        EXTENSION_TABLE_TYPES.put("onNewOptions", new TableType(Lua.LuaType.FUNCTION, true));

        EXTENSION_TABLE_TYPES.put("options", new LuaScript.TableType(Lua.LuaType.TABLE, true));
    }
}
