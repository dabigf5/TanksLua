package tools.important.tankslua;

import main.Tanks;
import party.iroiro.luajava.Lua;
import party.iroiro.luajava.LuaException;
import party.iroiro.luajava.lua54.Lua54;
import party.iroiro.luajava.value.LuaValue;
import tanks.Drawing;
import tanks.Game;
import tanks.Panel;
import tanks.extension.Extension;
import tanks.gui.Button;
import tanks.gui.screen.Screen;
import tanks.gui.screen.ScreenOptions;
import tools.important.tankslua.gui.EvalBox;
import tools.important.tankslua.gui.screen.ScreenOptionsLua;
import tools.important.tankslua.luacompatible.LuaCompatibleArrayList;
import tools.important.tankslua.luacompatible.LuaCompatibleHashMap;
import tools.important.tankslua.lualib.JavaLibExtras;
import tools.important.tankslua.lualib.LuaLib;
import tools.important.tankslua.lualib.TanksLib;
import tools.important.tankslua.luapackage.LevelPack;
import tools.important.tankslua.luapackage.LuaExtension;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public final class TanksLua extends Extension {
    /**
     * The one and only instance of TanksLua.
     */
    public static TanksLua tanksLua;
    public static final String VERSION = "TanksLua Alpha 0.2.0";
    public static final String SCRIPT_PATH = Game.directoryPath + "/scripts";
    public static final String FULL_SCRIPT_PATH = System.getProperty("user.home").replace('\\', '/') + SCRIPT_PATH;

    /**
     * A lua state which is used for miscellaneous operations which don't need any specific lua state.<br><br>
     * As a rule of thumb, it should never be written to (such as having globals declared within it).<br><br>
     * Additionally, no code given to us by the user will be ran with this lua state. To summarize:<br>
     * This lua state's purpose is to be a neutral, unbiased lua state which everyone goes to for advice.
     */
    public Lua internalLuaState;
    public LuaCompatibleHashMap<String, Object> options;
    public LuaCompatibleArrayList<LuaExtension> loadedLuaExtensions;
    public static final HashMap<String, Lua.LuaType> optionTypes = new HashMap<>();
    static {
        optionTypes.put("enableLevelScripts", Lua.LuaType.BOOLEAN);
        optionTypes.put("enableEvalBox", Lua.LuaType.BOOLEAN);
    }

    public EvalBox evalBox;
    public Button enterLuaOptionsButton;
    public TanksEventListener eventListener = new TanksEventListener();
    public ArrayList<Notification> activeNotifications = new ArrayList<>();

    public TanksLua() {
        super("TanksLua");
        if (tanksLua != null) throw new IllegalStateException("Attempt to create a new instance of TanksLua");
    }

    private static void initializeScriptsDir() {
        makeEmptyDirectory(FULL_SCRIPT_PATH);
        makeEmptyDirectory(FULL_SCRIPT_PATH + "/extensions");
        makeEmptyDirectory(FULL_SCRIPT_PATH + "/level");
        makeEmptyDirectory(FULL_SCRIPT_PATH + "/extension-options");
        makeFileWithContents(FULL_SCRIPT_PATH + "/options.lua", "return {}");
    }

    private static void makeEmptyDirectory(String path) {
        File dir = new File(path);
        if (!dir.mkdir()) {
            throw new RuntimeException("Unable to create directory " + path);
        }
    }
    // this may be needed one day
//    private static void makeEmptyFile(String path) {
//        File file = new File(path);
//
//        try {
//            if (!file.createNewFile()) {
//                throw new RuntimeException("Unable to create file "+path);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//            throw new RuntimeException("Unable to create file "+path);
//        }
//    }

    private static void makeFileWithContents(String path, @SuppressWarnings("SameParameterValue") String contents) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(path));
            writer.write(contents);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Unable to create file " + path);
        }
    }
    private static final LuaLib[] defaultLibraries = {
            new TanksLib(),
            new JavaLibExtras(),
    };
    public static void openCustomLibs(Lua luaState) {
        for (LuaLib lib : defaultLibraries) {
            lib.open(luaState);
        }
    }
    @Override
    public void setUp() {
        tanksLua = this;
        options = new LuaCompatibleHashMap<>();
        loadedLuaExtensions = new LuaCompatibleArrayList<>();

        if (!new File(FULL_SCRIPT_PATH).exists())
            initializeScriptsDir();

        internalLuaState = new Lua54();
        internalLuaState.openLibraries(); // not sure if really needed, but not taking risks either

        evalBox = new EvalBox();

        loadOptions();

        LuaExtension.loadExtensionsTo(loadedLuaExtensions);

        Screen screen = Game.screen;
        enterLuaOptionsButton = new Button(
                screen.centerX,
                screen.centerY + screen.objYSpace * 5,
                screen.objWidth,
                screen.objHeight,
                "Lua options", () -> Game.screen = new ScreenOptionsLua(),
                "The options for the TanksLua extension"
        );


    }

    public LevelPack currentLevelPack;

    private static final HashMap<String, Object> defaultOptions = new HashMap<>();
    static {
        defaultOptions.put("enableLevelScripts", false);
        defaultOptions.put("enableEvalBox", false);
    }

    public void loadOptions() {
        SafeLuaRunner.LuaResult result = SafeLuaRunner.safeLoadFile(internalLuaState, FULL_SCRIPT_PATH + "/options.lua");

        if (result.status != Lua.LuaError.OK) {
            throw new LuaException("options.lua failed to load!");
        }

        LuaValue fLoadedFunction = result.returns[0];

        SafeLuaRunner.LuaResult loadedCallResult = SafeLuaRunner.safeCall(fLoadedFunction);
        if (loadedCallResult.status != Lua.LuaError.OK) {
            throw new LuaException("options.lua failed to run!");
        }

        LuaValue[] returns = loadedCallResult.returns;

        if (returns.length != 1) {
            throw new LuaException("options.lua did not return exactly one value!");
        }

        LuaValue tOptions = returns[0];
        if (tOptions.type() != Lua.LuaType.TABLE) {
            throw new LuaException("options.lua did not return a table!");
        }

        HashMap<Object, Object> tOptionsMap = (HashMap<Object, Object>) tOptions.toJavaObject();
        assert tOptionsMap != null;
        for (Object tKey : tOptionsMap.keySet()) {
            if (!(tKey instanceof String)) {
                throw new LuaException("Key exists in options table that is not a string!");
            }
            String optionName = (String) tKey;
            Lua.LuaType expectedType = optionTypes.get(optionName);
            if (expectedType == null) {
                throw new LuaException("Unknown option name '" + optionName + "'!");
            }
            LuaValue value = tOptions.get(optionName);
            Lua.LuaType valueType = value.type();
            if (valueType != expectedType) {
                throw new LuaException("Option " + optionName + " is of the wrong type! (Expected " + expectedType + ", got " + valueType + ")");
            }
        }

        this.options.clearAndCopyLuaTable(tOptions);

        for (String expectedOptionName : optionTypes.keySet()) {
            if (tOptions.get(expectedOptionName).type() == Lua.LuaType.NIL) {
                options.put(expectedOptionName, defaultOptions.get(expectedOptionName));
            }
        }
    }

    public void saveOptions() {
        StringBuilder newOptionsScript = new StringBuilder("return {\n");

        for (Map.Entry<String, Object> entry : options.entrySet()) {
            String optionName = entry.getKey();
            Object optionValue = entry.getValue();

            String optionValueStringified = optionValue.toString();
            if (optionValueStringified.equals("null")) {
                optionValueStringified = "nil";
            }

            newOptionsScript.append("\t").append(optionName).append(" = ").append(optionValueStringified).append(",\n");
        }
        newOptionsScript.append("}");
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(FULL_SCRIPT_PATH + "/options.lua"));
            writer.write(newOptionsScript.toString());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void draw() {
        if (Drawing.drawing.enableStats) {
            final String drawnText = "Running " + VERSION;
            double rightEdge = Game.game.window.absoluteWidth;
            double textWidth = Game.game.window.fontRenderer.getStringSizeX(0.4, drawnText);

            Drawing.drawing.setColor(255.0, 227.0, 186.0);
            Game.game.window.fontRenderer.drawString(rightEdge - textWidth - 5, (int) (Panel.windowHeight - 55 + 22), 0.4, 0.4, drawnText);
        }

        boolean drawExtraMouseTarget = false;

        Screen screen = Game.screen;
        if (screen instanceof ScreenOptions) {
            drawExtraMouseTarget = true;
            enterLuaOptionsButton.draw();
        }

        if (options != null && (boolean) options.get("enableEvalBox")) {
            drawExtraMouseTarget = true;
            evalBox.draw();
        }

        for (Notification notif : activeNotifications) {
            notif.draw();
            drawExtraMouseTarget = true;
        }

        eventListener.onDraw();

        if (drawExtraMouseTarget) Panel.panel.drawMouseTarget();
    }

    @Override
    public void update() {
        eventListener.onUpdate();

        if (Game.screen instanceof ScreenOptions) {
            enterLuaOptionsButton.update();
        }

        if (options != null && (boolean) options.get("enableEvalBox")) {
            evalBox.update();
        }
        for (int i = 0; i < activeNotifications.size(); i++) {
            Notification notif = activeNotifications.get(i);

            notif.update();
            if (notif.removing) {
                activeNotifications.remove(i);
                i--;
            }
        }
    }

    public static void main(String[] args) {
        Tanks.launchWithExtensions(args, new Extension[]{new TanksLua()}, new int[]{});
    }
}
