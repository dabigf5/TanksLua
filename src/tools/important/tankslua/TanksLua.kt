package tools.important.tankslua

import main.Tanks
import party.iroiro.luajava.LuaException
import tanks.Drawing
import tanks.Game
import tanks.Panel
import tanks.extension.Extension
import tanks.gui.Button
import tanks.gui.screen.Screen
import tanks.gui.screen.ScreenGame
import tanks.gui.screen.ScreenOptions
import tools.important.tankslua.gui.*
import tools.important.tankslua.screen.ScreenLuaWarning
import tools.important.tankslua.screen.ScreenOptionsLua
import tools.important.tankslua.tkv.*
import java.io.IOException

class TanksLuaOptions {
    var warningSeen: Boolean = false
    var evalBoxEnabled: Boolean = false
    var levelScriptsEnabled: Boolean = false

    fun save() {
        val encodedOptions = encodeTKV(
            mapOf(
                "warningSeen" to TKVValue(TKVType.BOOLEAN, warningSeen),
                "evalBoxEnabled" to TKVValue(TKVType.BOOLEAN, evalBoxEnabled),
                "levelScriptsEnabled" to TKVValue(TKVType.BOOLEAN, levelScriptsEnabled)
            )
        )

        try {
            settingsFile.bufferedWriter().use {
                it.write(encodedOptions)
            }
        } catch (i: IOException) {
            Notification("Failed to write options (${i.message})", NotificationType.ERR)
            return
        }
    }

    fun load() {
        if (!settingsFile.exists()) return

        val encodedOptions = try {
            settingsFile.bufferedReader().use {
                it.readText()
            }
        } catch (e: IOException) {
            Notification("Failed to read options (${e.message})", NotificationType.ERR)
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
                "warningSeen" -> {
                    if (value.type != TKVType.BOOLEAN) continue
                    warningSeen = value.value as Boolean
                }
                "evalBoxEnabled" -> {
                    if (value.type != TKVType.BOOLEAN) continue
                    evalBoxEnabled = value.value as Boolean
                }
                "levelScriptsEnabled" -> {
                    if (value.type != TKVType.BOOLEAN) continue
                    levelScriptsEnabled = value.value as Boolean
                }
            }
        }
    }
}

object TanksLua {
    const val VERSION = "TanksLua 0.4.0"

    lateinit var evalBox: EvalBox
    lateinit var luaOptionsButton: Button
    val options = TanksLuaOptions()
}

var lastScreen: Screen? = null

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

        verifyDirectoryStructure()

        TanksLua.options.load()
        if (TanksLua.options.warningSeen) loadLuaExtensions()
    }

    fun screenChanged(old: Screen?, new: Screen) {
        if (new is ScreenGame) {
            if (!(TanksLua.options.levelScriptsEnabled && new.name != null)) return

            try {
                tryLoadingLevelScript(new.name.replace(".tanks",""))
            } catch (e: LevelScriptLoadException) {
                Notification("Level script failed to load: ${e.message}", NotificationType.ERR)
            }

            return
        }

        if (old is ScreenGame) {
            clearCurrentLevelScript()
        }
    }

    override fun update() {
        if (!TanksLua.options.warningSeen && Game.screen !is ScreenLuaWarning) {
            Game.screen = ScreenLuaWarning()
        }

        if (TanksLua.options.evalBoxEnabled) TanksLua.evalBox.update()
        val screen = Game.screen

        if (screen is ScreenOptions) TanksLua.luaOptionsButton.update()
        updateNotifications()

        val levelScript = currentLevelScript
        if (levelScript != null) {
            try {
                levelScript.updateFunction?.call(Panel.frameFrequency)
            } catch (e: LuaException) {
                Notification("The level script ran into an issue updating: ${e.message}", NotificationType.ERR)
                clearCurrentLevelScript()
            }
        }
        for (extension in loadedExtensions) {
            if (extension !is RealLuaExtension) continue

            if (extension.enabled && extension.updateFunction != null) try {
                extension.updateFunction!!.call()
            } catch (e: LuaException) {
                Notification("Extension ${extension.id}'s update function ran into a problem: ${e.message}", NotificationType.ERR, 1000.0)
                extension.updateFunction = null
            }
        }

        if (screen != lastScreen) screenChanged(lastScreen, screen)

        lastScreen = screen
    }

    override fun draw() {
        if (TanksLua.options.evalBoxEnabled) TanksLua.evalBox.draw()
        drawNotifications()
        if (Game.screen is ScreenOptions) TanksLua.luaOptionsButton.draw()

        val levelScript = currentLevelScript
        if (levelScript != null) {
            try {
                levelScript.drawFunction?.call(Panel.frameFrequency)
            } catch (e: LuaException) {
                Notification("The level script ran into an issue drawing: ${e.message}", NotificationType.ERR)
                clearCurrentLevelScript()
            }
        }

        for (extension in loadedExtensions) {
            if (extension !is RealLuaExtension) continue

            if (extension.enabled && extension.drawFunction != null) try {
                extension.drawFunction!!.call()
            } catch (e: LuaException) {
                Notification("Extension ${extension.id}'s draw function ran into a problem: ${e.message}", NotificationType.ERR)
                extension.drawFunction = null
            }
        }

        Panel.panel.drawMouseTarget()
    }
}

fun main() {
    Tanks.launchWithExtensions(arrayOf("debug"), arrayOf(TanksLuaExtension()), null)
}