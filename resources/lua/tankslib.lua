tanks = {}

local Notification = java.import("tools.important.tankslua.gui.Notification")
local NotificationType = java.import("tools.important.tankslua.gui.NotificationType")
local Drawing = java.import("tanks.Drawing")
local Game = java.import("tanks.Game")

function tanks.notify(message, duration)
    ensureType("message", "string", message)
    Notification(message, NotificationType.INFO, duration or 200)
end

function tanks.playSound(sound, pitch)
    ensureType("sound", "string", sound)

    Drawing.drawing:playSound(sound, pitch or 1)
end


tanks.isModApi = pcall(function()
    java.import("tanks.ModAPI"):getDeclaredField("version")
end)

tanks.version = Game.version

tanks.game = {}

tanks.game.tileSize = Game.tile_size

function tanks.game.distance(a, b)
    return math.sqrt(tanks.game.sqDistance(a, b))
end

function tanks.game.sqDistance(a, b)
    return (a.posX - b.posX)^2 + (a.posY - b.posY)^2
end

function tanks.game.getMovables()
    return java.luaify(Game.movables)
end

function tanks.game.getPlayerTank()
    return Game.playerTank
end


function tanks.game.playing()
    return
        java.import("tanks.gui.screen.ScreenGame").class:isInstance(Game.screen)
        and not Game.screen.paused
        and Game.screen.playing
        and not Game.playerTank.destroy
end

function tanks.game.allied(a, b)
    return a.team ~= nil and b.team ~= nil and a.team == b.team
end

tanks.input = {}

tanks.input.codes = {}
for _, field in pairs(
    java.luaify(
        java.import("basewindow.InputCodes").class:getFields()
    )
) do
    tanks.input.codes[field:getName()] = field:get(nil)
end

function tanks.input.keyHeld(keyToCheck)
    ensureType("keyToCheck", "number", keyToCheck)

    local keys = java.luaify(Game.game.window.validPressedKeys)
    for _, key in pairs(keys) do
        if key == keyToCheck then return true end
    end
    return false
end

function tanks.input.mouseButtonHeld(buttonToCheck)
    ensureType("buttonToCheck", "number", buttonToCheck)

    local buttons = java.luaify(Game.game.window.validPressedButtons)
    for _, button in pairs(buttons) do
        if button == buttonToCheck then return true end
    end
    return false
end



tanks.drawing = {}

tanks.drawing.objWidth = Drawing.drawing.objWidth
tanks.drawing.objHeight = Drawing.drawing.objHeight
tanks.drawing.objXSpace = Drawing.drawing.objXSpace
tanks.drawing.objYSpace = Drawing.drawing.objYSpace

function tanks.drawing.color(r, g, b, a)
    ensureType("r", "number", r)
    ensureType("g", "number", g)
    ensureType("b", "number", b)

    if not a then
        Drawing.drawing:setColor(r, g, b)
        return
    end
    Drawing.drawing:setColor(r, g, b, a)
end


function tanks.drawing.oval(x, y, width, height, forInterface, mode)
    ensureType("x", "number", x)
    ensureType("y", "number", y)
    ensureType("width", "number", width)
    ensureType("height", "number", height)
    ensureType("forInterface", "boolean", forInterface)
    ensureType("mode", "string", mode)


    if forInterface then
        if mode == "fill" then
            Drawing.drawing:fillInterfaceOval(x, y, width, height)
        elseif mode == "line" then
            Drawing.drawing:drawInterfaceOval(x, y, width, height)
        end
        return
    end

    if mode == "fill" then
        Drawing.drawing:fillOval(x, y, width, height)
    elseif mode == "line" then
        Drawing.drawing:drawOval(x, y, width, height)
    end
end

function tanks.drawing.fontSize(newSize, forInterface)
    if not newSize then -- get
        if forInterface then
            return Drawing.drawing.fontSize / 36.0 * Drawing.drawing.interfaceScale
        end
        return Drawing.drawing.fontSize
    end

    -- set
    if forInterface then
        Drawing.drawing:setInterfaceFontSize(newSize)
        return
    end
    Drawing.drawing:setFontSize(newSize)
end

function tanks.drawing.text(x, y, text, forInterface)
    ensureType("x", "number", x)
    ensureType("y", "number", y)
    ensureType("text", "string", text)

    if forInterface then
        Drawing.drawing:drawInterfaceText(x, y, text)
        return
    end

    Drawing.drawing:drawText(x, y, text)
end

function tanks.drawing.size(forInterface)
    if forInterface then
        return Drawing.drawing.interfaceSizeX, Drawing.drawing.interfaceSizeY
    end

    return Drawing.drawing.sizeX, Drawing.drawing.sizeY
end


tanks.multiplayer = {}

function tanks.multiplayer.isServer()
    return java.import("tanks.gui.screen.ScreenPartyHost").isServer
end

function tanks.multiplayer.isClient()
    return java.import("tanks.gui.screen.ScreenPartyLobby").isClient
end
