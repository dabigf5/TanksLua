package tools.important.tankslua.screen

import org.lwjgl.glfw.GLFW
import tanks.Drawing
import tanks.Game
import tanks.gui.Button
import tanks.gui.screen.Screen
import tanks.gui.screen.ScreenTitle
import tools.important.tankslua.TanksLua
import kotlin.system.exitProcess

class ScreenLuaWarning : Screen() {
    val proceeding: Boolean
        get() = Game.game.window.validPressedKeys.contains(GLFW.GLFW_KEY_LEFT_SHIFT)

    val button = Button(
        centerX,
        centerY + objYSpace * 4,
        objWidth,
        objHeight,
        ""
    ) {
        if (proceeding){
            TanksLua.options.warningSeen = true
            TanksLua.options.save()
            Game.screen = ScreenTitle()
        } else exitProcess(0)
    }

    override fun update() {
        button.setText(if (proceeding) "Proceed" else "Exit")
        button.update()
    }

    override fun draw() {
        drawDefaultBackground()

        val drawing = Drawing.drawing!!

        drawing.setColor(255.0, 0.0, 0.0)
        drawing.setInterfaceFontSize(150.0)
        drawing.drawInterfaceText(centerX, drawing.interfaceSizeY * 0.2, "WARNING!")

        drawing.setColor(0.0, 0.0, 0.0)
        drawing.setInterfaceFontSize(titleSize * 0.8)
        drawing.drawInterfaceText(centerX, drawing.interfaceSizeY * 0.3,
            "This extension runs unsandboxed Lua code!"
        )
        drawing.drawInterfaceText(centerX, drawing.interfaceSizeY * 0.3 + titleSize,
            "Lua code has great potential to destroy your computer,"
        )
        drawing.drawInterfaceText(centerX, drawing.interfaceSizeY * 0.3 + titleSize*2,
            " or even steal important data such as passwords,"
        )
        drawing.drawInterfaceText(centerX, drawing.interfaceSizeY * 0.3 + titleSize*3,
            "due to the fact it can do everything Tanks can do."
        )
        drawing.drawInterfaceText(centerX, drawing.interfaceSizeY * 0.3 + titleSize*4,
            "Only add Lua code from sources you trust!"
        )

        drawing.setInterfaceFontSize(titleSize)
        drawing.drawInterfaceText(centerX, drawing.interfaceSizeY/2,
            "If you acknowledge the risk and wish to proceed,"
        )
        drawing.drawInterfaceText(centerX, drawing.interfaceSizeY/2 + titleSize,
            "you can hold down left shift and click the proceed button."
        )

        drawing.drawInterfaceText(centerX, drawing.interfaceSizeY/2 + titleSize*3,
            "If you do not wish to proceed,"
        )
        drawing.drawInterfaceText(centerX, drawing.interfaceSizeY/2 + titleSize*4,
            "you can click the exit button to exit the game."
        )

        button.draw()
    }
}