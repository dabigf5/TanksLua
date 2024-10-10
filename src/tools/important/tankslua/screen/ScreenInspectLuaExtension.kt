package tools.important.tankslua.screen

import tanks.Drawing
import tanks.Game
import tanks.gui.Button
import tanks.gui.screen.Screen
import tools.important.tankslua.LuaExtension
import tools.important.tankslua.RealLuaExtension
import tools.important.tankslua.gui.ToggleButton
import tools.important.tankslua.initializeExtensionLuaState

class ScreenInspectLuaExtension(val extension: LuaExtension) : Screen() {
    init {
        music = "menu_options.ogg"
        musicID = "menu"
    }

    val backButton = Button(
        centerX,
        centerY + objYSpace * 5.0,
        objWidth,
        objHeight,
        "Back"
    ) {
        if (extension is RealLuaExtension) extension.saveOptions()

        Game.screen = ScreenLuaExtensions()
    }

    val enableButton = ToggleButton(
        centerX,
        centerY + objYSpace * 4.0,
        objWidth,
        objHeight,
        "Enabled",
        { b ->
            (extension as RealLuaExtension).enabled = b
            if (b && extension.luaState == null) initializeExtensionLuaState(extension)
        },
        if (extension is RealLuaExtension) extension.enabled else false
    )

    override fun update() {
        backButton.update()
        if (extension is RealLuaExtension) enableButton.update()
    }

    override fun draw() {
        drawDefaultBackground()
        backButton.draw()

        val drawing = Drawing.drawing!!

        drawing.setColor(0.0, 0.0, 0.0)

        drawing.setInterfaceFontSize(titleSize)
        drawing.drawInterfaceText(centerX, drawing.interfaceSizeY * 0.2, extension.displayName)
        drawing.setInterfaceFontSize(titleSize*0.8)
        drawing.drawInterfaceText(centerX, drawing.interfaceSizeY * 0.3, extension.id)

        val descriptionX = centerX
        drawing.setInterfaceFontSize(titleSize*0.8)
        val descriptionY = drawing.interfaceSizeY * 0.35
        extension.description?.split("---")?.also { lines ->
            for ((i, line) in lines.withIndex()) {
                val height = drawing.getStringHeight(line)
                drawing.drawInterfaceText(descriptionX, descriptionY + (i*height), line)
            }
        }

        if (extension is RealLuaExtension) enableButton.draw()
    }
}