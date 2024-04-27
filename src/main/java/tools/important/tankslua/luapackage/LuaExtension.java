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
        String content;
        try {
            content = new Scanner(extensionMetaFile).useDelimiter("\\Z").next();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (NoSuchElementException e) {
            throw new LKVParseException("Empty metadata file!");
        }


        HashMap<String, LKVValue> pairs = LKV.parse(content);

        // maybe change this if there's a lot of metadata

        LKVValue lkvName = pairs.get("name");
        if (lkvName == null) throw new LKVParseException("Missing required LKV metadata entry name");
        if (lkvName.type != LKVType.STRING) throw new LKVParseException("LKV metadata entry name is of wrong type (expected string)");
        name = (String) lkvName.value;

        LKVValue lkvDescription = pairs.get("description");
        if (lkvDescription == null) throw new LKVParseException("Missing required LKV metadata entry description");
        if (lkvDescription.type != LKVType.STRING) throw new LKVParseException("LKV metadata entry description is of wrong type (expected string)");
        description = (String) lkvDescription.value;

        LKVValue lkvAuthor = pairs.get("authorName");
        if (lkvAuthor == null) throw new LKVParseException("Missing required LKV metadata entry authorName");
        if (lkvAuthor.type != LKVType.STRING) throw new LKVParseException("LKV metadata entry authorName is of wrong type (expected string)");
        authorName = (String) lkvAuthor.value;

        LKVValue lkvVersion = pairs.get("version");
        if (lkvVersion == null) throw new LKVParseException("Missing required LKV metadata entry version");
        if (lkvVersion.type != LKVType.VERSION) throw new LKVParseException("LKV metadata entry version is of wrong type (expected version)");
        version = (SemanticVersion) lkvVersion.value;
    }




    public LuaCompatibleHashMap<String, Object> optionValues = new LuaCompatibleHashMap<>();
    public LuaCompatibleHashMap<String, EntryType> optionTypes = new LuaCompatibleHashMap<>();



    public LuaExtension(File extensionFile) throws LKVParseException, FileNotFoundException {
        super(extensionFile);

        File extensionLuaFile = packSource.getFile("extension.lua");
        File extensionMetaFile = packSource.getFile("extension-meta.lkv");

        if (extensionMetaFile == null) throw new FileNotFoundException("Missing metadata file for extension "+extensionFile.getName());

        loadMeta(extensionMetaFile);
        loadCallbacks(extensionLuaFile);

        LuaValue fOnLoad = callbacks.get("onLoad");
        if (fOnLoad.type() == Lua.LuaType.NIL) return;
        SafeLuaRunner.safeCall(fOnLoad);
    }



    public HashMap<String, LuaValue> callbacks = new HashMap<>();
    private static final HashMap<String, EntryType> CALLBACK_TYPES = new HashMap<>();static {
        CALLBACK_TYPES.put("onLoad", new EntryType(Lua.LuaType.FUNCTION, true));
        CALLBACK_TYPES.put("onDraw", new EntryType(Lua.LuaType.FUNCTION, true));
        CALLBACK_TYPES.put("onUpdate", new EntryType(Lua.LuaType.FUNCTION, true));

        CALLBACK_TYPES.put("onLevelLoad", new EntryType(Lua.LuaType.FUNCTION, true));
    }
    private void loadCallbacks(File extensionLuaFile) {
        LuaValue table = getAndVerifyTableFrom(extensionLuaFile, CALLBACK_TYPES);

        for (String callbackName : CALLBACK_TYPES.keySet()) {
            callbacks.put(callbackName, table.get(callbackName));
        }
    }




    public static void loadExtensionsTo(List<LuaExtension> extensionList) {
        File extensionDirectory = new File(TanksLua.FULL_SCRIPT_PATH + "/extensions/");
        File[] extensionFiles = Objects.requireNonNull(extensionDirectory.listFiles());

        for (File file : extensionFiles) {
            try {
                extensionList.add(new LuaExtension(file));
            } catch (LKVParseException e) {
                new Notification(Notification.NotificationType.WARN, 5, "Extension "+file.getName()+" has invalidly formatted metadata! See log for details");
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                new Notification(Notification.NotificationType.WARN, 5, "Extension "+file.getName()+" has a missing metadata file! See log for details");
                e.printStackTrace();
            }
        }
    }
}
