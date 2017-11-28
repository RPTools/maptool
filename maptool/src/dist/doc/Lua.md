# LUA Support for Maptool
## What is Lua?
[Lua](https://Lua.org/) is a small programming language usually embedded in programs (mostly games) 
You can try it online under [https://www.Lua.org/demo.html]
## How does a macro become a Lua macro?
Every input (macro, chatline, eval()) that might be macro code is assumed to be a Lua-macro if it starts with the Lua identifier.
### Lua-Identifier
```lua
--{assert(0, "LUA")}--

```
The identifier has to be followed by a newline
The identifier is both valid LUA and maptool macro language, but prevents the macro interpreter from running it (should LUA not be available) 
## Major changes
### Use print() and println()
Lua macros do not output anything by default. Everything that needs to be written to the chat-box must be printed.
```lua
--{assert(0, "LUA")}-- 
gold = 22 --Comments in Lua start with --
println("You found ", gold, " Gold")
print("Lucky you")
```
println() does the same as print(), but it adds an HTML-Linebreak to the output.

Writing to the default Lua output stream has the same effect, however the Lua io library is currently disabled for security concerns.
### There are now objects
Lua allows for objects, and under with this in mind, many of the functions and special variables have been grouped in to objects like [token](#token) for a single token, or the [campaign](#campaign) global for the campaign properties

### String-Lists, String-Property-Lists und JSON-Variables
These should not be used in LUA, there are functions like [toJSON](#tojson), [fromJSON](#fromjson), [toStr](#tostr) and [fromStr](#fromstr) that provide an interface to normal macros, However, all functions that can be called from Lua use Lua-objects (specifically Luatable). These have direct support in Lua, and can be nested without any hassle. 

In fact JSON-Objects and Arrays are always treated as Text in Lua and use their special-ness they have in the normal macro language, making them slower to use in Lua.

#### LuaTable as array (replaces String-Lists and JSON-Arrays)
```lua
--{assert(0, "LUA")}--
local array = {} -- new array (table), similar to json actually, just different brackets
local filledarray = {1, 2, "Text, with a comma"} -- Array with some contents
for i=1, 2000 do -- 2000 iterations, so quick you won't notice the difference
  array[i] = i * 10
end
for j=1, #array do --#array is the length of the LuaTable named array
  print(array[j], ", ")
end
println()
for index, text in ipairs(filledarray) do --ipairs iterates over the array part of any table
  println(index, " is ", text)
end
```
#### Nested Lists
```lua
--{assert(0, "LUA")}--
local nested = {} -- new array (table), similar to json actually
for i=1, 200 do -- 2000 iterations
  nested[i] = {}
  for j=1, 2000 do -- 2000*200 iterations
    nested[i][j] = i*j
  end
end
for index, array in ipairs(nested) do
  local sum = 0 -- sum is only valid in this loop
  for index2, val in ipairs(array) do -- another 400000 iterations, why not?
    sum = sum + val; --However printing 400k lines is not good for maptool, so we just sum them up
  end
  print(sum, ", ")
end
```
#### LuaTables as dictionaries (replaces string property lists and JSON-Objects)
```lua
--{assert(0, "LUA")}--
local table = {} -- Same as array, since both are tables
local filledtable = {first=1, second=2, ["Some Text"]="Text, with a comma"} -- Table with some contents
table.key = "Value"
local variable = "Text"
table["key with spaces"] = variable
table[variable] = 100
println("Table: ")
for key, value in pairs(table) do --pairs iterates over all elements of the table (array-parts included)
  println(key, " is ", value)
end
println("Filled-Table: ")
for key, value in pairs(filledtable) do
  println(key, " is ", value)
end
```
#### Nested tables
The keys and the values of a table can be anything, so nesting them is quite easy
```lua
--{assert(0, "LUA")}--
local filledtable = {first=1, second=2, ["Some Text"]="Text, with a comma"}
local filledarray = {1, 2, "Text, with a comma"}
local table = {aTable=filledtable}
table.anArray = filledarray
table.moreTable = {1, 4, 9, text="Value"} --mixed table
println(table.aTable.second)
println(table.anArray[3])
for key, value in pairs(table.moreTable) do
  println(key, " is ", value)
end
println("table = ", toJSON(table))
```

### Be careful when saving Lua datastructures in places where they don't belong
Always convert tables and similar to string with  [toJSON](#tojson) and retrieve them back with [fromJSON](#fromjson) when saving values to Token-Properties, calling other (non-Lua) macros with them, they will loose their Lua-ness and be useless.

### Stack size
Lua macros barely use any stacksize, since Lua manages its own stack in normal memory.

### Parsing/Compiling
Lua macros are compiled, so execution times are a lot lower. This is very noticable in big frameworks and when using many loops. For single line commands, Lua is often not necessary and might be slower due to the compile time overhead.

### Functions and require 
Lua allows functions to be defined in the same macro they are used in. The require command can load other macros and treat them as Lua-libraries.

### Nesting Loops
There is no limit on how many IFs and FORs can be nested in Lua.

### macro.args and macro.return
Those variables are automatically converted to and from JSON to Lua-datastructures, the original values can be accessed with this:
```lua
--{assert(0, "LUA")}--
println(macro.args.enemy) --converted from JSON
print(_G["macro.args"]) -- original value: {enemy="Skeleton", count=3}
```

Setting macro.return can be done anywhere like this:
```lua
--{assert(0, "LUA")}--
macro["return"] = {key=value} --return is a keyword in lua, so it must be written that way instead of macro.return
```
However, at the end of a block, lua allows a return statement like this:
```lua
--{assert(0, "LUA")}--
local enemies = _G["Number of Enemies"] --this creates a prompt, because it does not exist
if enemies > 5 then
	return "We are in Danger"
elseif enemies > 0 then
	return  "We can take them"
end
return {opinion = "The coast is clear"} --Lua datastructures are converted as well
```
Using return is the more readable and preferred version

When calling other macros, macro.return is not filled in.

### Case-Sensitive
Lua is case-sensitive, that means for example the functions toJson and toJSON are different functions (if toJson is defined at all). There are parts however that try to work around this, like the token-Object for example.

### Variables
All variables written by Lua are local to Lua, if there is an unknown variable, the MapTool-Variables will be queried.
```lua
--{assert(0, "LUA")}--
println(Strength) --token property or input
```
Writing those Variables back, will not change the token or the MapTool-Variables (used in eval() and such)
```lua
--{assert(0, "LUA")}--
println(Strength) --token property or input
Strength = 10
println(Strength) --Now only accessing the Lua-Variables
println(token.properties.Strength.value) --Unchanged (if it was a token property)
```
If the Maptool-Variables need to be changes, we can use [export](#export) for that.
```lua
--{assert(0, "LUA")}--
println(Strength) --token property or input
export("Strength", 10) --export as Strength
println(Strength) --Since no other Strength, back to token property or MapTool-Variable
println(token.properties.Strength.value) --Changed (if it was a token property)
```
To get the original value back, the current one has to be set to NIL
```lua
--{assert(0, "LUA")}--
println(Strength) --token property or input
Strength = 1000
println(Strength) --Now only accessing the Lua-Variables, but what was the original?
Strength = nil --Set to nil
println(Strength) --now again token property or input
```

### BigDecimal
Lua uses normal integer and floating point values for its numbers, while the Macro-language always uses abritary precision BigDecimal-numbers. Normally this should not make a difference, but this is something to keep in mind when working with huge values.
## Globals
### Libraries
#### bit32
#### package
#### math
#### string
#### table
### print
### println
### fromJSON
### toJSON
### fromStr
### toStr
### encode
### decode
### export
### token
### dice
### selectTokens
### deselectTokens
### tokenProperties
### tokens
### maps
### macro
### chat
### functions
### defineFunction


## Objects


## How do I do in LUA?
### Macro-Functions
#### Macro-Function abort()
This function is part the Macro library
```lua
--{assert(0, "LUA")}--
macro.abort(isGM()) -- abort when not GM (boolean is false)
local enemiesLeft=1;
macro.abort(enemiesLeft) -- abort when enemiesLeft is zero
macro.abort()
```
#### Macro-Functions abs() and absolutevalue()
abs() is part of the defualt [Lua Math library](https://www.lua.org/pil/18.html)
```lua
--{assert(0, "LUA")}--
local value = -13
print(math.abs(value))
```

#### Macro Functions add() and concat()
Lua supports + and .. to add numbers and concatinate strings respektively, however there is no dedicated sum() function. It can be very easily implemented however:
```lua
--{assert(0, "LUA")}--
function sum(...) -- for variable args
   result = 0;
   for k, v in ipairs({...}) do
        result = result + v
    end 
  return result
end

function sumArray(a) -- No varargs, just for arrays
   result = 0;
   for k, v in ipairs(a) do
        result = result + v
    end 
  return result
end

function concat(...) -- For Strings
   result = "";
   for k, v in ipairs({...}) do
        result = result .. v
    end 
  return result
end

println(sum(1,2,3,4))
println(concat(1,2,3,4))
println(sumArray({1,2,3,4})) -- create an Array
println(table.concat({1,2,3,4})) -- table library has a concat for tables
local array = {2,3,4,5,6}
println(sum(table.unpack(array))) -- Convert array to var-args
println(sumArray(array))
println(concat(table.unpack(array))) -- Convert array to var-args
println(table.concat(array)) -- table library has a string-concat for tables
```
#### Marco Functions addAllNPCsToInitiative(), addAllPCsToInitiative() and addAllToInitiative()
These functions are in the tokens library-object
```lua
--{assert(0, "LUA")}--
tokens.addAllNPCsToInitiative()
tokens.addAllPCsToInitiative(false) --don't add again (same as no argument)
tokens.addAllToInitiative(false) --add again, even if already in initiative
```

#### Marco Function addToInitiative()
addToInitiative() is now a method for token-objects, that means they can be called on the current token.
```lua
--{assert(0, "LUA")}--
token.addToInitiative(false, 20) --Global token Object
```
Or on other tokens (Trusted-Macro or Ownership required)
```lua
--{assert(0, "LUA")}--
for index, tok in ipairs(tokens.visible()) do
  tok.addToInitiative(false, index)
end
```

#### Marco Functions arg() and argCount()
These are in the macro library and like macro.args convert the arguments into lua-datastructures should they be JSON
```lua
--{assert(0, "LUA")}--
for i = 1, macro.argCount() do
  println("Arg ", i, " = ", macro.arg(i - 1)) -- arg starts at 0, lua starts at 1
end
```

#### Marco Function assert()
Lua has its own assert statement with almost exactly the same functionality
```lua
--{assert(0, "LUA")}--
assert(isGM(), "Player not GM")
println("Welcome back, Sire")
```

#### Marco Functions avg() and average()
There is no direct function to do this, but it can be easily implemented in lua
```lua
--{assert(0, "LUA")}--
function avg(...) -- for variable args
   result = 0;
   count = 0;
   for k, v in ipairs({...}) do
        result = result + v
        count = count + 1
    end 
  if count > 0 then
    return result / count
  else
    return 0
  end
end

println(avg(1,2,3,4))
local array = {2,3,4,5,6}
println(avg(table.unpack(array))) -- Convert array to var-args
```
#### Marco Functions band() and bitwiseand()
[bit32](https://www.lua.org/manual/5.2/manual.html#6.7) is the Lua Library for bit-manipulation (Currently sadly limited to 32 bits)
```lua
--{assert(0, "LUA")}--
print(bit32.band(5,9))
```

#### Marco Functions bnot() and bitwisenot()
[bit32](https://www.lua.org/manual/5.2/manual.html#6.7) is the Lua Library for bit-manipulation (Currently sadly limited to 32 bits)
```lua
--{assert(0, "LUA")}--
print(bit32.bnot(5))
```

#### Marco Functions bor() and bitwiseor()
[bit32](https://www.lua.org/manual/5.2/manual.html#6.7) is the Lua Library for bit-manipulation (Currently sadly limited to 32 bits)
```lua
--{assert(0, "LUA")}--
print(bit32.bor(5,9))
```

#### Marco Functions bxor() and bitwisexor()
[bit32](https://www.lua.org/manual/5.2/manual.html#6.7) is the Lua Library for bit-manipulation (Currently sadly limited to 32 bits)
```lua
--{assert(0, "LUA")}--
print(bit32.bxor(5,9))
```

#### Marco Function bringToFront()
The function bringToFront() can be called on any token.
```lua
--{assert(0, "LUA")}--
token.bringToFront() -- Current Token
```
On other tokens (Trusted-Macro or Ownership required):
```lua
--{assert(0, "LUA")}--
for index, tok in ipairs(tokens.visible()) do
  tok.bringToFront()
end
```

#### Marco Function broadcast()
The function broadcast() is part of the chat library
```lua
--{assert(0, "LUA")}--
chat.broadcast("Hi, my name is " .. token.name); --All
chat.broadcast("Actually I am " .. token.gm_name, "gm") --All GMs
```

#### Macro Function canSeeToken()
The function canSee() can be called on any token.
```lua
--{assert(0, "LUA")}--
for index, tok in ipairs(tokens.visible()) do
  println("I can See: ", tok.name, " at ", table.concat(token.canSee(tok), ", "))
end
```
#### Macro Functions ceil() and ceiling()
The [Lua Math library](https://www.lua.org/pil/18.html) has a ceil() function
```lua
--{assert(0, "LUA")}--
println(math.ceil(10.2))
```
#### Macro Function clearLights()
The function clearLights() can be called on any token.
```lua
--{assert(0, "LUA")}--
token.clearLights();
```

#### Macro Function closeFrame()
The Frame functions are in the UI-Package
```lua
--{assert(0, "LUA")}--
UI.frame("Test", "<html>Hello World</html>","temporary:1")
println(UI.isFrameVisible("Test"))
UI.resetFrame("Test")
UI.closeFrame("Test")
println(UI.isFrameVisible("Test"))
```

#### Macro Function closeDialog()
The Dialog functions are in the UI-Package
```lua
--{assert(0, "LUA")}--
UI.dialog("Test", "<html>Hello World</html>","width=400")
println(UI.isDialogVisible("Test"))
UI.closeDialog("Test")
println(UI.isDialogVisible("Test"))
```

#### Macro Function copyToken()
This function can be called on any map. It accepts token names, ids or objects as sources
```lua
--{assert(0, "LUA")}--
local list = maps.current.copyToken(token, 2, {name="New Name"})
println(list[1].name)
println(list[2])
local ids = table.map(list, function(t)  --As copyToken returns Tokens, we have to get the ids ourselves if we want them
  return t.id --table.map calls this function once for every element in the list and creates a new list with the result
end)
println(ids[1])
println(ids[2])
```
#### Macro Function countStrProp()
Lua has no dedicated String Property and String List function, they have to be converted with [fromStr](#fromstr) to an acutal Lua-Table
```lua
--{assert(0, "LUA")}--
println(table.length(fromStr("a=blah; b=doh; c=meh")));
println(table.length(fromStr("a=blah, b=doh, c=meh")));
println(table.length(fromStr("a=blah, b=doh, c=meh", nil, ","))); --Change seperator to ","
```
#### Macro Functions countsuccess() and success()
The dice library has all dice roll functions
```lua
--{assert(0, "LUA")}--
println(dice.success(4,10,8)) --4d10s8
```

#### Macro Function createMacro()
This function can be called on any token
```lua
--{assert(0, "LUA")}--
token.createMacro({label = "World 3", command = _MAPTOOL_LUA_HEADER.."\nprint(\"Hello, World\")", group="Auto"})

token.createMacro("World 2",{command = "--{assert(0, "LUA")}--\nprint(\"Hello, World\")", group="Auto"})

local m = token.createMacro("World","--{assert(0, "LUA")}--\nprint(\"Hello, World\")",{group="Auto"})
local index = m.index -- Macro index, as createMacro returns macro-objects
```

#### Macro Function currentToken()
The current token is always the token Object
```lua
/me --{assert(0, "LUA")}--
println("says: \"Hi, my name is ", token.name,"\"")
```

#### Macro Functions d(), dice() and roll()
These functions are part of the dice-library
```lua
--{assert(0, "LUA")}--
println(dice.roll(4,10)) --4d10
```
#### Macro Function decode()
```lua
--{assert(0, "LUA")}--
println(decode("value = a%3Bb; value2 = c"))
```
#### Macro Function defineFunction()
```lua
--{assert(0, "LUA")}--
defineFunction("character.damage", "damage@Lib:General", 0, 1)
defineFunction("character.heal", "heal@Lib:General", 0, 1)
```
#### Macro Function deleteStrProp()
Lua has no functions to modify string properties, so they have to be converted first.
```lua
--{assert(0, "LUA")}--
local prop = "a=blah; b=doh; c=meh", "a"
local table = fromStr(prop)
table.a = nil --delete entry
println(toStr(table))
```
#### Macro Function deselectTokens()
This function is part of the Token-library
```lua
--{assert(0, "LUA")}--
tokens.deselect(token, "Yin", "Bandit Champion") --both token-objects, ids, and names allowed
```
#### Macro Function divide()
Lua supports the '/'-Operator to divide 2 numbers. A function can be easily implemented to divide more
```lua
--{assert(0, "LUA")}--
function divide(...) -- for variable args
  values = {...}
  result = table.remove(values, 1)
  for k, v in ipairs(values) do
       result = result / v
  end 
  return result
end

println(divide(24, 2, 2))
local array = {1024, 2, 4, 8}
println(divide(table.unpack(array))) -- Convert array to var-args
```

#### Macro Function drawVBL()
This function is part of the VBL library
```lua
--{assert(0, "LUA")}--
local rectangle = "{'shape':'rectangle','x':50,'y':50,'w':100,'h':200,'r':45,'fill':1,'thickness':1,'scale':0}" --JSON-String
local cross = "{'shape':'cross','x':-50,'y':-50,'w':50,'h':100,'r':30,'fill':1,'thickness':1,'scale':2}"
local circle = {shape='circle', X=50, Y=100, radius=200, thickness=3, fill=0, sides=12, r=45} -- Lua table
local polygon = "{'shape':'polygon','r':0,'close':1,'thickness':10,'points':[{'x':0,'y':0},{'x':200,'y':200},{'x':150,'y':10}]}"
VBL.draw(rectangle, cross, circle, polygon) --No array needed. convert arrays with table.unpack(objectarray)
```

#### Macro Function drop()
This function is part of the dice-library
```lua
--{assert(0, "LUA")}--
println(dice.drop(4,10,2)) --4d10d2
```

#### Macro Function encode()
```lua
--{assert(0, "LUA")}--
println("value = " .. encode("a;b") .. "; value2 = c")
```

#### Macro Function endsWith()
endsWith() has been added to the string library
```lua
--{assert(0, "LUA")}--
println(string.endsWith("This is a test","test"))
```
Since the string libraray is a metatable for all strings, this creates the same result:
```lua
--{assert(0, "LUA")}--
local str = "This is a test"
println(str:endsWith("test")) -- str:endsWith(... is the same as str.endsWith(str, ...
```
#### Macro Function eraseVBL()
This function is part of the VBL library
```lua
--{assert(0, "LUA")}--
local rectangle = "{'shape':'rectangle','x':50,'y':50,'w':100,'h':200,'r':45,'fill':1,'thickness':1,'scale':0}" --JSON-String
local cross = "{'shape':'cross','x':-50,'y':-50,'w':50,'h':100,'r':30,'fill':1,'thickness':1,'scale':2}"
local circle = {shape='circle', X=50, Y=100, radius=200, thickness=3, fill=0, sides=12, r=45} -- Lua table
local polygon = "{'shape':'polygon','r':0,'close':1,'thickness':10,'points':[{'x':0,'y':0},{'x':200,'y':200},{'x':150,'y':10}]}"
VBL.erase(rectangle, cross, circle, polygon) --No array needed. convert arrays with table.unpack(objectarray)
```
### Macro Function eval()
This function can be used as normal, but for variables to be available in the macro code, they have to be exported first
endsWith() has been added to the string library
```lua
--{assert(0, "LUA")}--
export("Bonus", 10)
local result = eval("2d10 + Bonus")
println(result)
```
The result is automatically converted from JSON, the raw output is also returned as a second result, furthermore, arguments are supported and converted to JSON.
```lua
--{assert(0, "LUA")}--
local table = {a="b"}
local result, rawresult = eval('json.set(arg(0),"b","c")', table) -- Lua multiple results: https://www.lua.org/pil/5.1.html
println(result.b)
println(rawresult)
```

### Macro Function evalMacro(), execMacro() and json.evaluate()
These functions are part of the macro library as macro.exec() and macro.eval(). macro.exec() does not allow access to the original variables, otherwise they are the same.
For macro.eval(), named variables have be be exported first before they can be seen by the evaluated code

They always return 3 values, the first is macro.result converted from JSON. The last one is the same, but not converted. The second one is the output created by the macro.
```lua
--{assert(0, "LUA")}--
export("Bonus", 10)
local result, output, rawresult = macro.eval("[t: macro.return = 2d10 + Bonus]") 
println(result)
println(output)
```
You can add parameters to the call that are converted to JSON
```lua
--{assert(0, "LUA")}--
local table = {a="b", d = {1,2,3}} -- LUA table
local result, output, rawresult = macro.exec('[h: macro.return = json.set(arg(0),"b","c")][r:json.get(macro.return,"d")]', table)
println(result.b)
println(output)
println(rawresult)
```
You can also run LUA code:
```lua
--{assert(0, "LUA")}--
local table = {a="b", d = {1,2,3}} -- LUA table
local result, output, rawresult = macro.exec(_LUA_HEADER..'local r=macro.arg(0); r.b="c";print(toJSON(r.d)); return r', table)
println(result.b)
println(output)
println(rawresult)
```
If you give these functions a table of strings, every entry of that table will be evaluated, like json.evaluate:
```lua
--{assert(0, "LUA")}--
export("Bonus", 10)
local code = {
	attack="[t: macro.return = 1d20 + Bonus]",
	damage=_LUA_HEADER.."damage=dice.roll(1,6); print(damage, \" massive damage\"); return damage"
}
local result, output, rawresult = macro.eval(code) 
println(result.attack)
println(output.attack)
println(result.damage)
println(output.damage)
```
### Macro Function execLink()
execLink() is part of the marco library
```lua
--{assert(0, "LUA")}--
token.createMacro('link','Called');
local link = macro.linkText("link@TOKEN", "all")
print(link)
macro.execLink(link)
macro.execLink(link, true) -- deferred
```
#### Macro Function explode()
This function is part of the dice-library
```lua
--{assert(0, "LUA")}--
println(dice.explode(4,10)) --4d10e
```
#### Macro Function explodingSuccess()
This function is part of the dice-library
```lua
--{assert(0, "LUA")}--
println(dice.explodingSuccess(4,2,8)) --4d10es8
```
#### Macro Functions exposeFOW() and exposePCOnlyArea()
These can be called on any map
```lua
--{assert(0, "LUA")}--
maps.current.exposeFOW()
maps.visible["Grasslands"].exposePCOnlyArea()
```
#### Macro Functions f() and fudge()
This function is part of the dice-library
```lua
--{assert(0, "LUA")}--
println(dice.fudge(4)) --4df
```
#### Macro Functions findToken()
In a trusted Macro, tokens.resolve() can be used for this purpose
```lua
--{assert(0, "LUA")}--
println(tokens.resolve("Test").gm_name) 
```
#### Macro Function floor()
The [Lua Math library](https://www.lua.org/pil/18.html) has a floor() function
```lua
--{assert(0, "LUA")}--
println(math.floor(10.2))
```
#### Macro Function formatStrProp()
There is no direct function to do this, but it can be done easily in LUA
```lua
--{assert(0, "LUA")}--
function formatTable(t, listFormat, entryFormat, seperator) 
  local values = {}
  for key, value in pairs(t) do
    table.insert(values, entryFormat:replace("%key", key):replace("%value", value))
  end
  return listFormat:replace("%list",table.concat(values, seperator))
end

props = "Strength=14 ; Constitution=8 ; Dexterity=13 ; Intelligence=4 ; Wisdom=18 ; Charisma=9"
println(formatTable(fromStr(props), "<table border=1>%list</table>", "<tr> <td><b>%key</b></td> <td>%value</td> </tr>", "")) 
--normally Lua-Tables are unsorted, but fromStr() keeps the order from the String Property
```

#### Macro Function getAllMapNames()
There is no direct function to do this, but it can be done easily in LUA using the maps library
```lua
--{assert(0, "LUA")}--
function getAllMapNames() 
  local values = {}
  for name, map in pairs(maps.all) do
    table.insert(values, name)
  end
  return values
end

println(toJSON(getAllMapNames()))
```
But you probably want to do something with the map:
```lua
--{assert(0, "LUA")}--
for name, map in pairs(maps.all) do --Get all Map objects
  println("Map ", name, " is ", map.visible and "visible" or "invisible")
end
```

#### Macro Function getAllMapNames()
This value is saved in the table chat.players
```lua
--{assert(0, "LUA")}--
for index, player in ipairs(chat.players) do
  println(player, " is logged in") --do something with a player
end

println(toStr(chat.players, ", ")) --to String list
```

#### Macro Function getAllPropertyNames()
There is no direct function to do this, but it can be done easily in LUA using the campaign table
```lua
--{assert(0, "LUA")}--
function getAllPropertyNames(group)
  local source = campaign.allTokenProperties
  if group ~= nil then
    source = campaign.tokenProperties[group]
  end
  local values = {}
  if source then
    for index, prop in ipairs(source) do
      table.insert(values, prop.name)
    end
  end
  return values
end

println(toJSON(getAllPropertyNames()))
println(toStr(getAllPropertyNames("PC")))
```
But you probably want to do something with the properties:
```lua
--{assert(0, "LUA")}--
for index, prop in ipairs(campaign.tokenProperties["NPC"]) do --or campaign.allTokenProperties
  println(prop.name, " is ", prop.default)
end
```

#### Macro Function getBar()
The bars are part of any Token
```lua
--{assert(0, "LUA")}--
println ("Health: ", token.bars["Health"] * 100, "%") --careful: "health" does not work, case-sensitive
```
#### Macro Function getCurrentInitiative()
This information is in the initiative library.
This can be run in an untrusted macro, since getInitiativeList() returns the same information from an untrusted macro as well.
```lua
--{assert(0, "LUA")}--
println ("Current: ", initiative.current)
```
#### Macro Function getCurrentMapName()
The current map can be gotten from the Maps library, it has a name attribute which contains the name
```lua
--{assert(0, "LUA")}--
println ("Current Map: ", maps.current.name) --map.current is the current Map object
```
#### Macro Function getDistance()
The getDistance function can be called on any token
```lua
--{assert(0, "LUA")}--
println ("Distance: ", token.getDistance("Bandit Champion", true, "NO_GRID"))
println ("Distance: ", token.getDistance(tokens.resolve("Bandit Champion"), false))
```
#### Macro Function getDistanceXY()
The getDistance function can be called on any token, it can also work with grid coordinates
```lua
--{assert(0, "LUA")}--
println ("Distance: ", token.getDistance(10,10))
println ("Distance: ", token.getDistance(10,10, true, "MANHATTAN"))
```

#### Macro Functions getExposedTokenNames() and getExposedTokens()
The tokens library has an exposed() function that collect all exposed tokens, which can be used to create these functions
```lua
--{assert(0, "LUA")}--
function getExposedTokenNames()
  local result = {}
  for index, tok in ipairs(tokens.exposed()) do
    table.insert(result, tok.name)
  end
  return result
end

function getExposedTokens()
  local result = {}
  for index, tok in ipairs(tokens.exposed()) do
    table.insert(result, tok.id)
  end
  return result
end

println(toJSON(getExposedTokenNames()))
println(toStr(getExposedTokens()))
```
Usually one would want to work with the token objects instead
```lua
--{assert(0, "LUA")}--
for index, tok in ipairs(tokens.exposed()) do
  println(tok.name, " is ", tok.label)
end
```

#### Macro Function getFindCount()
The amount of matches is returned as the first value of the strfind()-method of the string-library or any string
```lua
--{assert(0, "LUA")}--
local test =  "this is a test"
local findCount, groupCount, groups = test:strfind("(\\S+)\\s+(\\S+)\\s*")
println("Count: ", findCount)
```
Used as a library function:
```lua
--{assert(0, "LUA")}--
local findCount, groupCount, groups = string.strfind("this is a test", "(\\S+)\\s+(\\S+)\\s*")
println("Count: ", findCount)
```

#### Macro Function getGMName()
The GM-Name is a property of any token
```lua
--{assert(0, "LUA")}--
println ("GM-Name: ", token.gmName)
```
#### Macro Function getGMNotes()
The text of the GM-Notes is a property of any token
```lua
--{assert(0, "LUA")}--
println ("GM-Notes: ", token.gmNotes)
```

#### Macro Functions getGroup(), getGroupCount(), getGroupStart(), getGroupEnd()
The function findstr works slightly differently in LUA, since LUA can return multiple values, everything is returned on the first call

```lua
--{assert(0, "LUA")}--
local test =  "Command-20, Sleight of Hand 10, Knowledge (Arcana) +5, Murder +100"
local findCount, groupCount, groups = test:strfind("([^,]*?)\\s?([-+]?\\d+)(,|\$)")
println("Found: ", findCount, " results with ", groupCount, " Groups each");
for index, match in ipairs(groups) do
  print("<b>Match ", index, "</b>")
  local value, groupStart, groupEnd = match() -- no params or 0 is the 0-group
  println (" from ", groupStart, " to ", groupEnd, " = ", value) -- end is a reserved word in lua
  for i = 1, groupCount do
    local value, groupStart, groupEnd = match(i) -- match is a function that also returns multiple values
    println ("Group ", i, " from ", groupStart, " to ", groupEnd, " = ", value)
  end
end
println("Direct access: ", groups[2](1).."") -- match 2 group 1 only the value
```

#### Macro Function getHalo()
The halo is a property of any token
```lua
--{assert(0, "LUA")}--
println ("Halo: ", token.halo)
```

#### Macro Function getImage()
This function is part of the tokens library
```lua
--{assert(0, "LUA")}--
println("<img src=\"", tokens.image("Image:Test"), "\">")
println("<img src=\"", tokens.image("Image:Test", 100), "\">") --The function allows a size parameter
```

#### Macro Functions getImpersonated() and getImpersonatedName()
The impersonated() function of the tokens libraray returns the currently impersonated token object
```lua
--{assert(0, "LUA")}--
println("Impersonated : ", tokens.impersonated().name) -- getImpersonated()
println("Impersonated ID : ", tokens.impersonated().id) -- getImpersonatedName()
```
If there is doubts whether a token is impersonated, the tokens.impersonated() might return nil, that can be guarded against like this:
```lua
--{assert(0, "LUA")}--
println("Impersonated : ", tokens.impersonated() and tokens.impersonated().name) -- getImpersonated()
println("Impersonated ID : ", tokens.impersonated() and tokens.impersonated().id) -- getImpersonatedName()
```

#### Macro Function getInfo()
This function is available, however it does return LuaTables instead of JSON.
```lua
--{assert(0, "LUA")}--
println(getInfo("debug").java.version)
println(table.indent(getInfo("campaign")):replace("\n","<br>"):replace(" ","&nbsp;"))
```

#### Macro Function getInitiative()
The initiative (for the current map) is a property of any token
```lua
--{assert(0, "LUA")}--
println ("My initiative: ", token.initiative)
```

#### Macro Function getIniativeHold()
The initiative hold (for the current map) is a property of any token
```lua
--{assert(0, "LUA")}--
if (token.initiativeHold) then
  println("Holding")
else
  println("Going")
end
```

#### Macro Function getIniativeList()
This information is contained in the tokens property of the initiative library
```lua
--{assert(0, "LUA")}--
println("<pre>", table.indent(initiative.tokens), "</pre>")
```

#### Macro Function getIniativeRound()
This information is contained in the round property of the initiative library
```lua
--{assert(0, "LUA")}--
println("Round ", initiative.round, "!")
```

#### Macro Function getIniativeToken()
This information is contained in the token property of the initiative library. The returned value is a token object
```lua
--{assert(0, "LUA")}--
if initiative.token then
  println(initiative.token.name, ", it is your turn")
else
  println("It's noone's turn right now")
end
```

#### Macro Function getLabel()
The label is a property of any token
```lua
--{assert(0, "LUA")}--
println (token.name, " (", token.label, ")")
```

#### Macro Function getLastPath()
This function getLastPath() can be called on any token
```lua
--{assert(0, "LUA")}--
for pathpoint, pos in ipairs(token.getLastPath()) do
  println(toStr(pos))
end
println(table.indent(token.getLastPath(false)))
```
#### Macro Function getLayer()
The layer is a property of any token
```lua
--{assert(0, "LUA")}--
println ("I am on ", token.layer:lower())
```

#### Macro Function getLibProperty()
The Tokens-library has a getLibProperty(property, [Token]) function that returns a token-property object.
That object has a value property containing the value
```lua
--{assert(0, "LUA")}--
println (tokens.getLibProperty("VERSION","Lib:Library").value)
local prop = tokens.getLibProperty("VERSION","Lib:Library")
println(prop.raw)
println(prop.converted)
```

#### Macro Function getLibPropertyNames()
The Tokens-library has a getLibProperties([Token]) function that returns a table of properties.
```lua
--{assert(0, "LUA")}--
for name, obj in pairs(tokens.getLibProperties("Lib:Library")) do
  println(name)
end
```
With this, a getLibPropertyNames function can be easily defined as such:
```lua
--{assert(0, "LUA")}--
function getLibPropertyNames(tok)
  local result = {}
  for name, obj in pairs(tokens.getLibProperties(tok)) do
    table.insert(result, name)
  end
  return result
end

println(toJSON(getLibPropertyNames("Lib:Library")))
```

#### Macro Function getLights()
Each token-object has a lights-tables that contains all the lights by category
```lua
--{assert(0, "LUA")}--
for name, light in pairs(token.lights.D20) do
  println(name, " is on")
end
```
With this, a getLights function for all lights can be defined as such:
```lua
--{assert(0, "LUA")}--
function getLights()
  local result = {}
  for catname, cat in pairs(token.lights) do
    for lightname, light in pairs(cat) do
      table.insert(result, lightname)
    end
  end
  return result
end

println("Active Lights: ", toJSON(getLights()))
```

#### Macro Function getMacroButtonIndex()
This is a property of the macro libraray
```lua
--{assert(0, "LUA")}--
println(macro.buttonIndex)
```

#### Macro Function getMacroCommand()
This is a property of any macro object
```lua
--{assert(0, "LUA")}--
println(token.macros[1].command)
```

#### Macro Function getMacroGroup()
There is no direct function to do this, but it can be implemented by iterating the token.macros table
```lua
--{assert(0, "LUA")}--
function getMacroGroup(group, tok)
  tok = tok or token
  local result = {}
  for index, macro in pairs(tok.macros) do
    if macro.group == group then
      table.insert(result, macro.label)
    end
  end
  return result
end

println("Macros: ", toJSON(getMacroGroup(""))) --empty group
println("Macros: ", toJSON(getMacroGroup("Group1")))
println("Macros: ", toJSON(getMacroGroup("Group1", tokens.resolve("Test"))))
```

#### Macro Function getMacroIndexes()
There is no direct function to do this, but it can be implemented by iterating the token.macros table
```lua
--{assert(0, "LUA")}--
function getMacroIndices(label, tok)
  tok = tok or token
  local result = {}
  for index, macro in pairs(tok.macros) do
    if macro.label == label then
      table.insert(result, index)
    end
  end
  return result
end

println("Indices: ", toJSON(getMacroIndices("tokenVarTest")))
println("Indices: ", toJSON(getMacroIndices("tokenVarTest", tokens.resolve("Test"))))
```

#### Macro Function getMacroLocation()
This is a property of the macro libraray
```lua
--{assert(0, "LUA")}--
println(macro.location)
```

#### Macro Function getMacroName()
This is a property of the macro libraray
```lua
--{assert(0, "LUA")}--
println(macro.name)
```

#### Macro Function getMacroProps()
A macro object is also a table of the macro properties
```lua
--{assert(0, "LUA")}--
println(toJSON(token.macros[1]))
println(token.macros[1].label)
```

#### Macro Function getMacros()
There is no direct function to do this, but it can be implemented by iterating the token.macros table
```lua
--{assert(0, "LUA")}--
function getMacros(tok)
  tok = tok or token
  local result = {}
  for index, macro in pairs(tok.macros) do
    table.insert(result, macro.label)
  end
  return result
end

println("Macros: ", toJSON(getMacros())) --current Token
println("Macros: ", toJSON(getMacros(tokens.resolve("Test"))))
```

#### Macro Function getMapVisible()
This is a property of a map
```lua
--{assert(0, "LUA")}--
println(maps.current.visible)
println(maps.all["Grasslands"].visible)
println(maps.visible["Grasslands"]~=nil)
```

#### Macro Function getMatchingLibProperties()
The Tokens-library has a getMatchingLibProperties(Token, Pattern) function that returns a table of matching properties.
```lua
--{assert(0, "LUA")}--
for name, obj in pairs(tokens.getMatchingLibProperties("Lib:Library", "Weapon.*")) do
  println(name)
end
```
The matching ignores the case by default, so to enable case-sensitivity, the pattern (?-i) needs to be added to the front
```lua
--{assert(0, "LUA")}--
for name, obj in pairs(tokens.getMatchingLibProperties("Lib:Library", "(?-i)Weapon.*")) do
  println(name)
end
``` 

With this, a getMatchingLibPropertiesNames function can be easily defined as such:
```lua
--{assert(0, "LUA")}--
function getMatchingLibPropertiesNames(pattern, tok)
  local result = {}
  for name, obj in pairs(tokens.getMatchingLibProperties(tok, pattern)) do
    table.insert(result, name)
  end
  return result
end

println(toJSON(getMatchingLibPropertiesNames("Weapon.*", "Lib:Library")))
println(toJSON(getMatchingLibPropertiesNames("(?-i)Weapon.*", "Lib:Library")))
```

#### Macro Function getMatchingProperties()
Each Token has a matchingProperties(Pattern) function that returns a table of matching properties.
```lua
--{assert(0, "LUA")}--
for name, obj in pairs(token.matchingLibProperties("Weapon.*")) do
  println(name)
end
```
The matching ignores the case by default, so to enable case-sensitivity, the pattern (?-i) needs to be added to the front
```lua
--{assert(0, "LUA")}--
for name, obj in pairs(token.matchingLibProperties("(?-i)Weapon.*")) do
  println(name)
end
```
There is also a getMatchingProperties() function, that returns just the name
```lua
--{assert(0, "LUA")}--
println(toJSON(token.getMatchingProperties("Weapon.*"))) --Names in lowercase
println(toJSON(token.getMatchingProperties("Weapon.*", true))) --Raw name, case sensitiv matching
```

#### Macro Function getMoveCount()
This method is part of any token
```lua
--{assert(0, "LUA")}--
println(token.getMoveCount())
```

#### Macro Function getName()
The name is a token property of any token
```lua
--{assert(0, "LUA")}--
println(token.name)
println(tokens.impersonated().name) -- The same, unless token got changed
```

#### Macro Function getNotes()
The name is a token property of any token
```lua
--{assert(0, "LUA")}--
println(token.notes)
```

#### Macro Functions getNPCNames() and getNPC()
The tokens library has an npc() function that collect all NPCs, which can be used to create these functions
```lua
--{assert(0, "LUA")}--
function getNPCNames()
  local result = {}
  for index, tok in ipairs(tokens.npc()) do
    table.insert(result, tok.name)
  end
  return result
end

function getNPC()
  local result = {}
  for index, tok in ipairs(tokens.npc()) do
    table.insert(result, tok.id)
  end
  return result
end

println(toJSON(getNPCNames()))
println(toStr(getNPC()))
```
Usually one would want to work with the token objects instead
```lua
--{assert(0, "LUA")}--
for index, tok in ipairs(tokens.npc()) do
  println(tok.name, " is ", tok.label)
end
```

#### Macro Functions getOwnedNames() and getOwned()
The tokens library has an ownedBy() function that collect all tokens owned by a player, which can be used to create these functions
```lua
--{assert(0, "LUA")}--
function getOwnedNames(player)
  local result = {}
  for index, tok in ipairs(tokens.ownedBy(player)) do
    table.insert(result, tok.name)
  end
  return result
end

function getOwned(player)
  local result = {}
  for index, tok in ipairs(tokens.ownedBy(player)) do
    table.insert(result, tok.id)
  end
  return result
end

println(toJSON(getOwnedNames(chat.player)))
println(toStr(getOwned(chat.player)))
```
Usually one would want to work with the token objects instead
```lua
--{assert(0, "LUA")}--
for index, tok in ipairs(tokens.ownedBy(chat.player)) do
  println(tok.name, " is ", tok.label)
end
```

#### Macro Function getOwnerOnlyVisible()
This flag is a token property of any token
```lua
--{assert(0, "LUA")}--
println(token.ownerOnlyVisible)
```

#### Macro Function getOwners()
This method is part of any token and returns a table of owners
```lua
--{assert(0, "LUA")}--
for index, owner in ipairs(token.getOwners()) do
  println(owner)
end
```

#### Macro Functions getPCNames() and getPC()
The tokens library has an pc() function that collect all PCs, which can be used to create these functions
```lua
--{assert(0, "LUA")}--
function getPCNames()
  local result = {}
  for index, tok in ipairs(tokens.pc()) do
    table.insert(result, tok.name)
  end
  return result
end

function getPC()
  local result = {}
  for index, tok in ipairs(tokens.pc()) do
    table.insert(result, tok.id)
  end
  return result
end

println(toJSON(getPCNames()))
println(toStr(getPC()))
```
Usually one would want to work with the token objects instead
```lua
--{assert(0, "LUA")}--
for index, tok in ipairs(tokens.pc()) do
  println(tok.name, " is ", tok.label)
end
```

#### Macro Function getPlayerName()
The player name can be retrieved from the chat library
```lua
--{assert(0, "LUA")}--
println(chat.player)
chat.broadcast("Hello, I'm "..chat.player..". Nice to meet you.")
```

#### Macro Function getProperty(), getRawProperty() and getPropertyDefault()
The Properties are mapped in the properties table of any token. Each Property has multiple options, like the value, default and name. There is also converted which converts any JSON content in the property into LUA objects
```lua
--{assert(0, "LUA")}--
token.properties.HP.value = 20
println(token.properties.HP.raw)
println(token.properties.HP.value)
println(token.properties.HP.default)
token.properties.HP.reset()
println(token.properties.HP.value)
token.properties.HP.value = "{a: \"b\"}"
println(token.properties.HP.converted.a)
token.properties.HP.value = "{1d6}"
println(token.properties.HP.raw)
println(token.properties.HP.value)
```

#### Macro Function getPropertyNames() and getPropertyNamesRaw()
There are no direct functions for this, however all properties can be listed by iterating through token.properties

```lua
--{assert(0, "LUA")}--
for key, value in pairs(token.properties) do
  println(key, " = ", value, " object = ", token.properties[key])
end
```

Functions to replace these can be easily created
```lua
--{assert(0, "LUA")}--
function getPropertyNames(tok)
  tok = tok or token
  local result = {}
  for key in pairs(tok.properties) do
    table.insert(result, key:lower())
  end
  return result
end

function getPropertyNamesRaw(tok)
  tok = tok or token
  local result = {}
  for key in pairs(tok.properties) do
    table.insert(result, key)
  end
  return result
end

println(toStr(getPropertyNamesRaw()))
println(toStr(getPropertyNames()))
println(toStr(getPropertyNames(token)))
println(toStr(token.getMatchingProperties(".*")))
println(toStr(token.getMatchingProperties(".*", true)))
```

#### Macro Function getPropertyType()
The Property Type is a property of any token:
```lua
--{assert(0, "LUA")}--
println(token.propertyType)
```

#### Macro Function getSelected() and getSelectedNames()
The tokens library has an selected() function that collect all selected tokens, which can be used to create these functions
```lua
--{assert(0, "LUA")}--
function getSelectedNames()
  local result = {}
  for index, tok in ipairs(tokens.selected()) do
    table.insert(result, tok.name)
  end
  return result
end

function getSelected()
  local result = {}
  for index, tok in ipairs(tokens.selected()) do
    table.insert(result, tok.id)
  end
  return result
end

println(toJSON(getSelectedNames()))
println(toStr(getSelected()))
```
Usually one would want to work with the token objects instead
```lua
--{assert(0, "LUA")}--
for index, tok in ipairs(tokens.selected()) do
  println(tok.name, " is ", tok.label)
end
```

#### Macro Function getSightType()
The Sight Type is a property of any token:
```lua
--{assert(0, "LUA")}--
println(token.sightType)
```

#### Macro Function getSize()
The Size is a property of any token:
```lua
--{assert(0, "LUA")}--
println(token.size)
```

#### Macro Function getSpeech() and getSpeechNames()
The speech table of a token contains this information
```lua
--{assert(0, "LUA")}--
for name, value in pairs(token.speech) do
  println(name, " says: ", value)
end
```

The functions can be created as such:

```lua
--{assert(0, "LUA")}--
function getSpeech(name, tok)
  tok = tok or token
  return tok.speech[name]
end

function getSpeechNames(tok)
  tok = tok or token
  local result = {}
  for key in pairs(tok.speech) do
    table.insert(result, key)
  end
  return result
end

println(toStr(getSpeechNames()))
println(getSpeech(name))
```

#### Macro Function getState()
The states are a table on any token
```lua
--{assert(0, "LUA")}--
println(token.states.Bloodied)
```

#### Macro Function getStateImage()
The state image can be extracted from the campaign properties
```lua
--{assert(0, "LUA")}--
println("<img src = \"", campaign.allStates.Bloodied.image, "\">")
println("<img src = \"", campaign.allStates.Bloodied.image, "-300", "\">")
```

With this, the getStateImage function with scaling can be implemented
```lua
--{assert(0, "LUA")}--
function getStateImage(name, scale)
  local state = campaign.allStates[name]
  if state == nil then error("State not defined") end
  if state.image == nil then error("State is not an Image") end
  if type(scale) == "number" then 
    if scale < 1 then scale = 1 end
    if scale > 500 then scale = 500 end
    return state.image.."-"..math.floor(scale)
  end
  return state.image
end

println("<img src = \"", getStateImage("Bloodied", 300) , "\">")
```

#### Macro Function getStrProp()
Lua has no dedicated String Property and String List function, they have to be converted with [fromStr](#fromstr) to an acutal Lua-Table
```lua
--{assert(0, "LUA")}--
println(fromStr("a=blah; b=doh; c=meh")["a"]);
println(fromStr("a=blah; b=doh; c=meh")["b"]);
println(fromStr("a=blah, b=doh, c=meh", nil, ",")["c"]); --Change seperator to ","
``` 

#### Macro Function getTokenDrawOrder()
The Draw Order is a property of any token:
```lua
--{assert(0, "LUA")}--
println(token.drawOrder)
```

#### Macro Function getTokenFacing()
The Facing is a property of any token:
```lua
--{assert(0, "LUA")}--
println(token.facing)
```

#### Macro Function getTokenGMName()
The GM Name is a property of any token:
```lua
--{assert(0, "LUA")}--
println(token.gmName)
```

#### Macro Function getTokenHalo()
The Halo is a property of any token:
```lua
--{assert(0, "LUA")}--
println(token.halo)
```

#### Macro Function getTokenHalo()
The Halo is a property of any token:
```lua
--{assert(0, "LUA")}--
println(token.halo)
```

#### Macro Function getTokenHandout()
The Handout Picture is a property of any token:
```lua
--{assert(0, "LUA")}--
println(token.handout)
```

#### Macro Function getTokenHandout()
The Handout Picture is a property of any token:
```lua
--{assert(0, "LUA")}--
println(token.handout)
```

#### Macro Function getTokenHeight()
The Height is a property of any token:
```lua
--{assert(0, "LUA")}--
println(token.height)
```

#### Macro Function getTokenImage()
The Image is a property of any token:
```lua
--{assert(0, "LUA")}--
println(token.image)
```

#### Macro Function getTokenLabel()
The Label is a property of any token:
```lua
--{assert(0, "LUA")}--
println(token.label)
```

#### Macro Function getTokenNames() and getTokens()
The tokens library has an find() function that collect all tokens matching the same condition these functions user, which can be used to create them.
The condtion can be specified as a lua tables instead of JSON
```lua
--{assert(0, "LUA")}--
function getTokenNames(condition)
  local result = {}
  for index, tok in ipairs(tokens.find(condition)) do
    table.insert(result, tok.name)
  end
  return result
end

function getTokens(condition)
  local result = {}
  for index, tok in ipairs(tokens.find(condition)) do
    table.insert(result, tok.id)
  end
  return result
end

println(toJSON(getTokenNames({layer = {"TOKEN", "HIDDEN", "OBJECT", "BACKGROUND"}})))
println(toStr(getTokens()))
```
Usually one would want to work with the token objects instead
```lua
--{assert(0, "LUA")}--
for index, tok in ipairs(tokens.find({ range = {upto = 2, distancePerCell = 0}, npc = 1, unsetStates = {"Dead"} })) do
  println(tok.name, " is ", tok.label)
end
```

#### Macro Function getTokenPortrait()
The Portrait Image is a property of any token:
```lua
--{assert(0, "LUA")}--
println(token.portrait)
```

#### Macro Function getTokenStates()
This information can be extracted from the campaign properties
```lua
--{assert(0, "LUA")}--
for name in pairs(campaign.allStates) do
  println(name)
end
for name in pairs(campaign.states[group]) do
  println(name)
end
```


#### Macro Function getTokenShape()
The Shape is a property of any token:
```lua
--{assert(0, "LUA")}--
println(token.shape)
```

#### Macro Function getTokenWidth()
The Width is a property of any token:
```lua
--{assert(0, "LUA")}--
println(token.width)
```

#### Macro Function getTokenX() and getTokenY()
These coordiantes can be gotten from the location(units) of any Token
```lua
--{assert(0, "LUA")}--
println(token.location().x) --or location(true): Pixels
println(token.location(false).y) --Cells
```

#### Macro Function getVBL() 
There are two functions for this in the VBL library: get() and getSimple(), where getSimple() retruns the same as the format of getVBL(shape, format) being 1.
The functions take and return Lua Tables instead of JSON
```lua
--{assert(0, "LUA")}--
println(toJSON(VBL.get({shape="rectangle",x=50,y=50,w=100,h=200,r=45,fill=1,thickness=1,scale=0})))
println(toJSON(VBL.getSimple({shape="rectangle",x=50,y=50,w=100,h=200,r=45,fill=1,thickness=1,scale=0})))
```

#### Macro Function getVisible()
Visible is a property of any token:
```lua
--{assert(0, "LUA")}--
println(token.visible)
```

#### Macro Function getVisibleMapNames()
The visible table in the maps object has all visible maps

--{assert(0, "LUA")}--
for name in pairs(maps.visible) do
  println(name)
end

#### Macro Function getVisibleTokens() and getVisibleTokenNames()
The tokens library has an visible() function that collect all visible tokens, which can be used to create these functions
```lua
--{assert(0, "LUA")}--
function getVisibleTokenNames()
  local result = {}
  for index, tok in ipairs(tokens.visible()) do
    table.insert(result, tok.name)
  end
  return result
end

function getVisibleTokens()
  local result = {}
  for index, tok in ipairs(tokens.visible()) do
    table.insert(result, tok.id)
  end
  return result
end

println(toJSON(getVisibleTokenNames()))
println(toStr(getVisibleTokens()))
```
Usually one would want to work with the token objects instead
```lua
--{assert(0, "LUA")}--
for index, tok in ipairs(tokens.visible()) do
  println(tok.name, " is ", tok.label)
end
```

#### Macro Function getWithState() and getWithStateNames()
The tokens library has an withState() function that collect all tokens with a state, which can be used to create these functions
```lua
--{assert(0, "LUA")}--
function getWithStateNames(state)
  local result = {}
  for index, tok in ipairs(tokens.withState(state)) do
    table.insert(result, tok.name)
  end
  return result
end

function getWithState(state)
  local result = {}
  for index, tok in ipairs(tokens.withState(state)) do
    table.insert(result, tok.id)
  end
  return result
end

println(toJSON(getWithStateNames("Prone")))
println(toStr(getWithState("Dead")))
```
Usually one would want to work with the token objects instead
```lua
--{assert(0, "LUA")}--
for index, tok in ipairs(tokens.withState(state)) do
  println(tok.name, " is ", tok.label)
end
```

#### Macro Function getZoom()
The zoom is in the maps object
```lua
--{assert(0, "LUA")}--
println(maps.zoom)
```

#### Macro Function goto()
The goTo function can be called on the maps object. (goto is a reserved word in Lua, so it is called goTo)
```lua
--{assert(0, "LUA")}--
maps.goTo(token)
maps.goTo(0, 1)
```

#### Macro Function hasImpersonated()
The impersonated() function of the tokens libraray returns the currently impersonated token object and can be checked for nil
```lua
--{assert(0, "LUA")}--
println("Impersonated : ", tokens.impersonated() ~= nil) -- hasImpersonated()
```

#### Macro Function hasLightSource()
The hasLightSource() function can be called on any token:
```lua
--{assert(0, "LUA")}--
println(token.hasLightSource())
println(token.hasLightSource("Generic"))
println(token.hasLightSource("Generic", "10"))
```

#### Macro Function hasMacro()
The hasMacro() function can be called on any token:
```lua
--{assert(0, "LUA")}--
println(token.hasMacro("(new)"));
```

#### Macro Function hasProperty()
Each property of any token can be checked for existence and definedness, whereas exists is equal to hasProperty and definedness checks if the property has ever been defined in the Token (not the opposite of isPropertyEmpty)
```lua
--{assert(0, "LUA")}--
token.properties.HP = 10
println(token.properties.HP.exists)
println(token.properties.HP.defined)
token.properties.HP = nil -- or ""
println(token.properties.HP.exists)
println(token.properties.HP.defined)
token.properties.HP.reset()
println(token.properties.HP.exists)
println(token.properties.HP.defined)
println(token.properties.UNKNOWN.exists)
println(token.properties.UNKNOWN.defined)
```

#### Macro Function hasSight()
Sight is a property of any token:
```lua
--{assert(0, "LUA")}--
println(token.sight)
```

#### Macro Functions hero(), heroStun(), heroBody()
There functions are part of the dice library, since they are linked together, the hero function results a table with stun and body instead
```lua
--{assert(0, "LUA")}--
local result = dice.hero(3,6)
println(result.stun, "/", result.body)
```

#### Macro Function hex()
The lua function string.format can be used to convert into many formats, including hexadecimal (%x), the function tonumber can convert from any base, including hex
```lua
--{assert(0, "LUA")}--
println(string.format("%x",100)) -- 64 == hex(100)
println(tonumber(64, 16)) -- 100
```

#### Macro Function hypot() or hypotenuse()
This function has been added to the math lib
```lua
--{assert(0, "LUA")}--
println(math.hypot(10,20))
```

#### Macro Function if()
if is a lua language construct. Lua also supports a somewhat ternary operator with EXPR and TRUE or FALSE. [http://lua-users.org/wiki/TernaryOperator]
Note, lua will not evaluate both expressions unlike the macro language does

```lua
--{assert(0, "LUA")}--
a = 10
b = 20
if a > b then
  println("A is larger than B")
else
  println("A is not larger than B")
end

println(a>b and "A is larger than B" or "A is not larger than B") -- ternary, will not work if the part in "A is larger than B" is false/nil

#### Macro Function indexValueStrProp()
Lua has no dedicated String Property and String List function, they have to be converted with [fromStr](#fromstr) to an acutal Lua-Table. During conversion, the order is preserved, so a indexValueStrProp function can be created like this

```lua
--{assert(0, "LUA")}--
function indexValueStrProp(prop, index) 
  if type(prop) ~= "table" then
    prop = fromStr(prop)
  end
  for key, value in pairs(prop) do
    if index == 0 then 
      return value
    end
    index = index-1
  end
  return nil
end

println(indexValueStrProp("a=blah; b=doh; c=meh", 1));
println(indexValueStrProp(fromStr("a=blah; b=doh; c=meh"), 2));
println(indexValueStrProp(fromStr("a=blah, b=doh, c=meh", nil, ","), 0)); --Change seperator to ","
```

#### Macro Function initiativeSize()
This information is stored in the size property of the initiative object
```lua
--{assert(0, "LUA")}--
println(initiative.size)
println(table.length(initiative.tokens)) -- The same
```

### Macro Function input()
Input is available as a global function, however there are some slight changes. Instead of just setting global variables, input will return them as a Lua Table (or NIL on a failure/abort)
The parameters can also be given as a Lua Table of Lua Tables (or multiple Lua Tables), for this, the name, content, prompt and options are all in the same table, and everything but the options has to be written in lowercase

```lua
--{assert(0, "LUA")}--
println(toJSON(input("AtkBonus", "DmgBonus", "CombatAdvantage")))
println(toJSON(input("tab0 | Info || TAB", "Name ## Rank ## Serial number | 1,2,3 || LIST","tab1 | Abilities || TAB","Strength ## Dexterity ## Wisdom")))
println(toJSON(input(
	{name="tab0", content = "Info", type="TAB"},
	{name="Name", width = 89},  --options are just included in there
	{name="Rank"},
	{name="Serial number", type="LIST", content = {1, 2, 3}},  -- allows for way better handling of string values with , |  or ##
	"tab1 | Abilities || TAB","Strength ## Dexterity ## Wisdom"))) -- Mixed input is also allowed
```
the LIST also supports tokens as content for LIST and RADIO

```lua
--{assert(0, "LUA")}-- 
local selected = input({name = "Token", type = "LIST", content = tokens.visible(), value="object"}) -- VALUE=OBJECT is also a new option
println(selected.Token.label) --The selected item is still a token object
println("<img src=\"",selected.Token.image,"\">")
println(toJSON(selected))
```

#### Macro Function isBarVisible()
There is no function for this, but any bar that is visible on a token has a non nil value

```lua
--{assert(0, "LUA")}-- 
if token.bars.Health == nil then
  println("Healtbar not visible")
else
  println("Healtbar visible")
end
```

#### Macro Functions isDialogVisible() and isFrameVisible()
These functions are part of the UI library
```lua
--{assert(0, "LUA")}-- 
println(UI.isDialogVisible("Name"))
println(UI.isFrameVisible("Name"))
```

#### Macro Function isFunctionDefined()
There is no function is Lua for this, but the functions table holds all user defined functions. The table can be used to check for the existance

```lua
--{assert(0, "LUA")}-- 
if functions.name ~= nil then -- function is defined
  functions.name("parameter")
else
  println("No user defined function \"name\" found")
end
```

#### Macro Function isGM()
This macro is part of the Macro library.
```lua
--{assert(0, "LUA")}--
macro.abort(isGM()) -- abort when not GM
```

#### Macro Function isNPC()
NPC-ness is a property of any token:
```lua
--{assert(0, "LUA")}--
println(token.npc) --true/false
```

#### Macro Function isNumber()
Type checking can be done with the lua language type() operator
```lua
--{assert(0, "LUA")}--
local x=10
println(x, ": ", type(x) == "number")
x = "abc"
println(x, ": ", type(x) == "number")
x = "10"
println(x, ": ", type(x) == "number")
x = tonumber(x)
println(x, ": ", type(x) == "number")
```

#### Macro Function isOwnedByAll()
Owned By All is a property of any token:
```lua
--{assert(0, "LUA")}--
println(token.ownedByAll)
```

#### Macro Function isOwner()
This function can be called on any token
```lua
--{assert(0, "LUA")}--
println(token.isOwner(chat.name))
```

#### Macro Function isPC()
PC-ness is a property of any token:
```lua
--{assert(0, "LUA")}--
println(token.pc) --true/false
```

#### Macro Function isPropertyEmpty()
Each property of any token can be checked for emptyness using .empty
```lua
--{assert(0, "LUA")}--
token.properties.HP = 10
println(token.properties.HP.empty)
token.properties.HP = nil -- or ""
println(token.properties.HP.empty)
token.properties.HP.reset()
println(token.properties.HP.empty)
println(token.properties.UNKNOWN.empty)
```

#### Macro Function isSnapToGrid()
Snap To Grid is a property of any token:
```lua
--{assert(0, "LUA")}--
println(token.snapToGrid)
```

#### Macro Function isTrusted()
This function is part of the macro library
```lua
--{assert(0, "LUA")}--
println(macro.isTrusted())
```

#### Macro Function isVisible()
This function can be called on any token
```lua
--{assert(0, "LUA")}--
println(token.isVisible(10, 0))
```

### Roll-Options


