tanks = {}

local Notification = java.import("tools.important.tankslua.gui.Notification")
local NotificationType = java.import("tools.important.tankslua.gui.NotificationType")
local Drawing = java.import("tanks.Drawing")
local Game = java.import("tanks.Game")

function tanks.notify(message, duration)
    Notification(message, NotificationType.INFO, duration or 200)
end

function tanks.playSound(sound, pitch)
    Drawing.drawing:playSound(sound, pitch or 1)
end



tanks.game = {}
tanks.game.version = Game.version
tanks.game.isModApi = pcall(function()
    java.import("tanks.ModAPI"):getDeclaredField("version")
end)


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
    local keys = java.luaify(Game.game.window.validPressedKeys)
    for _, key in pairs(keys) do
        if key == keyToCheck then return true end
    end
    return false
end

function tanks.input.mouseButtonHeld(buttonToCheck)
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
    if not a then
        Drawing.drawing:setColor(r, g, b)
        return
    end
    Drawing.drawing:setColor(r, g, b, a)
end


function tanks.drawing.oval(x, y, width, height, drawMode, coordinateMode)
    if coordinateMode == "interface" then
        if drawMode == "fill" then
            Drawing.drawing:fillInterfaceOval(x, y, width, height)
        elseif drawMode == "line" then
            Drawing.drawing:drawInterfaceOval(x, y, width, height)
        end
        return
    end

    if coordinateMode == "game" then
        if drawMode == "fill" then
            Drawing.drawing:fillOval(x, y, width, height)
        elseif drawMode == "line" then
            Drawing.drawing:drawOval(x, y, width, height)
        end
        return
    end
end

function tanks.drawing.rect(x, y, width, height, drawMode, coordinateMode)
    if coordinateMode == "interface" then
        if drawMode == "fill" then
            Drawing.drawing:fillInterfaceRect(x, y, width, height)
        elseif drawMode == "line" then
            Drawing.drawing:drawInterfaceRect(x, y, width, height)
        end
        return
    end

    if coordinateMode == "game" then
        if drawMode == "fill" then
            Drawing.drawing:fillRect(x, y, width, height)
        elseif drawMode == "line" then
            Drawing.drawing:drawRect(x, y, width, height)
        end
        return
    end
end

function tanks.drawing.fontSize(newSize, coordinateMode)
    if not newSize then -- get
        if coordinateMode == "interface" then
            return Drawing.drawing.fontSize / 36.0 * Drawing.drawing.interfaceScale
        end
        if coordinateMode == "game" then
            return Drawing.drawing.fontSize
        end
        return nil
    end

    -- set
    if coordinateMode == "interface" then
        Drawing.drawing:setInterfaceFontSize(newSize)
        return
    end
    if coordinateMode == "game" then
        Drawing.drawing:setFontSize(newSize)
        return
    end

    return nil
end

function tanks.drawing.text(x, y, text, coordinateMode)
    if coordinateMode == "interface" then
        Drawing.drawing:drawInterfaceText(x, y, text)
        return
    end

    if coordinateMode == "game" then
        Drawing.drawing:drawText(x, y, text)
        return
    end
end

function tanks.drawing.size(coordinateMode)
    if coordinateMode == "interface" then
        return Drawing.drawing.interfaceSizeX, Drawing.drawing.interfaceSizeY
    end

    if coordinateMode == "game" then
        return Drawing.drawing.sizeX, Drawing.drawing.sizeY
    end
end



tanks.multiplayer = {}

function tanks.multiplayer.isServer()
    return java.import("tanks.gui.screen.ScreenPartyHost").isServer
end

function tanks.multiplayer.isClient()
    return java.import("tanks.gui.screen.ScreenPartyLobby").isClient
end
