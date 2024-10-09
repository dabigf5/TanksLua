package tools.important.tankslua.screen

import tanks.Drawing
import tanks.Game
import tanks.gui.Button
import tanks.gui.screen.Screen
import tanks.gui.screen.ScreenOptions
import tools.important.tankslua.TanksLua
import tools.important.tankslua.gui.ToggleButton

class ScreenOptionsLua : Screen() {
    val backButton = Button(
        centerX,
        centerY + objYSpace * 3.5,
        objWidth,
        objHeight,
        "Back"
    ) {
        Game.screen = ScreenOptions()
        TanksLua.options.save()
    }

    val toggleEvalBox = ToggleButton(
        centerX,
        centerY - objYSpace,
        objWidth,
        objHeight,
        "Eval box",
        { b -> TanksLua.options.evalBoxEnabled = b },
        TanksLua.options.evalBoxEnabled
    )



    val gotoLuaExtensions = Button(
        centerX,
        centerY + objYSpace * 2.0,
        objWidth,
        objHeight,
        "Lua Extensions",
    ) {
        Game.screen = ScreenLuaExtensions()
    }

    init {
        music = "menu_options.ogg"
        musicID = "menu"
    }

    override fun update() {
        backButton.update()
        toggleEvalBox.update()
        gotoLuaExtensions.update()
    }

    override fun draw() {
        drawDefaultBackground()

        val drawing = Drawing.drawing!!

        drawing.setInterfaceFontSize(titleSize)
        drawing.setColor(0.0, 0.0, 0.0)

        drawing.displayInterfaceText(centerX, centerY - objYSpace * 3.5, "TanksLua Options")

        drawing.setInterfaceFontSize(titleSize/2)
        drawing.drawInterfaceText(centerX, centerY - objYSpace * 3.0, TanksLua.VERSION)

        backButton.draw()
        toggleEvalBox.draw()
        gotoLuaExtensions.draw()
    }
}