package tools.important.tankslua

import main.Tanks
import tanks.Drawing
import tanks.Game
import tanks.Panel
import tanks.extension.Extension
import tanks.gui.Button
import tanks.gui.screen.ScreenOptions
import tools.important.tankslua.gui.*
import tools.important.tankslua.screen.ScreenOptionsLua
import tools.important.tankslua.tkv.*
import java.io.File
import java.io.IOException

class TanksLuaOptions {
    var evalBoxEnabled: Boolean = false

    fun save() {
        val encodedOptions = encodeTKV(
            mapOf(
                "evalBoxEnabled" to TKVValue(TKVType.BOOLEAN, evalBoxEnabled)
            )
        )

        try {
            TanksLua.settingsFile.bufferedWriter().use {
                it.write(encodedOptions)
            }
        } catch (i: IOException) {
            Notification("Failed to write options (${i.message})", NotificationType.ERR)
            return
        }
    }

    fun load() {
        if (!TanksLua.settingsFile.exists()) return

        val encodedOptions = try {
            TanksLua.settingsFile.bufferedReader().use {
                it.readText()
            }
        } catch (i: IOException) {
            Notification("Failed to read options (${i.message})", NotificationType.ERR)
            return
        }

        val decodedOptions = try {
            decodeTKV(encodedOptions)
        } catch (e: TKVDecodeException) {
            Notification("Failed to decode options (${e.message})", NotificationType.ERR)
            return
        }

        for ((name, value) in decodedOptions) {
            when (name) {
                "evalBoxEnabled" -> {
                    if (value.type != TKVType.BOOLEAN) continue
                    evalBoxEnabled = value.value as Boolean
                }
            }
        }
    }
}

object TanksLua {
    const val VERSION = "TanksLua 0.4.0"
    val tanksLuaDir = File(
        System.getProperty("user.home").replace('\\', '/') + "/.tanks/tankslua"
    ).apply {
        if (!isDirectory) {
            if (exists()) delete()
            mkdir()
        }
    }

    val levelDir = File(tanksLuaDir.path + "/level").apply {
        if (!isDirectory) {
            if (exists()) delete()
            mkdir()
        }
    }

    val settingsFile = File(tanksLuaDir.path + "/settings.tkv").apply {
        if (isDirectory) {
            if (exists()) delete()
        }
    }

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