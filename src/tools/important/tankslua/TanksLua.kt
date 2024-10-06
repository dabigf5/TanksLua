package tools.important.tankslua

import main.Tanks
import party.iroiro.luajava.Lua
import party.iroiro.luajava.luajit.LuaJit
import tanks.Drawing
import tanks.Game
import tanks.Panel
import tanks.extension.Extension
import tanks.gui.Button
import tanks.gui.screen.ScreenOptions
import tools.important.tankslua.gui.EvalBox
import tools.important.tankslua.gui.drawNotifications
import tools.important.tankslua.gui.updateNotifications
import tools.important.tankslua.screen.ScreenOptionsLua

class TanksLuaOptions {
    var evalBoxEnabled: Boolean = false

    fun save() {} // todo
    fun load() {} // todo
}

object TanksLua {
    val internalState: Lua = LuaJit().apply { openLibraries() }
    lateinit var evalBox: EvalBox
    lateinit var luaOptionsButton: Button
    val options = TanksLuaOptions()
}

class TanksLuaExtension : Extension("TanksLua") {
    override fun setUp() {
        TanksLua.evalBox = EvalBox()
        TanksLua.luaOptionsButton = Button(
            Drawing.drawing.interfaceSizeX * 0.5,
            Drawing.drawing.interfaceSizeY * 0.5 + Drawing.drawing.objYSpace * 5,
            Drawing.drawing.objWidth,
            Drawing.drawing.objHeight,
            "TanksLua Options"
        ) {
            Game.screen = ScreenOptionsLua()
        }

        TanksLua.options.load()
    }

    override fun update() {
        if (TanksLua.options.evalBoxEnabled) TanksLua.evalBox.update()
        if (Game.screen is ScreenOptions) TanksLua.luaOptionsButton.update()
        updateNotifications()
    }

    override fun draw() {
        if (TanksLua.options.evalBoxEnabled) TanksLua.evalBox.draw()
        drawNotifications()
        if (Game.screen is ScreenOptions) TanksLua.luaOptionsButton.draw()

        Panel.panel.drawMouseTarget()
    }
}

fun main() {
    Tanks.launchWithExtensions(arrayOf("debug"), arrayOf(TanksLuaExtension()), null)
}