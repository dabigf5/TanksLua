# TanksLua
TanksLua is an extension for Tanks: The Crusades which allows for lua scripting.\
please note that this extension is quite early in development, and subject to change

## How-to
all things related to this extension are stored in a special directory, `.tanks/scripts/`
### Level Script Example
a level script is simply a script that runs when a level is loaded\
in order to set up one, you create a lua file in `.tanks/scripts/level/` which is the name of the level, but with `.lua` as the extension instead of `.tanks`

here's an example level script that does completely nothing:
```lua
local lvl = {}

return lvl
```

now here's one that prints "loaded" to the console when it's loaded, and "updated" to the console every frame:
```lua
local lvl = {}

function lvl.onLoad()
    print("loaded")
end

function lvl.onUpdate()
    print("updated")
end

return lvl
```

### Extension Example
this extension supports lua extensions, so you can have extensions inside your extension

in order to create an extension, create a lua file in `.tanks/scripts/extensions/` and name it whatever you want, as long as it ends with `.lua`\
here's an example of an extension that does completely nothing:

```lua
return {
    name = "My Extension",
    authorName = "you",
    description = "a simple extension that does nothing",

    versionMajor = 0, -- semantic versioning is enforced
    versionMinor = 1,
    versionPatch = 0,
}
```

and here's one that prints "loaded" to the console when it's loaded, and "updated" to the console every frame:
```lua
return {
    name = "My Extension",
    authorName = "you",
    description = "a simple extension that does something",

    versionMajor = 0,
    versionMinor = 2,
    versionPatch = 0,
    
    onLoad = function() 
        print("loaded")
    end,

    onUpdate = function()
        print("updated")
    end,
}
```

extensions can also have options, accessible through the Lua Options menu (only booleans are supported right now):
```lua
local enableUpdatePrint
return {
    name = "My Extension",
    authorName = "you",
    description = "a simple extension that does something and has options",

    versionMajor = 0,
    versionMinor = 3,
    versionPatch = 0,
    
    options = {
        enableUpdatePrint = {type="boolean", default=true}
    },
    
    onLoad = function() 
        print("loaded")
    end,
    
    onNewOptions = function(optionsT)
        enableUpdatePrint = optionsT.enableUpdatePrint
    end,

    onUpdate = function()
        if not enableUpdatePrint then return end
        print("updated")
    end,
}
```
