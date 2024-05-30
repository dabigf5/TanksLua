package tools.important.tankslua;

import main.Tanks;
import party.iroiro.luajava.Lua;
import party.iroiro.luajava.lua54.Lua54;
import tanks.Drawing;
import tanks.Game;
import tanks.Panel;
import tanks.extension.Extension;
import tanks.gui.Button;
import tanks.gui.screen.Screen;
import tanks.gui.screen.ScreenOptions;
import tools.important.javalkv.LKV;
import tools.important.javalkv.LKVParseException;
import tools.important.javalkv.LKVType;
import tools.important.javalkv.LKVValue;
import tools.important.tankslua.gui.EvalBox;
import tools.important.tankslua.gui.screen.ScreenOptionsLua;
import tools.important.tankslua.luacompatible.LuaCompatibleArrayList;
import tools.important.tankslua.lualib.LuaLib;
import tools.important.tankslua.lualib.TanksLib;
import tools.important.tankslua.luapackage.LevelPack;
import tools.important.tankslua.luapackage.LuaExtension;

import java.io.*;
import java.util.*;

public final class TanksLua extends Extension {
    /**
     * The one and only instance of TanksLua.
     */
    public static TanksLua tanksLua;
    public static final String VERSION = "TanksLua Alpha 0.3.0 (Bleeding Edge)";



    public static final String SCRIPT_PATH = Game.directoryPath + "/scripts";
    public static final String FULL_SCRIPT_PATH = System.getProperty("user.home").replace('\\', '/') + SCRIPT_PATH;

    /**
     * A lua state which is used for miscellaneous operations which don't need any specific lua state.<br><br>
     * As a rule of thumb, it should never be written to (such as having globals declared within it).<br><br>
     * Additionally, no code given to us by the user will be ran with this lua state. To summarize:<br>
     * This lua state's purpose is to be a neutral, unbiased lua state which everyone goes to for advice.
     */
    public Lua internalLuaState;


    public LevelPack currentLevelPack;
    public LuaCompatibleArrayList<LuaExtension> loadedLuaExtensions;

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
        makeEmptyDirectory(FULL_SCRIPT_PATH + "/temp");

        makeEmptyFile(FULL_SCRIPT_PATH + "/lua-options.lkv");
    }



    private static void makeEmptyDirectory(String path) {
        File dir = new File(path);
        if (!dir.mkdir()) {
            throw new RuntimeException("Unable to create directory " + path);
        }
    }
    // this may be needed one day
    private static void makeEmptyFile(String path) {
        File file = new File(path);

        try {
            if (!file.createNewFile()) {
                throw new RuntimeException("Unable to create file "+path);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Unable to create file "+path);
        }
    }

//    private static void makeFileWithContents(String path, @SuppressWarnings("SameParameterValue") String contents) {
//        try {
//            BufferedWriter writer = new BufferedWriter(new FileWriter(path));
//            writer.write(contents);
//            writer.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//            throw new RuntimeException("Unable to create file " + path);
//        }
//    }

    private static final LuaLib[] defaultLibraries = {
            new TanksLib(),
    };
    public static void openCustomLibs(Lua luaState) {
        for (LuaLib lib : defaultLibraries) {
            lib.open(luaState);
        }
    }


    public static String readContentsOfFile(File file) {
        String content;

        try {
            content = new Scanner(file).useDelimiter("\\Z").next();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (NoSuchElementException e) {
            return "";
        }

        return content;
    }

    public static void replaceContentsOfFile(File file, String newContents) {
        try {
            if (!file.exists()) //noinspection ResultOfMethodCallIgnored
                file.createNewFile();

            BufferedWriter writer = new BufferedWriter(new FileWriter(file));

            writer.write(newContents);

            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void setUp() {
        tanksLua = this;
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

    public static class TanksLuaOption {
        public String name;
        public LKVType type;
        public Object value;
        public Object defaultValue;
        private TanksLuaOption(String name, LKVType type, Object defaultValue) {
            this.name = name;
            this.type = type;
            this.defaultValue = defaultValue;
        }
    }

    private static final File OPTIONS_FILE = new File(FULL_SCRIPT_PATH + "/lua-options.lkv");
    private final List<TanksLuaOption> options = new ArrayList<>();{
        options.add(new TanksLuaOption("enableLevelScripts", LKVType.BOOLEAN, false));
        options.add(new TanksLuaOption("enableEvalBox", LKVType.BOOLEAN, false));
    }
    public Object getOptionValue(String name) {
        for (TanksLuaOption option : options) {
            if (option.name.equals(name)) return option.value;
        }
        throw new RuntimeException("No option exists called \""+name+"\"");
    }
    public void setOptionValue(String name, Object newValue) {
        for (TanksLuaOption option : options) {
            if (!option.name.equals(name)) continue;

            option.value = newValue;
            return;
        }

        throw new RuntimeException("No option exists called \""+name+"\"");
    }

    public void loadOptions() throws LKVParseException {
        Map<String, LKVValue> pairs = LKV.parse(readContentsOfFile(OPTIONS_FILE));

        for (TanksLuaOption option : options) {
            LKVValue optionValueLkv = pairs.get(option.name);

            if (optionValueLkv == null) {
                option.value = option.defaultValue;
                continue;
            }

            if (optionValueLkv.type != option.type) throw new LKVParseException("Option \""+option.name+"\" is of wrong type!");

            option.value = optionValueLkv.value;
        }
    }

    public void saveOptions() {
        StringBuilder optionsBuilder = new StringBuilder();

        for (TanksLuaOption option : options) {
            optionsBuilder
                    .append(option.type.typeName)
                    .append(' ')
                    .append(option.name)
                    .append(" = ")
                    .append(option.value.toString())
                    .append('\n');
        }

//        System.out.println(optionsBuilder);
        replaceContentsOfFile(OPTIONS_FILE, optionsBuilder.toString());
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

        if ((boolean) getOptionValue("enableEvalBox")) {
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

        if ((boolean) getOptionValue("enableEvalBox")) {
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
