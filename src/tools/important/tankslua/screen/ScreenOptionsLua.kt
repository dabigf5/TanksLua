package tools.important.tankslua.screen

import tanks.Drawing
import tanks.Game
import tanks.gui.Button
import tanks.gui.screen.Screen
import tanks.gui.screen.ScreenOptions
import tools.important.tankslua.TanksLua
import tools.important.tankslua.gui.ToggleButton
import tools.important.tankslua.openFileManagerTo
import tools.important.tankslua.tanksLuaDir
import tools.important.tankslua.verifyDirectoryStructure

class ScreenOptionsLua : Screen() {
    init {
        music = "menu_options.ogg"
        musicID = "menu"
    }

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
        centerX - objXSpace/2,
        centerY - objYSpace*0.5,
        objWidth,
        objHeight,
        "Eval box",
        { b -> TanksLua.options.evalBoxEnabled = b },
        TanksLua.options.evalBoxEnabled
    )

    val enableLevelScripts = ToggleButton(
        centerX - objXSpace/2,
        centerY - objYSpace*1.5,
        objWidth,
        objHeight,
        "Level scripts",
        { b -> TanksLua.options.levelScriptsEnabled = b },
        TanksLua.options.levelScriptsEnabled
    )

    val gotoLuaExtensions = Button(
        centerX + objXSpace/2,
        centerY - objYSpace*1.5,
        objWidth,
        objHeight,
        "Lua Extensions",
    ) {
        Game.screen = ScreenLuaExtensions()
    }

    val tanksluaFiles = Button(
        centerX + objXSpace/2,
        centerY - objYSpace*0.5,
        objWidth,
        objHeight,
        "   Open TanksLua folder", // The text will overlap with the folder icon without the spaces at the beginning
    ) {
        verifyDirectoryStructure()
        openFileManagerTo(tanksLuaDir)
    } .apply {
        image = "icons/folder.png"
        imageSizeX = objHeight * 0.75
        imageSizeY = objHeight * 0.75
        imageXOffset = -objWidth / 2.0 + imageSizeX
    }

    override fun update() {
        backButton.update()
        toggleEvalBox.update()
        gotoLuaExtensions.update()
        tanksluaFiles.update()
        enableLevelScripts.update()
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
        tanksluaFiles.draw()
        enableLevelScripts.draw()
    }
}