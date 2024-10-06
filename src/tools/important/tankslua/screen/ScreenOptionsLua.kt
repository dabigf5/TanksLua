package tools.important.tankslua.screen

import tanks.Game
import tanks.gui.Button
import tanks.gui.screen.Screen
import tanks.gui.screen.ScreenOptions
import tools.important.tankslua.TanksLua

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

    val toggleEvalBox = Button( // todo: toggle button
        centerX,
        centerY - objYSpace,
        objWidth,
        objHeight,
        "Eval box"
    ) {
        TanksLua.options.evalBoxEnabled = !TanksLua.options.evalBoxEnabled
    }

    init {
        music = "menu_options.ogg"
        musicID = "menu"
    }

    override fun update() {
        backButton.update()
        toggleEvalBox.update()
    }

    override fun draw() {
        drawDefaultBackground()
        backButton.draw()
        toggleEvalBox.draw()
    }
}