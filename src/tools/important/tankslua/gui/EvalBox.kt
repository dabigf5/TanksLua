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
    var lastEvalResult: String? = null

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
                val inputText = codeBox.inputText

                val code = if (inputText.startsWith('='))
                    "return "+inputText.substring(1)
                else
                    inputText

                luaState.load(code)
            } catch (e: LuaException) {
                Notification("Your code ran into an issue loading: ${e.message}", NotificationType.ERR)
                return
            }

            val function: LuaValue = luaState.get()

            lastEvalResult = try {
                val results = function.call()

                if (results.size > 0) {
                    results[0].toString()
                } else {
                    "<no return>"
                }
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
        val drawing = Drawing.drawing!!

        if (lastEvalResult != null) {
            drawing.setColor(0.0, 0.0, 0.0)
            drawing.setInterfaceFontSize(20.0)
            drawing.drawInterfaceText(codeBox.posX - codeBox.sizeX/2, codeBox.posY + codeBox.sizeY, lastEvalResult)
        }
    }

    fun update() {
        codeBox.update()
        evalButton.update()
    }
}