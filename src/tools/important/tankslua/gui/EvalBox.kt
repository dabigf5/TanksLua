package tools.important.tankslua.gui

import party.iroiro.luajava.Lua
import party.iroiro.luajava.LuaException
import party.iroiro.luajava.luajit.LuaJit
import party.iroiro.luajava.value.LuaValue
import tanks.Drawing
import tanks.gui.Button
import tanks.gui.TextBox
import tools.important.tankslua.initialize

class EvalBox {
    val luaState: Lua = LuaJit().apply { initialize() }

    val codeBox: TextBox = TextBox(
        Drawing.drawing.interfaceSizeX * 0.5,
        Drawing.drawing.interfaceSizeY * 0.1,
        Drawing.drawing.objWidth * 3,
        Drawing.drawing.objHeight,
        "Lua Code",
        {},
        ""
    ) .apply {
        allowAll = true
        maxChars = Int.MAX_VALUE // who in their right mind is gonna write lua code longer than this
    }
    val evalButton: Button = Button(
        Drawing.drawing.interfaceSizeX * 0.5,
        Drawing.drawing.interfaceSizeY * 0.1 + Drawing.drawing.objHeight * 1.2,
        Drawing.drawing.objWidth,
        Drawing.drawing.objHeight,
        "Evaluate",
        fun() {
            try {
                luaState.load(codeBox.inputText)
            } catch (e: LuaException) {
                Notification("Your code ran into an issue loading: ${e.message}", NotificationType.ERR)
                return
            }

            val function: LuaValue = luaState.get()
            try {
                function.call()
            } catch (e: LuaException) {
                Notification("Your code ran into an issue running: ${e.message}", NotificationType.ERR)
                return
            }

            Notification("Evaluation success!", NotificationType.INFO)
        }
    )

    fun draw() {
        evalButton.draw()
        codeBox.draw()
    }

    fun update() {
        codeBox.update()
        evalButton.update()
    }
}