package tools.important.tankslua.gui.screen;

import party.iroiro.luajava.Lua;
import party.iroiro.luajava.value.LuaValue;
import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.screen.Screen;
import tools.important.tankslua.LuaExtension;
import tools.important.tankslua.SafeLuaRunner;
import tools.important.tankslua.TanksLua;

import java.util.ArrayList;
public class ScreenOptionsLuaExtensionList extends Screen {
    public Button backButton;
    public Button reloadExtensionsButton;

    public ScreenOptionsLuaExtensionList() {
        backButton = new Button(centerX, centerY + objYSpace * 3.5, objWidth, objHeight, "Back", () -> Game.screen = new ScreenOptionsLua());
        reloadExtensionsButton = new Button(centerX, centerY+objHeight*3, objWidth, objHeight, "Reload Lua Extensions", () -> {
            TanksLua.tanksLua.loadedLuaExtensions.clear();
            Lua coreLuaState = TanksLua.tanksLua.coreLuaState;

            coreLuaState.getGlobal("tanks");
            int tanksIndex = coreLuaState.getTop();

            coreLuaState.push("lua");
            coreLuaState.getTable(tanksIndex);
            int luaIndex = coreLuaState.getTop();

            coreLuaState.push("registerExtensions");
            coreLuaState.getTable(luaIndex);

            LuaValue fRegisterExtensions = coreLuaState.get();
            SafeLuaRunner.safeCall(fRegisterExtensions);
        });
    }

    @Override
    public void update() {
        backButton.update();
        reloadExtensionsButton.update();
    }

    @Override
    public void draw() {
        drawDefaultBackground();
        Drawing.drawing.setColor(0,0,0);
        Drawing.drawing.setInterfaceFontSize(titleSize);
        Drawing.drawing.displayInterfaceText(centerX, centerY-objHeight*8, "Loaded Lua Extensions");
        backButton.draw();
        reloadExtensionsButton.draw();

        ArrayList<LuaExtension> extList = TanksLua.tanksLua.loadedLuaExtensions;

        for (int i = 0; i < extList.size(); i++) {
            LuaExtension lx = extList.get(i);
            final double baseX = centerX;
            final double baseY = (centerY/2) + (objHeight*(i*1.2));
            Drawing.drawing.setInterfaceFontSize(titleSize*0.8);
            Drawing.drawing.displayInterfaceText(baseX, baseY, lx.name());
            Drawing.drawing.setInterfaceFontSize(titleSize*0.5);
            Drawing.drawing.displayInterfaceText(baseX, baseY+(objHeight/2),"by "+lx.authorName());
        }
    }
    
}
