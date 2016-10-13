# LUA Support for Maptool
## What is Lua?
[Lua](https://Lua.org/) is a small programming language usually embedded in programs (mostly games) 
You can try it online under [https://www.Lua.org/demo.html]
## How does a macro become a Lua macro?
Every input (macro, chatline, eval()) that might be macro code is assumed to be a Lua-macro if it starts with the Lua identifier.
### Lua-Identifier
```lua
--{abort(0)} LUA--

```
The identifier has to be followed by a newline
The identifier is both valid LUA and maptool macro language, but prevents the macro interpreter from running it (should LUA not be available) 
## Major changes
### Use print() and println()
Lua macros do not output anything by default. Everything that needs to be written to the chat-box must be printed.
```lua
--{abort(0)} LUA-- 
gold = 22 --Comments in Lua start with --
println("You found ", gold, " Gold")
print("Lucky you")
```
println() does the same as print(), but it adds an HTML-Linebreak to the output.

Writing to the default Lua output stream has the same effect, however the Lua io library is currently disabled for security concerns.
### There are now Objects
Lua allows for objects, and under with this in mind, many of the functions and special variables have been grouped in to Objects like [token](#token) for a single token, or the [campaign](#campaign) global for the campaign properties

### String-Lists, String-Property-Lists und JSON-Variables
These should not be used in LUA, there are functions like [toJSON](#tojson), [fromJSON](#fromjson), TODO that provide an interface to normal macros, However, all functions that can be called from lue use Lua-objects (specifically Luatable). These have direct support in Lua, and can be nested without any hassle. 

In fact JSON-Objects and Arrays are always treated as Text in Lua and use their specialness they have in the normal macro language, making them slower to use in Lua.

#### LuaTable as array (replaces String-Lists and JSON-Arrays)
```lua
--{abort(0)} LUA--
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
--{abort(0)} LUA--
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
--{abort(0)} LUA--
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
--{abort(0)} LUA--
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

### macro.args and macro.retrun
Those variables are automatically converted to and from JSON to Lua-datastructures, the original values can be accessed with this:
```lua
--{abort(0)} LUA--
println(macro.args.enemy) --converted from JSON
print(_G["macro.args"]) -- original value: {enemy="Skeleton", count=3}
```

Setting macro.return can be done anywhere like this:
```lua
--{abort(0)} LUA--
macro["return"] = {key=value} --return is a keyword in lua, so it must be written that way instead of macro.return
```
However, at the end of a block, lua allows a return statement like this:
```lua
--{abort(0)} LUA--
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
### token
### copyToken
### selectTokens
### deselectTokens
### tokenProperties
### tokens
### maps
### broadcast


## Objects


## How do I do in LUA?
### Macro-Functions
#### Macro-Function abort()
```lua
--{abort(0)} LUA--
macro.abort(isGM()) -- abort when not GM (boolean is false)
local enemiesLeft=1;
macro.abort(enemiesLeft) -- abort when enemiesLeft is zero
macro.abort()
```
#### Macro-Functions abs() and absolutevalue()
abs() is part of the defualt [Lua Math library](https://www.lua.org/pil/18.html)
```lua
--{abort(0)} LUA--
local value = -13
print(math.abs(value))
```

#### Macro Function add()
Lua supports + and .. to add numbers and concatinate strings respektively, however there is no dedicated sum() function. It can be very easily implemented however:
```lua
--{abort(0)} LUA--
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
--{abort(0)} LUA--
tokens.addAllNPCsToInitiative()
tokens.addAllPCsToInitiative(false) --don't add again (same as no argument)
tokens.addAllToInitiative(false) --add again, even if already in initiative
```

#### Marco Function addToInitiative()
addToInitiative() is now a method for token-objects, that means they can be called on the current token.
```lua
--{abort(0)} LUA--
token.addToInitiative(false, 20) --Global token Object
```
Or on other tokens (Trusted-Macro or Ownership required)
```lua
--{abort(0)} LUA--
for index, tok in ipairs(tokens.visible()) do
  tok.addToInitiative(false, index)
end
```

#### Marco Functions arg() and argCount()
These are in the macro library and like macro.args convert the arguments into lua-datastructures should they be JSON
```lua
--{abort(0)} LUA--
for i = 1, macro.argCount() do
  println("Arg ", i, " = ", macro.arg(i))
end
```

#### Marco Function assert()
Lua has its own assert statement with almost exactly the same functionality
```lua
--{abort(0)} LUA--
assert(isGM(), "Player not GM")
println("Welcome back, Sire")
```

#### Marco Functions avg() and average()
There is no direct function to do this, but it can be easily implemented in lua
```lua
--{abort(0)} LUA--
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
--{abort(0)} LUA--
print(bit32.band(5,9))
```

#### Marco Functions bnot() and bitwisenot()
[bit32](https://www.lua.org/manual/5.2/manual.html#6.7) is the Lua Library for bit-manipulation (Currently sadly limited to 32 bits)
```lua
--{abort(0)} LUA--
print(bit32.bnot(5))
```

#### Marco Functions bor() and bitwiseor()
[bit32](https://www.lua.org/manual/5.2/manual.html#6.7) is the Lua Library for bit-manipulation (Currently sadly limited to 32 bits)
```lua
--{abort(0)} LUA--
print(bit32.bor(5,9))
```

#### Marco Functions bxor() and bitwisexor()
[bit32](https://www.lua.org/manual/5.2/manual.html#6.7) is the Lua Library for bit-manipulation (Currently sadly limited to 32 bits)
```lua
--{abort(0)} LUA--
print(bit32.bxor(5,9))
```

#### Marco Function bringToFront()
The function bringToFront() can be called on any token.
```lua
--{abort(0)} LUA--
token.bringToFront() -- Current Token
```
On other tokens (Trusted-Macro or Ownership required):
```lua
--{abort(0)} LUA--
for index, tok in ipairs(tokens.visible()) do
  tok.bringToFront()
end
```

#### Marco Function broadcast()
The function broadcast() is in the global namespace.
```lua
--{abort(0)} LUA--
broadcast("Hi, my name is " .. token.name); --All
broadcast("Actually I am " .. token.gm_name, "gm") --All GMs
```

### Roll-Options

