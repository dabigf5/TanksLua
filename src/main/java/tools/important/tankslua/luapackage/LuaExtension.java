package tools.important.tankslua.luapackage;

import party.iroiro.luajava.Lua;
import party.iroiro.luajava.value.LuaValue;
import tools.important.javalkv.*;
import tools.important.tankslua.Notification;
import tools.important.tankslua.SafeLuaRunner;
import tools.important.tankslua.TanksLua;
import tools.important.tankslua.luacompatible.LuaCompatibleHashMap;
import tools.important.tankslua.luapackage.verification.EntryType;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class LuaExtension extends LuaPackage {
    public String name;
    public String description;
    public String authorName;
    public SemanticVersion version;

    private void loadMeta(File extensionMetaFile) {
        String content = readContentsOfFile(extensionMetaFile);

        HashMap<String, LKVValue> pairs = LKV.parse(content);


        // maybe change this if there's a lot of metadata

        LKVValue lkvName = pairs.get("name");
        if (lkvName == null) throw new LKVParseException("Missing required LKV metadata entry name");
        if (lkvName.type != LKVType.STRING)
            throw new LKVParseException("LKV metadata entry name is of wrong type (expected string)");
        name = (String) lkvName.value;

        LKVValue lkvDescription = pairs.get("description");
        if (lkvDescription == null) throw new LKVParseException("Missing required LKV metadata entry description");
        if (lkvDescription.type != LKVType.STRING)
            throw new LKVParseException("LKV metadata entry description is of wrong type (expected string)");
        description = (String) lkvDescription.value;

        LKVValue lkvAuthor = pairs.get("authorName");
        if (lkvAuthor == null) throw new LKVParseException("Missing required LKV metadata entry authorName");
        if (lkvAuthor.type != LKVType.STRING)
            throw new LKVParseException("LKV metadata entry authorName is of wrong type (expected string)");
        authorName = (String) lkvAuthor.value;

        LKVValue lkvVersion = pairs.get("version");
        if (lkvVersion == null) throw new LKVParseException("Missing required LKV metadata entry version");
        if (lkvVersion.type != LKVType.VERSION)
            throw new LKVParseException("LKV metadata entry version is of wrong type (expected version)");
        version = (SemanticVersion) lkvVersion.value;

        loadOptionTypesAndDefaults(pairs);
    }


    public boolean enabled;


    public LuaCompatibleHashMap<String, Object> optionValues = new LuaCompatibleHashMap<>();


    public Map<String, LKVType> optionTypes = new LuaCompatibleHashMap<>();
    private final Map<String, Object> optionDefaults = new HashMap<>();

    private static final String OPTION_PREFIX = "option_";
    private static final String OPTION_DEFAULT_PREFIX = "optiondefault_";

    private void loadOptionTypesAndDefaults(HashMap<String, LKVValue> lkvPairs) {
        for (Map.Entry<String, LKVValue> entry : lkvPairs.entrySet()) {
            String key = entry.getKey();

            if (!key.startsWith(OPTION_PREFIX)) continue;

            LKVValue value = entry.getValue();
            if (value.type != LKVType.TYPE) throw new LKVParseException("Option definition \""+key+"\" is of a non-type type");

            String optionName = key.substring(OPTION_PREFIX.length());
            optionTypes.put(optionName, (LKVType) value.value);
        }

        for (Map.Entry<String, LKVValue> entry : lkvPairs.entrySet()) {
            String key = entry.getKey();

            if (!key.startsWith(OPTION_DEFAULT_PREFIX)) continue;

            LKVValue value = entry.getValue();

            String optionName = key.substring(OPTION_DEFAULT_PREFIX.length());

            if (value.type != optionTypes.get(optionName)) throw new LKVParseException("Option default for option \""+key+"\" is of the wrong type");

            optionDefaults.put(optionName, value.value);
        }

        for (String optionName : optionTypes.keySet()) {
            if (optionDefaults.get(optionName) == null) throw new LKVParseException("Option default for option \""+optionName+"\" is missing!");
        }
    }


    private static final String ENABLED_KEY = "ENABLED";
    private void loadOptions() {
        File optionsLkvFile = getOptionsLkvFile();
        if (!optionsLkvFile.exists()) return;

        String optionsLkv = readContentsOfFile(optionsLkvFile);

        Map<String, LKVValue> pairs = LKV.parse(optionsLkv);

        LKVValue enabledValue = pairs.get(ENABLED_KEY);
        if (enabledValue == null) throw new LKVParseException("Missing enabled value in options");
        if (enabledValue.type != LKVType.BOOLEAN)
            throw new LKVParseException("enabled value in options has wrong type");

        this.enabled = (boolean) enabledValue.value;

        pairs.remove(ENABLED_KEY); // so that the for loop below doesn't complain about invalid option

        for (Map.Entry<String, LKVValue> pair : pairs.entrySet()) {
            String key = pair.getKey();
            LKVValue value = pair.getValue();

            LKVType expectedType = optionTypes.get(key);
            if (expectedType == null) throw new LKVParseException("Option " + key + " is not a valid option");
            if (value.type != expectedType) throw new LKVParseException("Option " + key + " is of wrong type");

            optionValues.put(key, value.value);
//            System.out.println(key + ", " + value.value);
        }

        for (Map.Entry<String, Object> pair : optionDefaults.entrySet()) {
            String key = pair.getKey();
            Object defaultValue = pair.getValue();

            optionValues.putIfAbsent(key, defaultValue);
        }
    }
    public void saveOptions() {
        File optionsLkvFile = getOptionsLkvFile();

        StringBuilder optionsFileBuilder = new StringBuilder("boolean ENABLED = ").append(enabled).append("\n");

        for (Map.Entry<String, Object> entry : optionValues.entrySet()) {
            String optionName = entry.getKey();
            Object optionValue = entry.getValue();

            LKVType optionType = optionTypes.get(optionName);

            optionsFileBuilder.append(optionType.typeName).append(" ").append(optionName).append(" = ").append(optionValue.toString()).append("\n");
        }


        replaceContentsOfFile(optionsLkvFile, optionsFileBuilder.toString());
    }

    private File getOptionsLkvFile() {
        return new File(TanksLua.FULL_SCRIPT_PATH + "/extension-options/" + packSource.getPackName() + ".lkv");
    }


    public LuaExtension(File extensionFile) throws LKVParseException, FileNotFoundException {
        super(extensionFile);

        File extensionLuaFile = packSource.getFile("extension.lua");
        File extensionMetaFile = packSource.getFile("extension-meta.lkv");

        if (extensionMetaFile == null)
            throw new FileNotFoundException("Missing metadata file for extension " + extensionFile.getName());

        loadMeta(extensionMetaFile);
        loadOptions();

        loadCallbacks(extensionLuaFile);

        LuaValue fOnLoad = callbacks.get("onLoad");
        if (fOnLoad.type() == Lua.LuaType.NIL) return;
        SafeLuaRunner.safeCall(fOnLoad);
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

    private void loadCallbacks(File extensionLuaFile) {
        LuaValue table = getAndVerifyTableFrom(extensionLuaFile, CALLBACK_TYPES);

        for (String callbackName : CALLBACK_TYPES.keySet()) {
            callbacks.put(callbackName, table.get(callbackName));
        }
    }


    public static void loadExtensionsTo(List<LuaExtension> extensionList) {
//        loadDecoys(extensionList,30);
        File extensionDirectory = new File(TanksLua.FULL_SCRIPT_PATH + "/extensions/");
        File[] extensionFiles = Objects.requireNonNull(extensionDirectory.listFiles());

        for (File file : extensionFiles) {
            try {
                extensionList.add(new LuaExtension(file));
            } catch (LKVParseException e) {
                new Notification(Notification.NotificationType.WARN, 5, "Extension " + file.getName() + " has invalid metadata! See log for details");
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                new Notification(Notification.NotificationType.WARN, 5, "Extension " + file.getName() + " has a missing metadata file! See log for details");
                e.printStackTrace();
            }
        }
    }


    private LuaExtension() {
    }

    @SuppressWarnings({"unused", "SameParameterValue"})
    private static void loadDecoys(List<LuaExtension> extensionList, int amount) {
        @SuppressWarnings("NullableProblems")
        class LuaGaslightMap<K, V> implements Map<K, V> {
            @Override
            public int size() {
                return 0;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public boolean containsKey(Object key) {
                return false;
            }

            @Override
            public boolean containsValue(Object value) {
                return false;
            }

            @Override
            public V get(Object key) {
                return (V) TanksLua.tanksLua.internalLuaState.fromNull();
            }


            @Override
            public V put(K key, V value) {
                return null;
            }

            @Override
            public V remove(Object key) {
                return null;
            }

            @Override
            public void putAll(Map<? extends K, ? extends V> m) {

            }

            @Override
            public void clear() {

            }


            @Override
            public Set<K> keySet() {
                return null;
            }


            @Override
            public Collection<V> values() {
                return null;
            }


            @Override
            public Set<Entry<K, V>> entrySet() {
                return null;
            }
        }

        for (int num = 1; num <= amount; num++) {
            LuaExtension decoy = new LuaExtension();
            decoy.name = "Fake Extension #" + num;
            decoy.authorName = "John Null";
            decoy.description = "not real";
            decoy.version = new SemanticVersion(1, 0, 0);
            decoy.enabled = false;
            decoy.callbacks = new LuaGaslightMap<>();

            extensionList.add(decoy);
        }
    }
}
