package tools.important.tankslua;

import party.iroiro.luajava.Lua;
import party.iroiro.luajava.value.LuaValue;

import java.io.File;
import java.util.HashMap;

public final class LevelScript extends LuaScript {
    public String fileName;
    private static final HashMap<String, LuaScript.TableType> LEVEL_TABLE_TYPES = new HashMap<>();
    static {
        LEVEL_TABLE_TYPES.put("onLoad",      new LuaScript.TableType(Lua.LuaType.FUNCTION, true));
        LEVEL_TABLE_TYPES.put("onUpdate",    new LuaScript.TableType(Lua.LuaType.FUNCTION, true));
        LEVEL_TABLE_TYPES.put("onDraw",      new LuaScript.TableType(Lua.LuaType.FUNCTION, true));
    }

    @LuaNillable
    public LuaValue fOnLoad;
    @LuaNillable
    public LuaValue fOnUpdate;
    @LuaNillable
    public LuaValue fOnDraw;

    public LevelScript(LuaValue tLevel, String fileName) {
        super(fileName, tLevel, LEVEL_TABLE_TYPES);

        this.fileName = fileName;
        this.fOnLoad = tLevel.get("onLoad");



    }
    public static LevelScript currentLevelScript;
    public static void tryLoadingLevelScript(String rawName) {
        if (rawName == null) return;

        String nameNoExt = rawName.substring(0, rawName.length()-6);
        String levelLuaFileName = nameNoExt+".lua";
        String levelLuaFilePath = TanksLua.fullScriptPath+"/level/"+levelLuaFileName;
        File levelLuaFile = new File(levelLuaFilePath);
        if (!levelLuaFile.exists()) return;
        final int errNotifTime = 5;
        LuaValue loadedFile = SafeLuaRunner.safeLoadFile(levelLuaFilePath);
        if (loadedFile == null) {
            new Notification(Notification.NotificationType.WARN, errNotifTime, levelLuaFileName+" failed to load");
            return;
        }

        SafeLuaRunner.UserCallResult result = SafeLuaRunner.safeCall(loadedFile);

        if (result.status() != Lua.LuaError.OK) {
            new Notification(Notification.NotificationType.WARN, errNotifTime, levelLuaFileName+" ran into an error when running");
            return;
        }

        LuaValue[] returns = result.returns();

        if (returns.length != 1) {
            new Notification(Notification.NotificationType.WARN, errNotifTime, levelLuaFileName+" did not return exactly one value!");
            return;
        }

        LuaValue tLevel = returns[0];

        currentLevelScript = new LevelScript(tLevel, levelLuaFileName);
    }
}
