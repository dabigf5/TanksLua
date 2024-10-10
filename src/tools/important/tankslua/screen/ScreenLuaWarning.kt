package tools.important.tankslua.screen

import org.lwjgl.glfw.GLFW
import tanks.Drawing
import tanks.Game
import tanks.Panel
import tanks.gui.Button
import tanks.gui.screen.Screen
import tanks.gui.screen.ScreenTitle
import tools.important.tankslua.TanksLua
import tools.important.tankslua.clearLoadedExtensions
import tools.important.tankslua.loadLuaExtensions
import kotlin.system.exitProcess

class ScreenLuaWarning : Screen() {
    init {
        music = "ready_music_3.ogg"
        Panel.forceRefreshMusic = true

        TanksLua.options.evalBoxEnabled = false
    }

    val canProceed: Boolean
        get() = Game.game.window.validPressedKeys.contains(GLFW.GLFW_KEY_LEFT_CONTROL) &&
                Game.game.window.validPressedKeys.contains(GLFW.GLFW_KEY_LEFT_SHIFT) &&
                Game.game.window.validPressedKeys.contains(GLFW.GLFW_KEY_Z)

    val proceedButton = Button(
        centerX,
        centerY + objYSpace * 4,
        objWidth,
        objHeight,
        "Proceed"
    ) {
        TanksLua.options.warningSeen = true
        TanksLua.options.save()
        Game.screen = ScreenTitle()

        clearLoadedExtensions() // just in case
        loadLuaExtensions()
    }

    val exitButton = Button(
        centerX,
        centerY + objYSpace * 5,
        objWidth,
        objHeight,
        "Quit"
    ) {
        exitProcess(0)
    }

    override fun update() {
        proceedButton.enabled = canProceed
        proceedButton.update()
        exitButton.update()
    }

    override fun draw() {
        drawDefaultBackground()

        val drawing = Drawing.drawing!!

        drawing.setColor(255.0, 0.0, 0.0)
        drawing.setInterfaceFontSize(150.0)
        drawing.drawInterfaceText(centerX, drawing.interfaceSizeY * 0.2, "WARNING!")

        drawing.setColor(0.0, 0.0, 0.0)
        drawing.setInterfaceFontSize(titleSize * 0.8)
        drawing.drawInterfaceText(
            centerX, drawing.interfaceSizeY * 0.3,
            "TanksLua runs unsandboxed Lua code!"
        )
        drawing.drawInterfaceText(
            centerX, drawing.interfaceSizeY * 0.3 + titleSize,
            "Lua code has great potential to destroy your computer,"
        )
        drawing.drawInterfaceText(
            centerX, drawing.interfaceSizeY * 0.3 + titleSize * 2,
            " or even steal important data such as passwords,"
        )
        drawing.drawInterfaceText(
            centerX, drawing.interfaceSizeY * 0.3 + titleSize * 3,
            "due to the fact it can do everything Tanks can do."
        )
        drawing.drawInterfaceText(
            centerX, drawing.interfaceSizeY * 0.3 + titleSize * 4,
            "Only add Lua code from sources you trust!"
        )

        drawing.setInterfaceFontSize(titleSize)
        drawing.drawInterfaceText(
            centerX, drawing.interfaceSizeY / 2,
            "If you acknowledge the risk and wish to proceed,"
        )
        drawing.drawInterfaceText(
            centerX, drawing.interfaceSizeY / 2 + titleSize,
            "you can hold down left control, left shift, and Z, to enable the proceed button."
        )

        drawing.drawInterfaceText(
            centerX, drawing.interfaceSizeY / 2 + titleSize * 3,
            "If you do not wish to proceed,"
        )
        drawing.drawInterfaceText(
            centerX, drawing.interfaceSizeY / 2 + titleSize * 4,
            "you can click the exit button to exit the game."
        )

        proceedButton.draw()
        exitButton.draw()
    }
}