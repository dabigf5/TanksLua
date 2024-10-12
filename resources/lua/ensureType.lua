-- TODO: make this support __jobject__ and __jclass__

function ensureType(name, typename, v)
    local vtype = type(v)
    if vtype ~= typename then
        error(name.." is wrong type (expected "..typename..", got "..vtype..")", 0)
    end
end