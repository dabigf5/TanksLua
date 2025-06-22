![TanksLua Logo](icon.png)
# TanksLua

TanksLua is an extension for Tanks: The Crusades which allows for Lua scripting.\
Please note that this extension is quite early in development, and highly subject to change.

## How-to
(Information here is outdated for 0.3.0+, will fix at some point)

All things related to this extension are stored in a special directory, `.tanks/scripts/`.
### Level Script Example
A level script is simply a script that runs when a level is loaded.\
In order to set one up, you create a Lua file in `.tanks/scripts/level/` which is the name of the level, but with `.lua` as the extension instead of `.tanks`

Here's an example level script that does completely nothing:
```lua
local level = {}

return level
```

Now here's one that prints "loaded" to the console when it's loaded, and "updated" to the console every frame:
```lua
local level = {}

function level.onLoad()
    print("loaded")
end

function level.onUpdate()
    print("updated")
end

return level
```

### Extension Example
This extension supports Lua extensions, so you can have extensions inside your extension.

In order to create an extension, create a Lua file in `.tanks/scripts/extensions/` and name it whatever you want, as long as it ends with `.lua`.
Here's an example of an extension that does completely nothing:

```lua
local extension = {
    name = "My Extension",
    authorName = "you",
    description = "a simple extension that does nothing",

    versionMajor = 0, -- semantic versioning is enforced
    versionMinor = 1,
    versionPatch = 0,
}

return extension
```

And here's one that prints "loaded" to the console when it's loaded, and "updated" to the console every frame:
```lua
local extension = {
    name = "My Extension",
    authorName = "you",
    description = "a simple extension that does something",

    versionMajor = 0,
    versionMinor = 2,
    versionPatch = 0,
}

function extension.onLoad()
    print("loaded")
end

function extension.onUpdate()
        print("updated")
end

return extension
```

Extensions can also have options, accessible through the Lua Options menu (only booleans are supported right now):
```lua
local enableUpdatePrint

local extension = {
    name = "My Extension",
    authorName = "you",
    description = "a simple extension that does something and has options",

    versionMajor = 0,
    versionMinor = 3,
    versionPatch = 0,
    
    options = {
        enableUpdatePrint = {type="boolean", default=true}
    },
}

function extension.onLoad() 
    print("loaded")
end

function extension.onNewOptions(optionsT)
        enableUpdatePrint = optionsT.enableUpdatePrint
end

function extension.onUpdate()
        if not enableUpdatePrint then return end
        print("updated")
end

return extension
```
