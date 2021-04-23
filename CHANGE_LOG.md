# MapTool 1.8.5
Focus on bug fixes related to previous 1.8 releases plus an improvement to the macro editor.

#Enhancements
- [#2352][i2352] Token property names now get syntax highlighting

#Bug Fixes
- [#2522][i2522] Installs will no longer remove previous version.
- [#2513][i2513] `return(0,0)` could fail to return if `macro.return` was set to 1. Fixed.
- [#2511][i2511] Using `setTablePickOnce()` on older tables wasn't reset the picked status for entries. Fixed.
- [#2506][i2506] Assets with empty ID ("") could be stuck as transferring. Fixed.
- [#2490][i2490] Some assets were being loading with incorrect color space.  Fixed.
- [#2480][i2480] Inline expansion of URLs wasn't stopping at whitespace. Fixed.
- [#2471][i2471] Mixed case in build config could createa separate AppHome and log directories. Fixed. 
- [#2423][i2423] Servers were dropping from server registry.  Fixed.
- [#2396][i2396] JSON token properties would error silently when referenced by other properties. Fixed.  
- [#2382][i2382] `moveTokenToMap()` between maps with different distance/cell would put tokens in wrong place. Fixed. 

[i2522]: https://github.com/RPTools/maptool/pull/2522
[i2513]: https://github.com/RPTools/maptool/pull/2513
[i2511]: https://github.com/RPTools/maptool/pull/2511
[i2506]: https://github.com/RPTools/maptool/pull/2506
[i2490]: https://github.com/RPTools/maptool/pull/2490
[i2480]: https://github.com/RPTools/maptool/pull/2480
[i2471]: https://github.com/RPTools/maptool/pull/2471
[i2423]: https://github.com/RPTools/maptool/pull/2423
[i2396]: https://github.com/RPTools/maptool/pull/2396
[i2396]: https://github.com/RPTools/maptool/pull/2396
[i2382]: https://github.com/RPTools/maptool/pull/2382
[i2352]: https://github.com/RPTools/maptool/pull/2352

# MapTool 1.8.4
Focus on bug fixes related to 1.8.3 release.

#Bug Fixes
- [#2470][i2470] Fixed typo in JSON results of `getMacroProps()`.
- [#2449][i2449] Text color in `<p>` elements could be overridden by other elements in chat. Fixed.
- [#2444][i2444] Rolls from `json.objrolls()` were not showing in `getRolled()` and `getNewRolls()`. Fixed.
- [#2442][i2442] `getTableEntry()` returned incorrect results when passed out-of-bound roll values. Fixed.
- [#2441][i2441] Executing `[h: return(0)]` directly would produce an error. Fixed.
- [#2436][i2436] Auto-complete summary field was not showing tooltips for UDFs on GM/Campaign panels.
- [#2432][i2432] MapTool was being registered as default handler for `.zip` files. Fixed.
- [#2426][i2426] Exception thrown by Prefrences dialog when language file had a translation for `Label.startup` string. Fixed.
- [#2424][i2424] Changing token states in onInitiative events breaks initiative list synchronization between server and clients. Fixed.
- [#2422][i2422] UDFs no longer recognized by code-completion. Fixed.
- [#2416][i2416] Setting facing for tokens on hex maps could throw an exception.  Fixed.
- [#2411][i2411] Probable fix for TimSort Illegal Argument Exceptions from vision calculations.
- [#2408][i2408] Preferences was still trying to write to config file. Fixed.
- [#2402][i2402] Map Explorer was showing all NPC tokens to Players. Fixed.
- [#2393][i2393] Version update wasn't tracking versions correctly causing it to prompt for update to same version. Fixed.
- [#2394][i2394] Player could login as GM with Player password. Fixed. Client role is now determined by password used.  
- [#2392][i2392] MapTool would refuse to start when a custom theme is specified in prefs but was missing from datadir. Fixed.

## Other
- [#2475][i2475] Updated DiceLib to 1.7.1 for fix to [#2444][i2444]
- [#2474][i2474] Updated Twelve Monkeys imageio plugins to 3.64.
- [#2399][i2399] Updated GitHub README.MD

[i2475]: https://github.com/RPTools/maptool/pull/2475
[i2474]: https://github.com/RPTools/maptool/issues/2474
[i2470]: https://github.com/RPTools/maptool/issues/2470
[i2449]: https://github.com/RPTools/maptool/issues/2449
[i2444]: https://github.com/RPTools/maptool/issues/2444
[i2442]: https://github.com/RPTools/maptool/issues/2442
[i2441]: https://github.com/RPTools/maptool/issues/2441
[i2436]: https://github.com/RPTools/maptool/issues/2436
[i2432]: https://github.com/RPTools/maptool/issues/2432
[i2426]: https://github.com/RPTools/maptool/issues/2426
[i2424]: https://github.com/RPTools/maptool/issues/2424
[i2422]: https://github.com/RPTools/maptool/issues/2422
[i2416]: https://github.com/RPTools/maptool/issues/2416
[i2411]: https://github.com/RPTools/maptool/issues/2411
[i2408]: https://github.com/RPTools/maptool/issues/2408
[i2402]: https://github.com/RPTools/maptool/issues/2402
[i2399]: https://github.com/RPTools/maptool/issues/2399
[i2394]: https://github.com/RPTools/maptool/issues/2394
[i2393]: https://github.com/RPTools/maptool/issues/2393
[i2392]: https://github.com/RPTools/maptool/issues/2392

# Maptool 1.8.0
**MapTool now uses Java 14 from AdoptOpenJDK.**

Lots of enhancements, bug fixes and performance improvements to the code base.

Many thanks to community developers merudo, selquest, nmeier, euank, ebudai, grimreaper, Phi1010, Irarara, irisiflimsi, dluga93, MeloDub, dat-adi, sum_catnip for their work on this release.  Also, big thanks to the translators building up the language support and the wiki editors for improving/growing the documentation.

## Highlights
- Critical Security Fixes - see [Discord](https://discord.gg/hbn2bfn) or [Website](https://www.rptools.net) for more details.
- Significant improvements in macro run times giving a 4x to 10x (and sometimes more) reduction in run times.
- Chat output formatting refactored to produce a more consistent output and make future changes easier/cleaner.
- Macro errors now produce a _call stack_ showing the macro calls leading to the error.
  ```
  Function "setTokenDrawOrder" requires at least 1 parameters; 0 were provided.
  Error trace : m3@Campaign <<< m2@Campaign <<< m1@campaign
  ```
- ISO-8859-1 character support. Can now use accented characters in Token Properties and macros.
- Several Export Screenshot bugs fixed.
- Annoying bug in Resource Library where scroll bar coulnd't go down far enough fixed.
- A number of Initiative improvements/enhancement: [#987][i987], [#1458][i1458], [#1479][i1479], [#1845][i1845], [#2097][i2097]
  - See wiki page [Introduction to Initiative](https://wiki.rptools.info/index.php/Introduction_to_Initiative)


## Enhancements
- [#2345][i2345] `/ooc` chat command now displays Player name instead of current impersonation.  
- [#2314][i2314] Output formatting refactored to use CSS classes and moved to a new MessageUtil class.  
- [#2283][i2283] Basic support for SVG images added.
- [#2271][i2271] Improved readability for output of chat commands /emote & /say to bring them inline with normal macro output. 
- [#2256][i2256] Support for `data:` URIs in HTML5 windows (overlay, dialog5, html5).
- [#2237][i2237] MapTool Builds now include package for ArchLinux.
- [#2230][i2230] `getInfo("client")` function now includes details about Dialogs, Frames and Overlays.
- [#2229][i2229] New `delim` parameter for `herolab.XPath()` function allows for returning a string list or JSON Array to address issues with content having commas in it.
- [#2205][i2205] Improved efficiency of `onTokenMove` event handler with multiple tokens selected.
- [#2199][i2199] Updated Dicelib 1.7.0 adds support for Shadowrun 5 dice rolls: `sr5(n)`, `sr5(n,g)`, `sr5e(n)`, `sr5(n,g)`
- [#2188][i2188] New macro function `removeDrawing(MapName, DrawingID)` to remove drawings from map.
- [#2157][i2157] Macro function `resetTablePicks()` can now be used to reset specific entries in table. I.e. return drawn cards to a deck.
- [#2149][i2149] Improved performance of Fog of War checks for a noticeable improvement on complex maps.
- [#2097][i2097] Initiative can now be set to ascending or descending order with *descending* as default.
- [#2032][i2032] Added various Preferences settings to output of `getInfo("client")`.  Added _personal server_ status to `getInfo("server")`.
- [#1986][i1986] Stat sheets and token notes now appear above Overlays.
- [#1958][i1958] Moving a token into a unreachable location adds a `fail` key/value pair to the last object in the array of location objects returned by `getLastPath()` or to `onTokenMove`.
- [#1898][i1898] A number of improvements to macro performance resulting in as much as 10x or more reduction in run time.
- [#1890][i1890] Map name parameter added to `removeToken()` function. `copyToken()` function cleaned up internally.
- [#1882][i1882] NullPointerException messages now include more information about _why_ the error occured.
- [#1861][i1861] Macro errors now produce a _call stack_ showing the macro calls leading to the error.
- [#1845][i1845] Initiative Panel restyled to make it more obvious when a token isn't visible to players.
- [#1726][i1726] Lumens value added to Personal Lights in Sight definitions. This allows Sight definitions that will see into areas of darkness.
- [#1482][i1482] Can now export Macro Groups instead of an entire macro panel with right-click Export Macro Group option.
- [#1479][i1479] New macro function `prevInitiative()` allows stepping back one initiative advance.
- [#1458][i1458] New Initiative panel menu option to disable Next/Previous functions.
- [#1362][i1362] New macro function to run JavaScript functions defined in frames, dialogs and overlays: `runJsFunction(name, type, func, thisArg, argsArray)`
- [#987][i987] New events for initiative: `onInitiativeChange`, `onInitiativeChangeRequest`.  New system variable `init.denyChange`.

## Bug Fixes
- [#2343][i2343] Trusted Prefix preference colors weren't being correctly saved nor used in chat output. Fixed.  
- [#2335][i2335] Using `cp:` protocol with invalid resource could freeze client. Fixed.  
- [#2321][i2321] With Individual Views option OFF, players could not see owned NPC tokens. Fixed.
- [#2313][i2313] Switching to Player View with an Object flagged as VoFoW could cause an NPE. Fixed by fix for [#2242][i2242]  
- [#2311][i2311] `movedOverToken()` throwing exception in beta. Fixed.
- [#2287][i2287] Export screenshot broken in beta. Fixed. 
- [#2270][i2270] Control for restricting impersonation of tokens by players was working opposite of indicated state. Fixed. 
- [#2223][i2223] `onCampaignLoad` macros were no longer suppressing output when executed on load. Fixed. 
- [#2221][i2221] Beta builds had OpenJDK icon instead of MT icon in menus for linux. Fixed. 
- [#2220][i2220] MapTool beta 2 was creating a folder in user home for logs instead of using `.maptool-rptools\logs`. Fixed. 
- [#2215][i2215] Some previously working URIs were failing for playStream/Clip in beta. Fixed. 
- [#2242][i2242] NullPointerException: Cannot invoke "java.awt.geom.Area.intersects(java.awt.geom.Rectangle2D)" because "fog" is null. Fixed. 
- [#2211][i2211] Function `isNumber()` returning incorrect response on anything not a positive integer. Fixed. 
- [#2192][i2192] Using function `movedOverDrawing()` with a template would throw an NPE. Fixed. 
- [#2184][i2184] Initiative panel Lock Movement toggle was also changing Owner Permissions. Fixed. 
- [#2178][i2178] Token opacity was broken in develop.  Fixed.
- [#2177][i2177] Output of macroLinks was not being formatted properly.  Fixed.
- [#2174][i2174] Ability to save startup settings broken by Java/packaging changes.  Restored.  Startup tab now has more explanation.
- [#2152][i2152] Function `isNumber()` was returning true for empty strings. Fixed.
- [#2151][i2151] Function `isNumber()` was returning true for empty strings. Fixed.
- [#2119][i2119] Excessive processing of roll options was making macro execution slow. Changes improved speed by 10x or more.
- [#2118][i2118] Importing large macrosets was very slow and used **a lot** of memory. Changes improved speed by 10:1 up to 150:1 for macro buttons with images.
- [#2116][i2116] Failing to load a campaign incorrectly reported *"Could not save campaign."* Fixed.
- [#2092][i2092] Spurious *on change selection* events were occuring and caused infinite loops under 1.7.0. Fixed.
- [#2081][i2081] Universal VTT map import wasn't applying portal closed flag to enable/disable VBL on portals. Fixed.
- [#2078][i2078] Event `onChangeToken` was being generated multiple times even when token wasn't changed. Was repeating indefinitely on impersonated tokens. Fixed.
- [#2074][i2074] Updating overlay wasn't removing event handlers, e.g.`onChangeSelection`. Fixed.
- [#2069][i2069] Tokens partially out of line-of-sight were fully shown on player clients. Fixed.
- [#2057][i2057] Token properties with a single element JSON array returned bad results when accessed. Not in published code. Fixed.
- [#2056][i2056] Form submit for dialogs, frames and overlays was on wrong thread leading to inconsistent results from macro functions such as `getToken()`. Fixed.
- [#2046][i2046] Cancelling out of Rename Macro Group would cause an NPE. Fixed.
- [#2041][i2041] A number of macro functions if called with the wrong case such as `getplayername()` instead of `getPlayerName()`, would either call the wrong function internally or return the wrong result. A parser exception will now be thrown instead.  See issue for more details.
- [#2040][i2040] Vision of player-owned NPC tokens were not visible when no PC tokens were on map. Fixed.
- [#2037][i2037] Clicking on Move Up/Down buttons with only a Single bar defined was throwing an exception. Fixed.
- [#2023][i2023] Moving a token on a map with FoW and triggering an `onTokenMove` event that moved the token to a different map would cause an NPE. Fixed.
- [#2021][i2021] Double-clicking on token in Initiative List wasn't updating Selected panel. Fixed.
- [#2006][i2006] Some themes still had some UI elements using fonts lacking CJK support. Fixed.
- [#1996][i1996] Error message for missing remote repository was missing closing quote. Fixed.
- [#1995][i1995] Function `capitalize()` would always treat numbers and symbols as word boundaries. New parameter allows for turning off.
- [#1988][i1988] Audio funciton `playClip()` was hogging the JavaFX application thread. Fixed.
- [#1976][i1976] Calling `overlay()` with new content was not erasing previous content. Fixed.
- [#1974][i1974] Disabling deterministic expression building was causing NPEs. Not in released builds. Fixed.
- [#1972][i1972] Using `listGet()` on lists where elements had spaces in the name would fail. Fixed.
- [#1962][i1962] Selecting colors in Drawing tool color palette could fail if mouse is moved between click and release. Fixed.
- [#1950][i1950] Calling `getname()` instead of `getName()` was calling `setName() instead. Fixed.
- [#1948][i1948] Multiple fixes to Exporting Screenshots.
  - Uninitialized variables if attempting to do an Entire Map export would cause NPE. Fixed.
  - Exposed FoW areas not included in exported image. Fixed.
  - Entire Map exports were not getting correct extents. Fixed.
- [#1938][i1938] Macros created via `createaMacro()` or already present on a droped in token were getting set to _Player Editable_ as they should be. Fixed.
- [#1935][i1935] Players calling macros via a frame macrolink would not have permission to use trusted functions. Fixed.
- [#1926][i1926] Updates to Hot Key display checkbox and macro Commonality were only applied on first edit. Fixed.
- [#1921][i1921] Rapidly changing Token facing in vision range of of multiple lights would degrade performance. Fixed.
- [#1896][i1896] Light sources with a lumens value of 100 weren't revealing for tokens with personal light. Fixed.
- [#1894][i1894] Trusted functions called from a `frame` opened from a GM panel macro lacked permission to run. Fixed.
- [#1893][i1893] Several bugs related to personal lights fixed.
  - Areas exposed by personal lights would be "overridden" by dim light sources.
  - Superfluous calls to `flush(token)` and `exposeFoW()` were removed.
  - Light caches no longer accumulate duplicates.
- [#1889][i1889] Macro function `copyToken()` wasn't preserving Shape setting of token. Fixed.
- [#1888][i1888] Clicking on or hovering over a macro link in a campaign with no maps was producing an NPE. Fixed.
- [#1886][i1886] Error message dialog not being display correctly. Fixed. Not in released code.
- [#1884][i1884] Overlay transparency broken with Java 14. Fixed. Not in released code.
- [#1880][i1880] Attempting to create an `overlay` when there are no maps caused an NPE. Fixed.
- [#1878][i1878] HTML5 button formAction was ignored. Fixed.
- [#1876][i1876] Spurious quote symbols no longer created in chat by `[if(), code:{}]` blocks lacking the _else_ block.
- [#1874][i1874] Macro function `getMacroProps()` was returning a misnamed property (`toolapplyToSelected`). Fixed.
- [#1867][i1867] Macro links to macros with no output would produce an empty line in chat. Fixed.
- [#1863][i1863] HTML5 form submit was not allowed at document load. Fixed.
- [#1737][i1737] Some CMYK JPEGs would appear as all black. Fixed.
- [#1736][i1736] Variable names can now start with `tru`, `true`, `fal`, `fals`, and `false`.
- [#1733][i1733] Embedded double quotes in JSON objects not handled correctly. Fixed.
- [#1705][i1705] Map functions `getCurrentMapName(), getMapVisible() and setMapVisible()` would throw NPEs if campaign had no maps. Fixed.
- [#1562][i1562] Accessing certain data from HeroLab files in the Edit Token dialog could thrown an exception. Fixed.
- [#1560][i1560] Default property settings using assigment expressions, i.e.`{prop2 = prop1}`, would fail. Fixed.
- [#1359][i1359] Map coordinates were appearing over token notes. Fixed.
- [#755][i755] Scroll bar in Resource Library window wouldn't always go far enough to see all content. Fixed.
- [#715][i715] Bad or missing remote repository would produce a Null Pointer Exception in the log but not inform the user. Fixed.

## Other
- [#2280][i2280] Update to README.md for clarity of current processes. Fixed.
- [#2279][i2279] CampaignDialog unit test failing under DE locale.  Fixed.
- [#2244][i2244] Too much network debug output when using `gradlew run`.  Fixed.
- [#2197][i2197] Build changes caused release builds to barf if release tags contained alpha chars(i.e. beta).  Fixed.
- [#2135][i2135] Code cleanup: removed redundant null checks, tests that were always true/false, replaced anonymous inner classes with lambdas, etc.
- [#2109][i2109] Build.gradle update to pull version from tag. Fixed.  Dev only.
- [#2102][i2102] `jpackage` created Windows installers weren't installing/updating with existing installs. Fixed.  Dev only.
- [#2049][i2049] Removed a number of classes that were obsolete/unused.
- [#2047][i2047] Code cleanup. Removed obsolete Java version checks.
- [#2019][i2019] Updated to Parse 1.8.0 which added support for logical operators to Expression.format()/InlineTreeFormatter..
- [#1992][i1992] Code cleanup of I18NManager.
- [#1947][i1947] MapTool credits updated with recent (and long time) contributors.
- [#1943][i1943] Updated to Parse 1.7.1 which adds ISO-8859-1 character support. Can now use accented characters in Token Properties and macros.
- [#1907][i1907] Dicelib updated to 1.6.2 for fixes to `getRolled()` and `getNewRolls()`.

[i2345]: https://github.com/RPTools/maptool/issues/2345
[i2343]: https://github.com/RPTools/maptool/issues/2343
[i2335]: https://github.com/RPTools/maptool/issues/2335
[i2321]: https://github.com/RPTools/maptool/issues/2321
[i2314]: https://github.com/RPTools/maptool/issues/2314
[i2313]: https://github.com/RPTools/maptool/issues/2313
[i2311]: https://github.com/RPTools/maptool/issues/2311
[i2287]: https://github.com/RPTools/maptool/issues/2287
[i2283]: https://github.com/RPTools/maptool/issues/2283
[i2280]: https://github.com/RPTools/maptool/issues/2280
[i2279]: https://github.com/RPTools/maptool/issues/2279
[i2271]: https://github.com/RPTools/maptool/issues/2271
[i2270]: https://github.com/RPTools/maptool/issues/2270
[i2256]: https://github.com/RPTools/maptool/issues/2256
[i2244]: https://github.com/RPTools/maptool/issues/2244
[i2242]: https://github.com/RPTools/maptool/issues/2242
[i2237]: https://github.com/RPTools/maptool/issues/2237
[i2230]: https://github.com/RPTools/maptool/issues/2230
[i2229]: https://github.com/RPTools/maptool/issues/2229
[i2223]: https://github.com/RPTools/maptool/issues/2223
[i2221]: https://github.com/RPTools/maptool/issues/2221
[i2220]: https://github.com/RPTools/maptool/issues/2220
[i2215]: https://github.com/RPTools/maptool/issues/2215
[i2211]: https://github.com/RPTools/maptool/issues/2211
[i2205]: https://github.com/RPTools/maptool/issues/2205
[i2199]: https://github.com/RPTools/maptool/issues/2199
[i2197]: https://github.com/RPTools/maptool/issues/2197
[i2192]: https://github.com/RPTools/maptool/issues/2192
[i2188]: https://github.com/RPTools/maptool/issues/2188
[i2184]: https://github.com/RPTools/maptool/issues/2184
[i2178]: https://github.com/RPTools/maptool/issues/2178
[i2177]: https://github.com/RPTools/maptool/issues/2177
[i2174]: https://github.com/RPTools/maptool/issues/2174
[i2157]: https://github.com/RPTools/maptool/issues/2157
[i2152]: https://github.com/RPTools/maptool/issues/2152
[i2151]: https://github.com/RPTools/maptool/issues/2151
[i2149]: https://github.com/RPTools/maptool/issues/2149
[i2135]: https://github.com/RPTools/maptool/issues/2135
[i2119]: https://github.com/RPTools/maptool/issues/2119
[i2118]: https://github.com/RPTools/maptool/issues/2118
[i2116]: https://github.com/RPTools/maptool/issues/2116
[i2109]: https://github.com/RPTools/maptool/issues/2109
[i2102]: https://github.com/RPTools/maptool/issues/2102
[i2097]: https://github.com/RPTools/maptool/issues/2097
[i2092]: https://github.com/RPTools/maptool/issues/2092
[i2081]: https://github.com/RPTools/maptool/issues/2081
[i2078]: https://github.com/RPTools/maptool/issues/2078
[i2074]: https://github.com/RPTools/maptool/issues/2074
[i2069]: https://github.com/RPTools/maptool/issues/2069
[i2057]: https://github.com/RPTools/maptool/issues/2057
[i2056]: https://github.com/RPTools/maptool/issues/2056
[i2049]: https://github.com/RPTools/maptool/issues/2049
[i2047]: https://github.com/RPTools/maptool/issues/2047
[i2046]: https://github.com/RPTools/maptool/issues/2046
[i2041]: https://github.com/RPTools/maptool/issues/2041
[i2040]: https://github.com/RPTools/maptool/issues/2040
[i2037]: https://github.com/RPTools/maptool/issues/2037
[i2032]: https://github.com/RPTools/maptool/issues/2032
[i2023]: https://github.com/RPTools/maptool/issues/2023
[i2021]: https://github.com/RPTools/maptool/issues/2021
[i2019]: https://github.com/RPTools/maptool/issues/2019
[i2006]: https://github.com/RPTools/maptool/issues/2006
[i1996]: https://github.com/RPTools/maptool/issues/1996
[i1995]: https://github.com/RPTools/maptool/issues/1995
[i1992]: https://github.com/RPTools/maptool/issues/1992
[i1988]: https://github.com/RPTools/maptool/issues/1988
[i1986]: https://github.com/RPTools/maptool/issues/1986
[i1976]: https://github.com/RPTools/maptool/issues/1976
[i1974]: https://github.com/RPTools/maptool/issues/1974
[i1972]: https://github.com/RPTools/maptool/issues/1972
[i1962]: https://github.com/RPTools/maptool/issues/1962
[i1958]: https://github.com/RPTools/maptool/issues/1958
[i1950]: https://github.com/RPTools/maptool/issues/1950
[i1948]: https://github.com/RPTools/maptool/issues/1948
[i1947]: https://github.com/RPTools/maptool/issues/1947
[i1943]: https://github.com/RPTools/maptool/issues/1943
[i1938]: https://github.com/RPTools/maptool/issues/1938
[i1935]: https://github.com/RPTools/maptool/issues/1935
[i1926]: https://github.com/RPTools/maptool/issues/1926
[i1921]: https://github.com/RPTools/maptool/issues/1921
[i1907]: https://github.com/RPTools/maptool/issues/1907
[i1898]: https://github.com/RPTools/maptool/issues/1898
[i1896]: https://github.com/RPTools/maptool/issues/1896
[i1894]: https://github.com/RPTools/maptool/issues/1894
[i1893]: https://github.com/RPTools/maptool/issues/1893
[i1890]: https://github.com/RPTools/maptool/issues/1890
[i1889]: https://github.com/RPTools/maptool/issues/1889
[i1888]: https://github.com/RPTools/maptool/issues/1888
[i1886]: https://github.com/RPTools/maptool/issues/1886
[i1884]: https://github.com/RPTools/maptool/issues/1884
[i1882]: https://github.com/RPTools/maptool/issues/1882
[i1880]: https://github.com/RPTools/maptool/issues/1880
[i1878]: https://github.com/RPTools/maptool/issues/1878
[i1876]: https://github.com/RPTools/maptool/issues/1876
[i1874]: https://github.com/RPTools/maptool/issues/1874
[i1867]: https://github.com/RPTools/maptool/issues/1867
[i1863]: https://github.com/RPTools/maptool/issues/1863
[i1861]: https://github.com/RPTools/maptool/issues/1861
[i1845]: https://github.com/RPTools/maptool/issues/1845
[i1737]: https://github.com/RPTools/maptool/issues/1737
[i1736]: https://github.com/RPTools/maptool/issues/1736
[i1733]: https://github.com/RPTools/maptool/issues/1733
[i1726]: https://github.com/RPTools/maptool/issues/1726
[i1705]: https://github.com/RPTools/maptool/issues/1705
[i1560]: https://github.com/RPTools/maptool/issues/1560
[i1482]: https://github.com/RPTools/maptool/issues/1482
[i1458]: https://github.com/RPTools/maptool/issues/1458
[i1479]: https://github.com/RPTools/maptool/issues/1479
[i1362]: https://github.com/RPTools/maptool/issues/1362
[i1359]: https://github.com/RPTools/maptool/issues/1359
[i987]: https://github.com/RPTools/maptool/issues/987
[i755]: https://github.com/RPTools/maptool/issues/755
[i715]: https://github.com/RPTools/maptool/issues/715

[Change Log for 1.7.0](https://github.com/RPTools/maptool/blob/1.7.0/CHANGE_LOG.md)
