package tools.important.tankslua.luapackage;

import party.iroiro.luajava.Lua;
import party.iroiro.luajava.LuaException;
import party.iroiro.luajava.value.LuaValue;
import tools.important.javalkv.*;
import tools.important.tankslua.Notification;
import tools.important.tankslua.SafeLuaRunner;
import tools.important.tankslua.TanksLua;
import tools.important.tankslua.luapackage.verification.EntryType;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class LuaExtension extends LuaPackage {
    private LuaExtension(File extensionFile)
            throws LKVParseException, ExtensionMetaParseException, ExtensionOptionParseException, FileNotFoundException {
        super(extensionFile);
        TanksLua.initializeStateSearchers(luaState, packSource);

        String extensionMeta = packSource.readPlaintextFile("extension-meta.lkv");
        if (extensionMeta == null) {
            throw new FileNotFoundException("Extension "+packSource.getPackName()+" is missing a metadata file!");
        }
        loadMeta(extensionMeta);

        loadOptionValues();

        if (!enabled) return;
        String code = packSource.readPlaintextFile("extension.lua");
        if (code == null) {
            new Notification(Notification.NotificationType.WARN, 5, "Extension "+packSource.getPackName()+" is missing its extension.lua file!");
            return;
        }

        loadCallbacks(code);

        LuaValue fOnLoad = callbacks.get("onLoad");
        if (fOnLoad != null && fOnLoad.type() != Lua.LuaType.NIL) {
            TanksLua.tanksLua.runner.safeCall(fOnLoad);
        }
        onNewOptions();
    }

    public String name;
    public String description;
    public String authorName;
    public SemanticVersion version;

    private void loadMeta(String content) {
        HashMap<String, LKVValue> pairs = LKV.parse(content);

        // maybe change this if there's a lot of metadata

        LKVValue lkvName = pairs.get("name");
        if (lkvName == null) throw new ExtensionMetaParseException("Missing required LKV metadata entry name");
        if (lkvName.type != LKVType.STRING)
            throw new ExtensionMetaParseException("LKV metadata entry name is of wrong type (expected string)");
        name = (String) lkvName.value;

        LKVValue lkvDescription = pairs.get("description");
        if (lkvDescription == null)
            throw new ExtensionMetaParseException("Missing required LKV metadata entry description");
        if (lkvDescription.type != LKVType.STRING)
            throw new ExtensionMetaParseException("LKV metadata entry description is of wrong type (expected string)");
        description = (String) lkvDescription.value;

        LKVValue lkvAuthor = pairs.get("authorName");
        if (lkvAuthor == null) throw new ExtensionMetaParseException("Missing required LKV metadata entry authorName");
        if (lkvAuthor.type != LKVType.STRING)
            throw new ExtensionMetaParseException("LKV metadata entry authorName is of wrong type (expected string)");

        authorName = (String) lkvAuthor.value;

        LKVValue lkvVersion = pairs.get("version");
        if (lkvVersion == null) throw new ExtensionMetaParseException("Missing required LKV metadata entry version");
        if (lkvVersion.type != LKVType.VERSION)
            throw new ExtensionMetaParseException("LKV metadata entry version is of wrong type (expected version)");
        version = (SemanticVersion) lkvVersion.value;

        loadOptionMeta(pairs);
    }


    public boolean enabled;


    public static class LuaExtensionOption {
        public String displayName;
        public final String name;
        public final LKVType type;
        private final Object defaultValue;
        public Object value;

        public LuaExtensionOption(String displayName, String name, LKVType type, Object defaultValue) {
            this.displayName = displayName;
            this.name = name;
            this.type = type;
            this.defaultValue = defaultValue;
        }
    }

    public static class ExtensionOptionParseException extends RuntimeException {
        private ExtensionOptionParseException(String message) {
            super(message);
        }
    }

    public static class ExtensionMetaParseException extends RuntimeException {
        private ExtensionMetaParseException(String message) {
            super(message);
        }
    }

    public final List<LuaExtensionOption> options = new ArrayList<>();

    public LuaValue getOptionsLuaTable(Lua luaState) {
        luaState.createTable(0, options.size());
        int extensionTableStackIndex = luaState.getTop();

        for (LuaExtensionOption option : options) {
            luaState.push(option.name);
            luaState.push(option.value, Lua.Conversion.SEMI);
            luaState.setTable(extensionTableStackIndex);
        }

        return luaState.get();
    }

    private static final String OPTION_PREFIX = "option_";
    private static final String OPTION_DEFAULT_PREFIX = "optiondefault_";
    private static final String OPTION_DISPLAYNAME_PREFIX = "optiondisplayname_";
    private void loadOptionMeta(HashMap<String, LKVValue> lkvPairs) {
        for (Map.Entry<String, LKVValue> entry : lkvPairs.entrySet()) {
            String key = entry.getKey();
            if (!key.startsWith(OPTION_PREFIX)) continue;

            String optionName = key.substring(OPTION_PREFIX.length());
            if (optionName.equals(ENABLED_KEY)) throw new ExtensionMetaParseException("Forbidden name 'ENABLED' used for option");

            LKVValue optionDefinition = entry.getValue();

            if (optionDefinition.type != LKVType.TYPE)
                throw new ExtensionMetaParseException("Option definition \"" + key + "\" is of a non-type type");

            LKVType expectedOptionType = (LKVType) optionDefinition.value;

            LKVValue defaultValueLkv = lkvPairs.get(OPTION_DEFAULT_PREFIX + optionName);

            if (defaultValueLkv == null)
                throw new ExtensionMetaParseException("Option default value for \"" + optionName + "\" is not present");
            if (defaultValueLkv.type != expectedOptionType)
                throw new ExtensionMetaParseException("Option default value for \"" + optionName + "\" is of the wrong type");



            LKVValue optionDisplayNameLkv = lkvPairs.get(OPTION_DISPLAYNAME_PREFIX + optionName);
            if (optionDisplayNameLkv == null)
                throw new ExtensionMetaParseException("Option display name for \"" + optionName + "\" is not present");
            if (optionDisplayNameLkv.type != LKVType.STRING)
                throw new ExtensionMetaParseException("Option display name for \"" + optionName + "\" is of the wrong type");

            String optionDisplayName = (String) optionDisplayNameLkv.value;

            for (LuaExtensionOption option : options) {
                if (option.displayName.equals(optionDisplayName))
                    throw new ExtensionMetaParseException("Option display name for option "+optionName+" is identical to option "+option.name+"'s");
            }

            LuaExtensionOption option = new LuaExtensionOption(
                    optionDisplayName,
                    optionName,
                    expectedOptionType,
                    defaultValueLkv.value
            );

            options.add(option);
        }
    }


    private static final String ENABLED_KEY = "ENABLED";
    private void loadOptionValues() {
        if (this.isDecoy) return;

        File optionsLkvFile = getOptionsLkvFile();
        if (!optionsLkvFile.exists()) {
            for (LuaExtensionOption option : options) {
                option.value = option.defaultValue;
            }
            return;
        }

        String optionsLkv = TanksLua.readContentsOfFile(optionsLkvFile);

        Map<String, LKVValue> pairs = LKV.parse(optionsLkv);

        LKVValue enabledValue = pairs.get(ENABLED_KEY);

        if (enabledValue == null) throw new ExtensionOptionParseException("Missing enabled value in options");
        if (enabledValue.type != LKVType.BOOLEAN)
            throw new ExtensionOptionParseException("enabled value in options has wrong type");

        this.enabled = (boolean) enabledValue.value;

        for (LuaExtensionOption option : options) {
            LKVValue value = pairs.get(option.name);

            if (value == null) {
                option.value = option.defaultValue;
                continue;
            }

            if (value.type != option.type)
                throw new ExtensionOptionParseException("Option " + option.name + " is of wrong type");

            option.value = value.value;
//            System.out.println(option.name + " = " + value.value);
        }
    }
    public void saveOptions() {
        if (this.isDecoy) return;

        File optionsLkvFile = getOptionsLkvFile();

        StringBuilder optionsFileBuilder = new StringBuilder("boolean ENABLED = ").append(enabled).append("\n\n");

        for (LuaExtensionOption option : options) {
            String optionName = option.name;
            Object optionValue = option.value;

            LKVType optionType = option.type;

            optionsFileBuilder.append(optionType.typeName).append(" ").append(optionName).append(" = ").append(optionValue.toString()).append("\n");
        }

        TanksLua.replaceContentsOfFile(optionsLkvFile, optionsFileBuilder.toString());
    }
    private File getOptionsLkvFile() {
        return new File(TanksLua.FULL_SCRIPT_PATH + "/extension-options/" + packSource.getPackName() + ".lkv");
    }
    public void onNewOptions() {
        LuaValue fOnNewOptions = callbacks.get("onNewOptions");
        if (fOnNewOptions != null && fOnNewOptions.type() != Lua.LuaType.NIL) {
            TanksLua.tanksLua.runner.safeCall(fOnNewOptions, getOptionsLuaTable(luaState));
        }
    }


    public Map<String, LuaValue> callbacks = new HashMap<>();
    private static final HashMap<String, EntryType> CALLBACK_TYPES = new HashMap<>();

    static {
        CALLBACK_TYPES.put("onLoad", new EntryType(Lua.LuaType.FUNCTION, true));
        CALLBACK_TYPES.put("onDraw", new EntryType(Lua.LuaType.FUNCTION, true));
        CALLBACK_TYPES.put("onUpdate", new EntryType(Lua.LuaType.FUNCTION, true));

        CALLBACK_TYPES.put("onLevelLoad", new EntryType(Lua.LuaType.FUNCTION, true));
        CALLBACK_TYPES.put("onNewOptions", new EntryType(Lua.LuaType.FUNCTION, true));
    }

    private void loadCallbacks(String code) {
        if (isDecoy) return;

        LuaValue table;
        try {
            table = getAndVerifyTableFrom(code, "extension:"+packSource.getPackName(), CALLBACK_TYPES);
        } catch (LuaException e) {
            new Notification(Notification.NotificationType.WARN, 5, "Extension "+packSource.getPackName()+" ran into a problem verifying: "+e.getMessage());
            return;
        }


        for (String callbackName : CALLBACK_TYPES.keySet()) {
            callbacks.put(callbackName, table.get(callbackName));
        }
    }

    public void loadCallbacksIfNone() {
        if (!callbacks.isEmpty()) return;

        String code = packSource.readPlaintextFile("extension.lua");

        if (code == null) {
            new Notification(Notification.NotificationType.WARN, 5, "Extension "+packSource.getPackName()+" is missing its extension.lua file!");
            return;
        }

        loadCallbacks(code);
    }

    public static void loadExtensionsTo(List<LuaExtension> extensionList) {
//        loadDecoys(extensionList,30);

        File extensionDirectory = new File(TanksLua.FULL_SCRIPT_PATH + "/extensions/");
        File[] extensionFiles = Objects.requireNonNull(extensionDirectory.listFiles());

        for (File file : extensionFiles) {
            try {
                extensionList.add(new LuaExtension(file));
            } catch (LKVParseException e) {
                new Notification(Notification.NotificationType.WARN, 5, "Extension " + file.getName() + " has invalidly formatted metadata! See log for details");
                e.printStackTrace();
            } catch (ExtensionMetaParseException e) {
                new Notification(Notification.NotificationType.WARN, 5, "Extension " + file.getName() + " has invalid metadata! See log for details");
                e.printStackTrace();
            } catch (ExtensionOptionParseException e) {
                new Notification(Notification.NotificationType.WARN, 5, "TanksLua ran into a problem parsing " + file.getName() + "'s options! See log for details");
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                new Notification(Notification.NotificationType.WARN, 5, e.getMessage());
                e.printStackTrace();
            }
        }
    }


    private LuaExtension() {
    }
    private boolean isDecoy;
    @SuppressWarnings({"unused", "SameParameterValue"})
    private static void loadDecoys(List<LuaExtension> extensionList, int amount) {
        Map<String, LuaValue> callbacks = new HashMap<>();

        for (int num = 1; num <= amount; num++) {
            LuaExtension decoy = new LuaExtension();
            decoy.name = "Fake Extension #" + num;
            decoy.authorName = "John Null";
            decoy.description = "not real";
            decoy.version = new SemanticVersion(1, 0, 0);
            decoy.enabled = false;
            decoy.callbacks = callbacks;
            decoy.isDecoy = true;

            extensionList.add(decoy);
        }
    }
}