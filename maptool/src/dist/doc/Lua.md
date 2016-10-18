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
#### Macro function floor()
The [Lua Math library](https://www.lua.org/pil/18.html) has a floor() function
```lua
--{assert(0, "LUA")}--
println(math.floor(10.2))
```
#### Macro function formatStrProp()
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
### Roll-Options

