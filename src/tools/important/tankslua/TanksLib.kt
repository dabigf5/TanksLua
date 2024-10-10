package tools.important.tankslua

import party.iroiro.luajava.JFunction
import party.iroiro.luajava.Lua
import tanks.Drawing
import tanks.Game
import tanks.ModAPI
import tools.important.tankslua.gui.Notification
import tools.important.tankslua.gui.NotificationType

fun openTanksLib(luaState: Lua) {
    luaState.createTable(0, 0)
    val tanksLib = luaState.get()

    tanksLib.set("notify", JFunction(fun(state): Int {
        val argCount = state.top

        if (argCount < 1) luaError("Not enough arguments!")

        val message: String = state.toString(1) ?: luaError("Message is wrong type!")

        val duration: Double? = if (argCount >= 2) {
            if (!state.isNumber(2)) luaError("Duration is wrong type!")
            state.toNumber(2)
        } else null

        Notification(message, NotificationType.INFO, duration ?: 200.0)
        return 0
    }))

    tanksLib.set("version", Game.version)

    tanksLib.set("isModApi", try {
        ModAPI::class.java.getDeclaredField("version")
        true
    } catch (_: NoSuchFieldException) {
        false
    })

    luaState.createTable(0, 0)
    val tanksDrawingLib = luaState.get()

    tanksDrawingLib.set("color", JFunction(fun(state): Int {
        val argCount = state.top
        if (argCount < 3) luaError("Not enough arguments!")

        val r = if (state.isNumber(1)) state.toNumber(1) else luaError("R is wrong type!")
        val g = if (state.isNumber(2)) state.toNumber(2) else luaError("G is wrong type!")
        val b = if (state.isNumber(3)) state.toNumber(3) else luaError("B is wrong type!")

        val opacity: Double? = if (argCount >= 4) {
            if (state.isNumber(4)) state.toNumber(4) else luaError("Opacity is wrong type!")
        } else null

        state.pop(3 +
                (if (opacity != null) 1 else 0)
        )

        Drawing.drawing.setColor(r,g,b,opacity ?: 255.0)

        return 0
    }))

    tanksDrawingLib.set("size", JFunction(fun(state): Int {
        val argCount = state.top

        val interfaceMode = if (argCount >= 1) {
            if (state.isBoolean(1)) state.toBoolean(1) else luaError("Interface mode is wrong type!")
        } else null
        state.pop(1)

        if (interfaceMode == true) {
            state.push(Drawing.drawing.interfaceSizeX)
            state.push(Drawing.drawing.interfaceSizeY)
            return 2
        }
        state.push(Drawing.drawing.sizeX)
        state.push(Drawing.drawing.sizeY)
        return 2
    }))

    tanksDrawingLib.set("oval", JFunction(fun(state): Int {
        val argCount = state.top
        if (argCount < 6) luaError("Not enough arguments!")

        val interfaceMode = if (state.isBoolean(1)) state.toBoolean(2) else luaError("Interface mode is wrong type!")
        val x = if (state.isNumber(2)) state.toNumber(2) else luaError("X is wrong type!")
        val y = if (state.isNumber(3)) state.toNumber(3) else luaError("Y is wrong type!")
        val width = if (state.isNumber(4)) state.toNumber(4) else luaError("Width is wrong type!")
        val height = if (state.isNumber(5)) state.toNumber(5) else luaError("Height is wrong type!")
        val drawMode = if (state.isString(6)) state.toString(6) else luaError("Draw mode is wrong type!")

        state.pop(6)

        if (interfaceMode) {
            if (drawMode == "fill") {
                Drawing.drawing.fillInterfaceOval(x,y,width,height)
            } else if (drawMode == "line") {
                Drawing.drawing.drawInterfaceOval(x,y,width,height)
            }
        } else {
            if (drawMode == "fill") {
                Drawing.drawing.fillOval(x,y,width,height)
            } else if (drawMode == "line") {
                Drawing.drawing.drawOval(x,y,width,height)
            }
        }

        return 0
    }))

    tanksDrawingLib.set("fontSize", JFunction(fun(state): Int {
        val argCount = state.top

        val forInterface = if (argCount >= 1) {
            if (state.isBoolean(1)) state.toBoolean(1) else luaError("Interface mode is wrong type!")
        } else null

        val newSize = if (argCount >= 2) {
            if (state.isNumber(2)) state.toNumber(2) else luaError("New size is wrong type!")
        } else null

        state.pop(
            (if (forInterface != null) 1 else 0) +
                    (if (newSize != null) 1 else 0)
        )

        if (newSize == null) {
            if (forInterface == true) {
                state.push(Drawing.drawing.fontSize / 36.0 * Drawing.drawing.interfaceScale)
            } else {
                state.push(Drawing.drawing.fontSize)
            }
            return 1
        }

        if (forInterface == true) {
            Drawing.drawing.setInterfaceFontSize(newSize)
        } else {
            Drawing.drawing.setFontSize(newSize)
        }

        return 0
    }))

    tanksDrawingLib.set("text", JFunction(fun(state): Int {
        val argCount = state.top

        if (argCount < 4) luaError("Not enough arguments!")

        val forInterface = if (state.isBoolean(1)) state.toBoolean(1) else luaError("Interface mode is wrong type!")
        val x = if (state.isNumber(2)) state.toNumber(2) else luaError("X is wrong type!")
        val y = if (state.isNumber(3)) state.toNumber(3) else luaError("Y is wrong type!")
        val text = if (state.isString(4)) state.toString(4) else luaError("Text is wrong type!")

        if (forInterface) {
            Drawing.drawing.drawInterfaceText(x, y, text)
        } else {
            Drawing.drawing.drawText(x, y, text)
        }

        return 0
    }))

    tanksDrawingLib.set("objYSpace", Drawing.drawing.objYSpace)
    tanksDrawingLib.set("objXSpace", Drawing.drawing.objXSpace)

    tanksLib.set("drawing", tanksDrawingLib)

    luaState.set("tanks", tanksLib)
}