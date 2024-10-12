local loaders = package.loaders

for k in pairs(loaders) do
    loaders[k] = nil
end

if not readFile then return end

loaders[1] = function(s)
    ensureType("s", "string", s)
    local searchPath = s:gsub("%.", "/")..".lua"

    local content = readFile(searchPath)
    if not content then
        return "no "..searchPath.." in repository"
    end

    return (load or loadstring)(content)
end