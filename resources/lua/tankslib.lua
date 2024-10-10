tanks = {}

local Notification = java.import("tools.important.tankslua.gui.Notification")
local NotificationType = java.import("tools.important.tankslua.gui.NotificationType")
local Drawing = java.import("tanks.Drawing")
local Game = java.import("tanks.Game")

local function ensureType(name, typename, v)
    local vtype = type(v)
    if vtype ~= typename then
        error(name.." is wrong type (expected "..typename..", got "..vtype..")", 0)
    end
end

function tanks.notify(message, duration)
    ensureType("message", "string", message)
    Notification(message, NotificationType.INFO, duration or 200)
end


tanks.isModApi = pcall(function()
    java.import("tanks.ModAPI"):getDeclaredField("version")
end)

tanks.version = Game.version

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
    end

    Drawing.drawing:drawText(x, y, text)
end

function tanks.drawing.size(forInterface)
    if forInterface then
        return Drawing.drawing.interfaceSizeX, Drawing.drawing.interfaceSizeY
    end

    return Drawing.drawing.sizeX, Drawing.drawing.sizeY
end