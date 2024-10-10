package tools.important.tankslua.screen

import tanks.Drawing
import tanks.Game
import tanks.gui.Button
import tanks.gui.ButtonList
import tanks.gui.screen.Screen
import tools.important.tankslua.RealLuaExtension
import tools.important.tankslua.clearLoadedExtensions
import tools.important.tankslua.gui.Notification
import tools.important.tankslua.gui.NotificationType
import tools.important.tankslua.loadLuaExtensions
import tools.important.tankslua.loadedExtensions


private fun getExtensionButtons(): ArrayList<Button> {
    val buttons = arrayListOf<Button>()
    for (extension in loadedExtensions) {
        val button = Button(
            0.0, 0.0, 0.0, 0.0,
            extension.displayName, "Description: ${extension.description}---" +
                    "---" +
                    "${extension.id}---" +
                    if (extension is RealLuaExtension)
                        if (extension.enabled) "enabled" else "disabled"
                    else
                        "no functionality"
        )

        button.function = Runnable {Game.screen = ScreenInspectLuaExtension(extension)}
        button.enabled = true

        buttons.add(button)
    }
    return buttons
}

class ScreenLuaExtensions : Screen() {
    init {
        music = "menu_options.ogg"
        musicID = "menu"
    }

    val extensions = ButtonList(
        getExtensionButtons(),
        0,
        0.0,
        -objYSpace,
    )

    val backButton = Button(
        centerX,
        centerY + objYSpace * 5.0,
        objWidth,
        objHeight,
        "Back"
    ) {
        Game.screen = ScreenOptionsLua()
    }

    val reloadButton = Button(
        centerX,
        centerY + objYSpace * 6.0,
        objWidth,
        objHeight,
        "Reload Extensions"
    ) {
        clearLoadedExtensions()
        loadLuaExtensions()

        extensions.buttons = getExtensionButtons()
        extensions.sortButtons()

        Notification("Reloaded extensions!", NotificationType.INFO)
    }

    override fun update() {
        backButton.update()
        extensions.update()
        reloadButton.update()
    }

    override fun draw() {
        drawDefaultBackground()
        val drawing = Drawing.drawing!!
        drawing.setInterfaceFontSize(titleSize)
        drawing.setColor(0.0, 0.0, 0.0)
        drawing.displayInterfaceText(centerX, drawing.interfaceSizeY * 0.15, "Lua Extensions")

        backButton.draw()
        extensions.draw()
        reloadButton.draw()
    }
}