# Maptool 1.8.0
**MapTool now uses Java 14 from AdoptOpenJDK.**

Lots of enhancements, bug fixes and performance improvements to the code base.

Many thanks to community developers merudo, selquest, nmeier, euank, ebudai, grimreaper, pk1010, Irarara, irisiflimsi, dluga93, melodub, dat-adi for all their work on this release.  Also big thanks to the translators building up the language support and the wiki editors for improving/growing the documentation.

## Highlights
- Significant improvements in macro run times giving a 4x to 10x (and sometimes more) reduction in run times.
- Macro errors now produce a _call stack_ showing the macro calls leading to the error.
  ```
  Function "setTokenDrawOrder" requires at least 1 parameters; 0 were provided.
  Error trace : m3@Campaign <<< m2@Campaign <<< m1@campaign
  ```
- ISO-8859-1 character support. Can now use accented characters in Token Properties and macros.
- Several Export Screenshot bugs fixed.
- Annoying bug in Resource Library where scroll bar coulnd't go down far enough fixed.
- A number of Initiative improvements/enhancement: [#987][i987], [#1458][i1458], [#1479][i1479], [#1845][i1845], [#2097][i2097]
  - See wiki page [Introduction to Initiative](http://lmwcs.com/rptools/wiki/Introduction_to_Initiative)


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

---
# Maptool 1.7.0
Lots of enhancements, bug fixes and improvements to the code base.

## Some Highlights
- New macro functions for math operations on lists and arrays.
- New macro function `markdownToHTML()` converts text in Markdown to HTML.
- nmeier has brought the majority of the MapTool UI under the control of the Look & Feel themes accessible from the Themes menu.
- New GM option to disable the use of the Edit Token dialog by players.
- Issue with decimal point in Map Units per Cell for European locales fixed.
- New `overlay()` function allows for transparent overlays over the map area. Think MMORPG-like GUI elements.
- New Overlay helper functions for management.
- Huge localization effort by Merudo has brought translatable strings to the majority of the MapTool dialogs.
- MapTool can now import the Dungeondraft VTT export format.

Changes since 1.7.0-beta-2 in **BOLD**.

## Enhancements
- [#1847][i1847] Horizontal scrolling restored for MacOS.
- [#1808][i1808] New `impersonate()` function for setting impersonation.
- [#1801][i1801] Performance improvement for token image handling. Copying large sets of tokens from map to map, switching between maps with a large number of tokens, dragging large numbers of tokens have all been improved.
- [#1797][i1797] Forms now work in Chat window!
- [#1765][i1765] New macro function to take Markdown text and covert to HTML.  See issue for details.
- [#1764][i1764] New macro functions math operations on lists and JSON arrays.
  - `math.arraySum()`, `math.arrayMin()`, `math.arrayMax()`, `math.arrayMean()`, `math.arrayMedian()`, `math.arrayProduct()`
  - `math.listSum()`, `math.listMin()`, `math.listMax()`, `math.listMean()`, `math.listMedian()`, `math.listProduct()`
- [#1758][i1758] Restyled Initiative and Selection windows for usability and compatibility with Look & Feel themes.  See issue for details.
- [#1728][i1728] Allow JavaScript/CSS/fonts to be fetched from approved CDNs in frame5/dialog5 window.  See issue for details.
- [#1678][i1678] Error reporting improved when MapTool can't start. More likely to get information in log file.
- [#1669][i1669] New helper functions for HTML Overlays
  - `isOverlayRegistered()`, `closeOverlay()`, `getOverlayProperties()`
- [#1597][i1597] Macro functions `setTokenPortrait("")` and `setTokenHandout("")` will now clear their respective images from a token if passed an empty string for asset ID.
- [#1553][i1553] Many of the dialogs in MapTool have been localized so that the text strings are pulled from the I18N translation files.
- [#1528][i1528] New parameter for dialog and frame functions to prevent scrolling on reload.
- [#1518][i1518] PDF image extraction in the Resource Library now supports extaction of full page images.
- [#1506][i1506] MapTool now supports importing Dungeondraft `.dd2vtt` files. This brings in not only the map image but also vision blocking information and the location of light sources.
- [#1473][i1473] Image tokens (image:token) now included in output of `getInfo("client")`.
- [#1463][i1463] List of panels in Window menu is now sorted alphabetically.
- [#1425][i1425] New macro function `overlay()` allows for the creation of multiple transparent HTML map overlays.
- [#1318][i1318] Chat output tweaked so that token names are now part of the first output line keeping long token names from causing the output are from being reduced leaving large empty areas.
- [#975][i975] New GM option to disable use of Edit Token dialog by player clients.
- [#500][i500] Ars Magica Stress Dice roll option.
  - ASnb#+b / ASnb#-b (or asn#+b / asn#-b) - return is a string
  - ANSnb#+b / ANSnb#-b (or ansn#+b / ansn#-b) - return is a number
- [#412][i412] New macro function `getDefinedFunctions()` to get list of user-defined functions. Output of `getInfo("client")` also updated to include location of called function.
- [#27][i27] Reroll Once roll option added.
  - 2d6rk3 - reroll any die less than 3 and keep new result
  - 2d6rc3 - reroll any die less than 3 and keep higher value
- [#368][i368] Macro groups can now be renamed.

## Bug Fixes
- [#1853][i1853] CSS loading in incorrect order for Frame5/Dialog5/Overlay when CSS link points to macro. Fixed.
- [#1838][i1838] Internal changes to make detecting Show as Player within code more consistent.
- [#1831][i1831] math.\* functions always returned `0` as the bad argument when passed non-numeric values. Fixed.
- [#1828][i1828] `getSightType()` would fail on new created tokens. Fixed.
- [#1822][i1822] Changing name/image of impersonated token wasn't updating Chat window. Fixed.
- [#1814][i1814] Several macro functions would fail as not trusted when called by GM in chat. Fixed.
- [#1812][i1812] Call a macro on the GM panel from a trusted context wasn't keeping trusted status. Fixed.
- [#1804][i1804] Map selector reworked to behave like a normal popup menu and be theme compliant.
- [#1803][i1803] PC Tokens on Hidden layer were visible on Player's Map Explorer when Strict Token Ownership was off. Fixed.
- [#1799][i1799] Light segments for Light defitions in the results of `getInfo("campaign")` was malformed. Fixed.
- [#1790][i1790] Several dialogs had text that was hardcoded to the Tahoma font which doesn't support CJK characters. Fixed.
- [#1788][i1788] Pressing delete key with Draw Explorer active and no drawings would throw NPE. Fixed.
- [#1784][i1784] Attempting to use a port outside of valid range would cause start server to fail without a message. Fixed.
- [#1782][i1782] Form `<select multiple>` element only returned one element. Fixed.
- [#1775][i1775] Select next/previous token in Selection window broken (Unreleased). Fixed.
- [#1757][i1757] Macro function `moveToken()` did not correctly handle tokens not centered in the layout config. Fixed.
- [#1752][i1752] Reverted Look & Feel changes to restore native OS title bars because of performance issues and conflicts with MacOS.
- [#1732][i1732] Depending on situation the macro function `getImpersonated/Name()` could return the Current Token or the Impersonated Token. New boolean parameter adds ability to specify returning the actual Impersonated token.
- [#1725][i1725] Selecting token/object via Map Explorer wasn't updating Selection panel. Fixed.
- [#1720][i1720] Token properties with default values were not having spaces trimmed from name. Fixed.
- [#1709][i1709] Portrait images were rendered with low quality scaling. Improved.
- [#1700][i1700] Tokens/stamps showing as "?" image until map is updated. Fixed.
- [#1688][i1688] Figure tokens and tokens flagged as Visible over FoW on hidden layer were visible to players. Fixed.
- [#1686][i1686] Crowdin configuration file was display as option in Preferences -> Language. Fixed.
- [#1675][i1675] Left-click on a token when multiple are already selected wasn't clearing selection on other tokens. Fixed.
- [#1670][i1670] ModelChangeListeners on Impersonate/Selection panels were not getting removed causing a performance hit as more maps were added to campaign. Fixed.
- [#1667][i1667] Fix for token properties issues was not pulled from 1.6.1. Fixed.
- [#1666][i1666] Changing token selection was causing Impersonated panel to update causing a delay. Fixed.
- [#1658][i1658] Deleting a token was causing the Impersonated panel to update cause a delay. Fixed.
- [#1657][i1657] Macros deleted via the Common group were not being updated on clients. Fixed.
- [#1654][i1654] Deleting/cutting multiple tokens cause the TOKEN_REMOVED event to be fired multiple times creating lag as the Selection and Impersonate panels were then reset for eac one. Fixed.
- [#1653][i1653] Changing snap-to-grid status for tokens and stamps could cause them to move. Fixed.
- [#1648][i1648] Null Pointer Exception when toggling Player Movement Lock as non-hosting GM. Fixed.
- [#1646][i1646] Null Pointer Exception when a map is deleted while a token is being edited. Fixed.
- [#1642][i1642] VBL mode button wasn't staying in sync with actual mode. Fixed.
- [#1638][i1638] Macrolinks added via Javascript don't work in frame5/dialog5. Fixed.
- [#1635][i1635] Window decorations in MapTool dialogs and frames changed to pick up color scheme from themes.
- [#1631][i1631] Add Resource dialog wasn't using theme colors. Fixed.
- [#1629][i1629] Campaign Properties dialog was too wide, used strings not in translation files, didn't use theme colors. Fixed.
- [#1614][i1614] Pressing map zoom keys while a frame5 textbox had focus would zoom map. Fixed.
- [#1613][i1613] User selected theme was not being applied to menus causing text to not be rendered for some languages such as Japanese. Fixed.
  - Also fixed Edit Token dialog throwing exception for translated VBL tab name.
- [#1608][i1608] Various menu options would throw an exception if no maps in campaign. Fixed.
- [#1605][i1605] Clicking on FoW button and others after last map was deleted would throw NPE. Fixed.
- [#1589][i1589] Jumpy token movement on all layers, free-size or fixed, snap or non-snap, fixed for all grid types.
- [#1588][i1588] Popup for setting initiative from initiative panel showed placeholder instead of token name. Fixed.
- [#1575][i1575] Unclosed parens in Token properties definition would produce a StringIndexOutOfbounds exception. Fixed.
- [#1572][i1572] Deleting last map with FoW would produce an NPE if Map menu opened. Fixed.
- [#1570][i1570] Attempting to import a macroset as a macro produced exception. Exception caught and error message shown to user.
- [#1568][i1568] Deleting a map while a token was being dragged would produce a NPE. Fixed.
- [#1566][i1566] Saving lib:tokens would fail due to colon in name throwing exception. Fixed.
- [#1564][i1564] Function `playClip()` could fail to create a MediaPlayer throwing an exception. Exception caught and error message presented to user now.
- [#1551][i1551] Javascript `console.log`not working during page load. Fixed.
- [#1548][i1548] Table functions `getTableAccess()`, `getTableVisible()`, `setTableAccess()`, `setTableVisible()`, `getTablePickOnce()` and `setTablePickOnce()` were returning strings instead of BigInteger values. Fixed.
- [#1538][i1538] Close button on Token Editor changed to Cancel.
- [#1501][i1501] "User data directory is already in use" error when using frame5/dialog5 on two instances of MapTool. Fixed.
- [#1498][i1498] Tooltip for Language seletion in Preferences had wrong text. Fixed.
- [#1495][i1495] Chosing Edit on a macro already being edited no longer creates new Find/Replace entries in Edit menu.
- [#1441][i1441] Content of `input()` dialogs wouldn't fill full width of window with minimal prompt/input sizes. Fixed.
- [#1426][i1426] NPE on close caused by editing the same macro twice (simultaneously). Fixed.
- [#1317][i1317] Horizontal scrolling issues on MacOS fixed.
- [#1264][i1264] Fixes for thread-safety in Campaign Auto-Save.
- [#1263][i1263] Fixes for thread-safety in Campaign Loading.
- [#507][i507] The map setting Units per Cell wasn't being handled correctly for locales that use a comma for the decimal point. Fixed.
- [#375][i375] Last save location preserved separately for Tokens, Maps & Campaigns.

## Other
- [#1843][i1843] Refactor of Initiative classes to avoid raw list types.
- [#1776][i1776] dicelib updated to 1.6.1
- [#1704][i1704] dicelib updated to 1.6.0
- [#1223][i1223] Javadocs now build without errors.

[i1853]: https://github.com/RPTools/maptool/issues/1853
[i1847]: https://github.com/RPTools/maptool/issues/1847
[i1843]: https://github.com/RPTools/maptool/issues/1843
[i1838]: https://github.com/RPTools/maptool/issues/1838
[i1831]: https://github.com/RPTools/maptool/issues/1831
[i1828]: https://github.com/RPTools/maptool/issues/1828
[i1822]: https://github.com/RPTools/maptool/issues/1822
[i1814]: https://github.com/RPTools/maptool/issues/1814
[i1812]: https://github.com/RPTools/maptool/issues/1812
[i1808]: https://github.com/RPTools/maptool/issues/1808
[i1804]: https://github.com/RPTools/maptool/issues/1804
[i1803]: https://github.com/RPTools/maptool/issues/1803
[i1801]: https://github.com/RPTools/maptool/issues/1801
[i1799]: https://github.com/RPTools/maptool/issues/1799
[i1797]: https://github.com/RPTools/maptool/issues/1797
[i1790]: https://github.com/RPTools/maptool/issues/1790
[i1788]: https://github.com/RPTools/maptool/issues/1788
[i1784]: https://github.com/RPTools/maptool/issues/1784
[i1782]: https://github.com/RPTools/maptool/issues/1782
[i1776]: https://github.com/RPTools/maptool/issues/1776
[i1775]: https://github.com/RPTools/maptool/issues/1775
[i1765]: https://github.com/RPTools/maptool/issues/1765
[i1764]: https://github.com/RPTools/maptool/issues/1764
[i1758]: https://github.com/RPTools/maptool/issues/1758
[i1757]: https://github.com/RPTools/maptool/issues/1757
[i1752]: https://github.com/RPTools/maptool/issues/1752
[i1732]: https://github.com/RPTools/maptool/issues/1732
[i1728]: https://github.com/RPTools/maptool/issues/1728
[i1725]: https://github.com/RPTools/maptool/issues/1725
[i1720]: https://github.com/RPTools/maptool/issues/1720
[i1709]: https://github.com/RPTools/maptool/issues/1709
[i1704]: https://github.com/RPTools/maptool/issues/1704
[i1700]: https://github.com/RPTools/maptool/issues/1700
[i1688]: https://github.com/RPTools/maptool/issues/1688
[i1686]: https://github.com/RPTools/maptool/issues/1686
[i1678]: https://github.com/RPTools/maptool/issues/1678
[i1675]: https://github.com/RPTools/maptool/issues/1675
[i1670]: https://github.com/RPTools/maptool/issues/1670
[i1669]: https://github.com/RPTools/maptool/issues/1669
[i1667]: https://github.com/RPTools/maptool/issues/1667
[i1666]: https://github.com/RPTools/maptool/issues/1666
[i1658]: https://github.com/RPTools/maptool/issues/1658
[i1657]: https://github.com/RPTools/maptool/issues/1657
[i1654]: https://github.com/RPTools/maptool/issues/1654
[i1653]: https://github.com/RPTools/maptool/issues/1653
[i1648]: https://github.com/RPTools/maptool/issues/1648
[i1646]: https://github.com/RPTools/maptool/issues/1646
[i1642]: https://github.com/RPTools/maptool/issues/1642
[i1638]: https://github.com/RPTools/maptool/issues/1638
[i1635]: https://github.com/RPTools/maptool/issues/1635
[i1631]: https://github.com/RPTools/maptool/issues/1631
[i1629]: https://github.com/RPTools/maptool/issues/1629
[i1614]: https://github.com/RPTools/maptool/issues/1614
[i1613]: https://github.com/RPTools/maptool/issues/1613
[i1608]: https://github.com/RPTools/maptool/issues/1608
[i1605]: https://github.com/RPTools/maptool/issues/1605
[i1597]: https://github.com/RPTools/maptool/issues/1597
[i1589]: https://github.com/RPTools/maptool/issues/1589
[i1588]: https://github.com/RPTools/maptool/issues/1588
[i1575]: https://github.com/RPTools/maptool/issues/1575
[i1572]: https://github.com/RPTools/maptool/issues/1572
[i1570]: https://github.com/RPTools/maptool/issues/1570
[i1568]: https://github.com/RPTools/maptool/issues/1568
[i1566]: https://github.com/RPTools/maptool/issues/1566
[i1564]: https://github.com/RPTools/maptool/issues/1564
[i1553]: https://github.com/RPTools/maptool/issues/1553
[i1551]: https://github.com/RPTools/maptool/issues/1551
[i1548]: https://github.com/RPTools/maptool/issues/1548
[i1538]: https://github.com/RPTools/maptool/issues/1538
[i1528]: https://github.com/RPTools/maptool/issues/1528
[i1518]: https://github.com/RPTools/maptool/issues/1518
[i1513]: https://github.com/RPTools/maptool/issues/1513
[i1506]: https://github.com/RPTools/maptool/issues/1506
[i1501]: https://github.com/RPTools/maptool/issues/1501
[i1498]: https://github.com/RPTools/maptool/issues/1498
[i1495]: https://github.com/RPTools/maptool/issues/1495
[i1473]: https://github.com/RPTools/maptool/issues/1473
[i1463]: https://github.com/RPTools/maptool/issues/1463
[i1441]: https://github.com/RPTools/maptool/issues/1441
[i1426]: https://github.com/RPTools/maptool/issues/1426
[i1425]: https://github.com/RPTools/maptool/issues/1425
[i1318]: https://github.com/RPTools/maptool/issues/1318
[i1317]: https://github.com/RPTools/maptool/issues/1317
[i1264]: https://github.com/RPTools/maptool/issues/1264
[i1263]: https://github.com/RPTools/maptool/issues/1263
[i1223]: https://github.com/RPTools/maptool/issues/1223
[i975]: https://github.com/RPTools/maptool/issues/975
[i507]: https://github.com/RPTools/maptool/issues/507
[i500]: https://github.com/RPTools/maptool/issues/500
[i412]: https://github.com/RPTools/maptool/issues/412
[i375]: https://github.com/RPTools/maptool/issues/375
[i368]: https://github.com/RPTools/maptool/issues/368
[i27]: https://github.com/RPTools/dicelib/issues/27

Maptool 1.6.1
=====
Hotfix for token property issue.

**Bug Fixes**
- [#1504][i1504] Edit Token dialog would hold onto a previous tokens property values and property type. Fixed.

[i1504]: https://github.com/RPTools/maptool/issues/1504

Maptool 1.6.0
=====
Lots of enhancements, bug fixes and improvements to the code base.

**Some Highlights**
- New dialog and frame functions support HTML5, JavaScript and CSS.
- Token VBL Simplification - Generating VBL for large, complex tokens like maps is much faster and producers much tighter VBL that is less demanding.  Also allows for automatic generation via the `setTokenVBL()` macro function.
- Movement Blocking Layer - Now you can make those Force Fields that would block movement but not sight.
- Look & Feel themes can now be selected from the Themes menu.  More enhancements to come.
- Pre-defined Campaign Property sets can now be selected and loaded in the Campaign Properties dialog.
- New Marker assets added to Default in the Resource Library - Use Restore Default Images in the Help menu to get them.
- The annoying behavior where browsing for a drawing texture would cause the Resource Library to also change folders has been fixed!

**Enhancements**
- [#1427][i1427] Improved click response for macro links. Bonus: anchor links `<a href="#xxx">` now work in frames and dialogs.
- [#1382][i1382] Connection Info -> External Address no longer depends on Server Registry being available.
- [#1363][i1363] New and updated Marker assets in the Default Resource Library
- [#1337][i1337] _Pick Once_ support added to Tables. Currently uses macro functions to set/reset the tables.  GUI support to come.
- [#1323][i1323] New menu item, Frameworks, in Help menu opens default browser to [Frameworks](http://www.lmwcs.com/rptools/wiki/Frameworks) page on wiki.
- [#1320][i1320] Downloadable artpacks in the Add Resource to Library dialog are now sorted by name.
- [#1313][i1313] Gather Debug Information window (Help MenU) refactored. Window opens immediately with note that data collection is in progress and then updates once data has been collected.
- [#1278][i1278] JSON option added to PROPS type for `input()` function.  WIDTH option added to PROPS.  Defaults to 14.
- [#1270][i1270] Map Properties dialog now has a delete button to remove embedded maps.
- [#1218][i1218] Token VBL Simplification - major performance increase in generation of VBL for tokens but also in producing VBL that is much less demanding on the system. Has color picker now instead of relying upon transparency.
- [#1216][i1216] Movement Blocking Layer - similar to VBL, but specifically for blocking movement and not sight or light.
  - AI button and new VBL Blocks Movement button moved over by Measuring tool and are now under GM control
- [#1210][i1210] RADIO and LIST fields for `input()` function now support a DELIMITER option.
- [#1107][i1107] Using the line drawing tools while holding Alt will snap to grid centers.
- [#979][i979] New macro functions for setting/getting Token Image Layout properties.
  - `setTokenLayoutProps(scale, xOffset, yOffset, tokenID, mapName)` and ` getTokenLayoutProps()`
- [#972][i972] New macro functions for HTML5 compatible dialogs and frames.
  - `dialog5()` and `frame5()`
- [#921][i921] Support added for selecting an available them via the Themes menu in MapTool.
- [#891][i891] Menu option and macro functions to show/hide Text Labels and get display status.
  - `showTextLabels()`, `hideTextLabels()` and `getTextLabelStatus()`
- [#643][i643] Grid types for Light and Sight now supports Hex grids.
- [#221][i221] Campaign Properties dialog now has a drop-down to select from pre-defined Campaign Property sets (.mtprops).

**Bug Fixes**
- [#1477][i1477] HTML5 form submission ignored *novalidate* and *formnovalidate* attributes. Fixed.
- [#1472][i1472] `getInfo("campaign")` was not returning all states. Fixed.
- [#1469][i1469] Cursor offset when dragging snap-to-grid tokens on background layer. Fixed.
- [#1468][i1468] onChangeSelection event was firing multiple times per selection action. Fixed.
- [#1460][i1460] Uncaught cases in switch statement produces unhelpful error message. Fixed.
- [#1456][i1456] Pressing Enter in an HTML control loses focus - fixed.
- [#1455][i1455] `findToken()` using GM name failed when run via a trusted macro by Player client. Also `getGMName()` & `getGMNotes()` - fixed.
- [#1453][i1453] HTML5 form validation ignored, js form.submit() borken, button w/o type broken - fixed.
- [#1450][i1450] Fixed malfunctioning HTML elements in HTML5 dialog/frames: `<input>`, `<button>`, `<map>`, and `<a>`.
- [#1441][i1441] Title bar on input() dialogs didn't fill width of window. Fixed.
- [#1430][i1430] Macro links weren't working with HTML image maps. Not in relased code. Fixed.
- [#1412][i1412] MBL/VBL was flickering when switching modes. Fixed.
- [#1408][i1408] Draw Explorer drawing previews reflected pen width even with foreground was transparent. Fixed.
- [#1400][i1400] Function `getAllPropertyNames()` was ignoring `delim` parameter if not set to `json`. Fixed.
- [#1381][i1381] Exporting a macro with the same name as an existing `.mtmacro` file was producing a bad message and not warning the user. Fixed.
- [#1309][i1309] Three of the default maps have been missing from the Resource Library for ages. They are back now.
- [#1300][i1300] `copyToken()` macro function bugs when only a token name provided and though succeeding was reporting an error when making multiple copies. Fixed.
- [#1299][i1299] Issue with moving tokens next to VBL related to maps having grid offsets fixed.
- [#1292][i1292] The _setState_ and _unsetStates_ conditions for `getTokens` and `getTokenNames` were returning the wrong information. Similar issues with _propertyType_, _owned_, and _light_ also fixed.
- [#1288][i1288] Token Properties grid in the Edit Token dialog didn't fill the available area. Fixed.
- [#1278][i1278] PROPS option for `input()` broken. Fixed.
- [#1269][i1269] Exporting a campaign with too short a name was throwing an exception. Fixed.
- [#1267][i1267] Null Pointer Exceptions for Zone fields during Auto Save and Campaign Lod. Fixed.
- [#1243][i1243] Once a GRID light source was used on a map, further use on maps with a different grid size would be the wrong size. Fixed.
- [#1236][i1236] `getToken*` functions failing when setState/unsetState condition used. Fixed.
- [#1231][i1231] Choosing a non-writable directory for downloading updates was failing silently. Fixed.
- [#1228][i1228] `getLastPath` was returning bad path information. Fixed.
- [#1225][i1225] Exception during L&F setup. Not in released code. Fixed.
- [#1217][i1217] Moving tokens or measuring across long distance (200+ cells) performed poorly. Fixed.
- [#1206][i1206] JSON function code was returning NULL if the function couldn't be found. Now reports unknown function.
- [#1204][i1204] `getToken*` was throwing an exception if a string was passed to the `layer` option. Fixed.
- [#1092][i1092] Token States, Bars, and Halos weren't rotating properly with off-center, top-down tokens. Fixed.
- [#1049][i1049] Token VBL wasn't rotating properly with off-center, top-down tokens. Fixed.
- [#650][i650] Can't pass JSON data from a form submit. Fixed.
- [#616][i616] Token selection issue after combination dragging token and panning map. Fixed.
- [#472][i472] Edit Token Dialog gets slower to open with each subsequent reopen. Fixed.
- [#198][i198] Using tab panels in `input()` could add large empty space at bottom of panel. Fixed.

**Other**
- [#1432][i1432] Updated splash & install images to remove Dev Release notations.
- [#1355][i1355] Moving remaining classes off old JSON library to GSON.
- [#1352][i1352] Replaced use of `"UTF-i"` by `StandardCharsets.UTF_8`
- [#1325][i1325] Unit tests added for ImageUtils.
- [#1284][i1284] Removed superfluous ParserException try/catch blocks.
- [#1271][i1271] Clean up of Drawing Delete confirmation dialog code with comments and javadocs plus comments on the I18N strings.
- [#1268][i1268] Refactored PackeFile class to use *try with resource* instead of try/catch/finally blocks.
- [#1253][i1253] MD5 key generation wasn't thread-safe. Fixed.
- [#1219][i1219] Javadocs generated via `gradlew javadoc` switched to HTML5.
- [#1215][i1215] Replaced calls to deprecated IOUtils.closeQuietly() with try-with-resource.
- [#1189][i1189] Unit tests added for JSON macro functions.

[i1477]: https://github.com/RPTools/maptool/issues/1477
[i1472]: https://github.com/RPTools/maptool/issues/1472
[i1469]: https://github.com/RPTools/maptool/issues/1469
[i1468]: https://github.com/RPTools/maptool/issues/1468
[i1460]: https://github.com/RPTools/maptool/issues/1460
[i1456]: https://github.com/RPTools/maptool/issues/1456
[i1455]: https://github.com/RPTools/maptool/issues/1455
[i1453]: https://github.com/RPTools/maptool/issues/1453
[i1450]: https://github.com/RPTools/maptool/issues/1450
[i1441]: https://github.com/RPTools/maptool/issues/1441
[i1432]: https://github.com/RPTools/maptool/issues/1432
[i1430]: https://github.com/RPTools/maptool/issues/1430
[i1427]: https://github.com/RPTools/maptool/issues/1427
[i1412]: https://github.com/RPTools/maptool/issues/1412
[i1408]: https://github.com/RPTools/maptool/issues/1408
[i1400]: https://github.com/RPTools/maptool/issues/1400
[i1382]: https://github.com/RPTools/maptool/issues/1382
[i1381]: https://github.com/RPTools/maptool/issues/1381
[i1363]: https://github.com/RPTools/maptool-resources/issues/2
[i1355]: https://github.com/RPTools/maptool/issues/1355
[i1352]: https://github.com/RPTools/maptool/issues/1352
[i1337]: https://github.com/RPTools/maptool/issues/1337
[i1325]: https://github.com/RPTools/maptool/issues/1325
[i1323]: https://github.com/RPTools/maptool/issues/1323
[i1320]: https://github.com/RPTools/maptool/issues/1320
[i1313]: https://github.com/RPTools/maptool/issues/1313
[i1309]: https://github.com/RPTools/maptool/issues/1309
[i1300]: https://github.com/RPTools/maptool/issues/1300
[i1299]: https://github.com/RPTools/maptool/issues/1299
[i1292]: https://github.com/RPTools/maptool/issues/1292
[i1288]: https://github.com/RPTools/maptool/issues/1288
[i1284]: https://github.com/RPTools/maptool/issues/1284
[i1278]: https://github.com/RPTools/maptool/issues/1278
[i1271]: https://github.com/RPTools/maptool/issues/1271
[i1270]: https://github.com/RPTools/maptool/issues/1270
[i1269]: https://github.com/RPTools/maptool/issues/1269
[i1268]: https://github.com/RPTools/maptool/issues/1268
[i1267]: https://github.com/RPTools/maptool/issues/1267
[i1253]: https://github.com/RPTools/maptool/issues/1253
[i1243]: https://github.com/RPTools/maptool/issues/1243
[i1236]: https://github.com/RPTools/maptool/issues/1236
[i1231]: https://github.com/RPTools/maptool/issues/1231
[i1228]: https://github.com/RPTools/maptool/issues/1228
[i1225]: https://github.com/RPTools/maptool/issues/1225
[i1219]: https://github.com/RPTools/maptool/issues/1219
[i1218]: https://github.com/RPTools/maptool/issues/1218
[i1217]: https://github.com/RPTools/maptool/issues/1217
[i1216]: https://github.com/RPTools/maptool/issues/1216
[i1215]: https://github.com/RPTools/maptool/issues/1215
[i1210]: https://github.com/RPTools/maptool/issues/1210
[i1206]: https://github.com/RPTools/maptool/issues/1206
[i1204]: https://github.com/RPTools/maptool/issues/1204
[i1189]: https://github.com/RPTools/maptool/issues/1189
[i1107]: https://github.com/RPTools/maptool/issues/1107
[i1092]: https://github.com/RPTools/maptool/issues/1092
[i1049]: https://github.com/RPTools/maptool/issues/1049
[i979]: https://github.com/RPTools/maptool/issues/979
[i972]: https://github.com/RPTools/maptool/issues/972
[i921]: https://github.com/RPTools/maptool/issues/921
[i891]: https://github.com/RPTools/maptool/issues/891
[i650]: https://github.com/RPTools/maptool/issues/650
[i643]: https://github.com/RPTools/maptool/issues/643
[i616]: https://github.com/RPTools/maptool/issues/616
[i472]: https://github.com/RPTools/maptool/issues/472
[i221]: https://github.com/RPTools/maptool/issues/221
[i198]: https://github.com/RPTools/maptool/issues/198


Maptool 1.5.14
=====
**Highlights**
- Bug fix release.

**Bug Fixes**
- [#1326][i1326] `json.path.read()` was turning numbers into strings when using the max, min, avg, length, sum and stddev path functions. Fixed.
- [#1300][i1300] Several bugs with `copyTokens()` when used with/without updates parameter fixed.

[i1326]: https://github.com/RPTools/maptool/issues/1326
[i1300]: https://github.com/RPTools/maptool/issues/1300


Maptool 1.5.13
=====
**Highlights**
- Bug fix release to correct several JSON related issues.

**Bug Fixes**
- [#1296][i1296] Exception thrown when trying to read Hero Lab .por files that are missing the XML statblock. Exception caught and error reported.
- [#1236][i1236] `getTokens/getTokenNames()` was failing if `setState` or `unsetState` options were used. Fixed.
- [#1228][i1228] `getLastPath()` and other token move functions that made use of pathPointsToJsonArray were broken. Fixed.
- [#1206][i1206] Unknown json functions would return null instead of an error. Fixed.
- [#1204][i1204] Layer condition of `getTokens/getTokenNames()` weren't accepting accepting a string for a single layer. Fixed.

[i1296]: https://github.com/RPTools/maptool/issues/1296
[i1236]: https://github.com/RPTools/maptool/issues/1236
[i1228]: https://github.com/RPTools/maptool/issues/1228
[i1206]: https://github.com/RPTools/maptool/issues/1206
[i1204]: https://github.com/RPTools/maptool/issues/1204

Maptool 1.5.11/12
=====
**Highlights**
- Terrain Modifier enhancements:
  - Token terrain modifier can be flagged as NONE, MULTIPLY, ADD, BLOCK and FREE
  - Tokens can be set to ignore Terrain Modifiers.
  - Per map setting for rounding of fractional movement costs: NONE, CELL_UNIT, INTEGER
- Can now specify a campaign file on startup:
  - With command line options `-F` or `-file`, e.g. `-file=path/to/file/mycampaign.cmpgn`
  - Passing the path and file without command line switches, e.g. `maptool path/to/mycampaign.cmpgn`
  - File assocation - will need to manually configure this through the appropriate OS settings
  - Drag-n-drop of campaign file on executable.
- Major refactoring of use of JSON in code. Replaced all uses of net.sf.json-lib library with the Google GSON library.

**Enhancements**
- [#1178][i1178] New, per map, rounding options for AI movement costs: NONE, CELL_UNIT, INTEGER
- [#1165][i1165] New command line option for loading campaign on startup.
- [#1142][i1142] New macro function `getTokenMap(id,delim)`gets a maps with the provided token ID/name.
- [#1101][i1101] New chat commands `/version` and `/about`. MapTool version added to title bar.
- [#1072][i1072] Meta macro functions now accept a map name parameter.
  - `createMacro`, `getMacroCommand`, `getMacroIndexes`, `getMacroProps`, `getMacros`, `removeMacro`, `setMacroCommand`, `setMacroProps`
- [#1062][i1062] New Light preference setting for Maps. Can now be set to default to None, Day or Night. Same option is available on Map Properties dialog.
- [#941][i941] Hitting the tab key in the property fields of the Edit Token Dialog will now advance to the next property value instead of property name.
- [#870][i870] New configuration options for `json.path.read` function.
  - ALWAYS_RETURN_LIST, AS_PATH_LIST, DEFAULT_PATH_LEAF_TO_NULL, REQUIRE_PROPERTIES, SUPPRESS_EXCEPTIONS
- [#728][i728] Tokens can now be set to ignore one or more types of terrain modifiers.
- [#459][i459] Terrain Modifiers have multiple types now: None, Add, Multiply, Block and Free.

**Bug Fixes**
- [#1178][i1178] Some AI distance calculations were off when using terrain mods. Fixed.
- [#1177][i1177] `json.difference()` wasn't promoting strings to JSON. Not in released code. Fixed.
- [#1175][i1175] `getTokenNames()` and `getTokens()` was failing when _mapname_ condition was used. Not in released code. Fixed.
- [#1173][i1173] Macrolink argument encoding/decoding failing. Not in released code. Fixed.
- [#1167][i1167] `json.isEmpty` was not promoting passed strings to JSON array. Not in released code. Fixed.
- [#1151][i1151] `json.set` was adding extra quotes when setting value to empty string. Not in released code. Fixed.
- [#1149][i1149] `execFunction` failed when defer = 1 and a trusted function was used. Fixed.
- [#1144][i1144] `json.get` was not promoting strings/numbers to arrays. Not in released code. Fixed.
- [#1143][i1143] `json.toList` was not removing quotes around strings. Not in released code. Fixed.
- [#1139][i1139] Whitespace trimmed off strings containing numbers and converted to numbers. Not in released code. Fixed.
- [#1130][i1130] `foreach()` not working with JSON objects. Not in released code. Fixed.
- [#1127][i1127] Logging was broken by recent lib change for 1.5.9/10.  Also log files were not being zipped or pruned. Fixed.
- [#1125][i1125] Using `json.type` in an `if()` would produce an invalid condition error.  Fixed.
- [#1124][i1124] UDFs were being passed `null` when an empty string was sent. Not in released code. Fixed.
- [#1121][i1121] Function `json.get` was returning `null` if field didn't exist. Not in released code. Fixed.
- [#1120][i1120] Function `json.toVars` was adding quotes around the set values. Not in released code. Fixed.
- [#1117][i1117] Strings returned by UDFs were double-quotted. Not in released code.  Fixed.
- [#1075][i1075] Setting `applyToSelected=1` when using `createMacro()` was not being applied. Fixed.
- [#1069][i1069] AutoSave failure message changed.
- [#1066][i1066] SENTRY: When selecting images for tokens a null asset could be returned. Caught and error message displayed.
- [#1060][i1060] Labels, halos and selection boxes were incorrectly rendered on tokens with off-center layout in config. Fixed.
- [#1047][i1047] `json.contains` and `json.indexOf` were not handling values outside the range of a signed integer. Fixed.
- [#1015][i1015] A bad HREF in a anchor link could produce an NPE.  Fixed.
- [#456][i456] Tokens with VBL didn't show path and couldn't have movement reverted.  Fixed.
- [#413][i413] Facing could be changed when zooming in/out on Mac.  Fixed.
- [#353][i353] Macro Editor wasn't persisting window size.  Fixed.

**Note** The Windows install for this release requires Windows 7 or greater to install.  The `.jar` file release can still be used on older 64-bit Windows platform. [#1039][i1039]

[i1178]: https://github.com/RPTools/maptool/issues/1178
[i1177]: https://github.com/RPTools/maptool/issues/1177
[i1175]: https://github.com/RPTools/maptool/issues/1175
[i1173]: https://github.com/RPTools/maptool/issues/1173
[i1167]: https://github.com/RPTools/maptool/issues/1167
[i1165]: https://github.com/RPTools/maptool/issues/1165
[i1151]: https://github.com/RPTools/maptool/issues/1151
[i1149]: https://github.com/RPTools/maptool/issues/1149
[i1144]: https://github.com/RPTools/maptool/issues/1144
[i1143]: https://github.com/RPTools/maptool/issues/1143
[i1142]: https://github.com/RPTools/maptool/issues/1142
[i1139]: https://github.com/RPTools/maptool/issues/1139
[i1130]: https://github.com/RPTools/maptool/issues/1130
[i1127]: https://github.com/RPTools/maptool/issues/1127
[i1125]: https://github.com/RPTools/maptool/issues/1125
[i1124]: https://github.com/RPTools/maptool/issues/1124
[i1121]: https://github.com/RPTools/maptool/issues/1121
[i1120]: https://github.com/RPTools/maptool/issues/1120
[i1117]: https://github.com/RPTools/maptool/issues/1117
[i1101]: https://github.com/RPTools/maptool/issues/1101
[i1075]: https://github.com/RPTools/maptool/issues/1075
[i1072]: https://github.com/RPTools/maptool/issues/1072
[i1069]: https://github.com/RPTools/maptool/issues/1069
[i1066]: https://github.com/RPTools/maptool/issues/1066
[i1062]: https://github.com/RPTools/maptool/issues/1062
[i1060]: https://github.com/RPTools/maptool/issues/1060
[i1047]: https://github.com/RPTools/maptool/issues/1047
[i1039]: https://github.com/RPTools/maptool/issues/1039
[i1015]: https://github.com/RPTools/maptool/issues/1015
[i941]: https://github.com/RPTools/maptool/issues/941
[i870]: https://github.com/RPTools/maptool/issues/870
[i728]: https://github.com/RPTools/maptool/issues/728
[i459]: https://github.com/RPTools/maptool/issues/459
[i456]: https://github.com/RPTools/maptool/issues/456
[i413]: https://github.com/RPTools/maptool/issues/413
[i353]: https://github.com/RPTools/maptool/issues/353

Maptool 1.5.10
=====
Emergency bug fixes

- Fix for exception when deleting multiple tokens.
- Fixed German translations so macro editor and menu shortcuts work.

Bug Fixes
-----
- [#1079][i1079] Selecting and then deleting multiple tokens caused ConcurrentModification Exception.  Fixed.
- [#1078][i1078] Translations for German language broke a number of shortcuts and macro editor. Fixed.

[i1079]: https://github.com/RPTools/maptool/issues/1079
[i1078]: https://github.com/RPTools/maptool/issues/1078

Maptool 1.5.9
=====
This release focused on clearing out bugs both old and new.

**Highlights**
- Bug fixes. Some serious and some just quality of life.
- More translation updates.
- Brought the various libraries used by MapTool up to current releases.
- Shout out to Melek and other volunteers for their work on 20+ new and improved wiki pages .

Enhancements
-----
- [#753][i753] Reserved variable names `init.current` and `init.round` are now highlighted in macro editor and documented on wiki.
- [#359][i359] Added infomation from `getInfo("server")` and `getInfo("campaign")` to the Sentry error logging.

Bug Fixes
-----
- [#1024][i1024] Macro function `execFunction()` would choke on apostrophes. Fixed.
- [#1021][i1021] Confirmation dialog for clearing GM panel said "Campaign". Fixed.
- [#1006][i1006] The Perlin noise option for maps was causing significant memory usage whether enabled or not. The additional overhead (~28MB per map) is now only incurred when it is enabled on a map.  If using this feature, be mindful of how many maps you have in your campaign and your max memory allocation.
- [#998][i998] Player clients dropping tokens with duplicate names weren't getting a notice of why nothing happened. Fixed.
- [#993][i993] All clients were getting notified when a GM changed a token name to a duplicate. Fixed.
- [#989][i989] Unprintable character symbol (□) showed in output for rolls and tooltips in frames/dialogs. Fixed.
- [#962][i962] The GM-mode autoexpose (Preferences -> Application -> Auto-expose fog...) wasn't working. Fixed.
- [#943][i943] When using random token numbering and after a restart of MT, it was possible to get duplicate token names. Fixed.
- [#923][i923] Using Edit Map on a map with an adjusted grid would lose offset adjustment. Fixed.
- [#920][i920] The RPTools logo was being loaded across the net each time for use in the MacOS dock. Fixed.
- [#892][i892] Panels could be permanently checked in Window menu but not visible. Fixed.
- [#796][i796] Drawing a selection box on layers other than the Token layer ignored the "Fill selection box" preference. Fixed.
- [#739][i739] Initiative info was being returned by `getInfo("client")` and `getInfo("server")`. Removed from `server` option.
- [#722][i722] Find/replace action in Macro editor didn't give a Replace option.  Fixed.
- [#272][i272] Display area for memory usage didn't adapt to larger font sizes and string length. Fixed.
- [#251][i251] NPE when restoring FoW after deleting a token. Fixed.
- [#205][i205] Layer list in Drawing Tools allowed multi-select of layers. Fixed.

Other
-----
- [#1045][i1045] Update remaining dependencies.
- [#1037][i1037] Updated dependencies with reported vulnerabilites.

[i1045]: https://github.com/RPTools/maptool/issues/1045
[i1037]: https://github.com/RPTools/maptool/issues/1037
[i1024]: https://github.com/RPTools/maptool/issues/1024
[i1021]: https://github.com/RPTools/maptool/issues/1021
[i1006]: https://github.com/RPTools/maptool/issues/1006
[i998]: https://github.com/RPTools/maptool/issues/998
[i993]: https://github.com/RPTools/maptool/issues/993
[i989]: https://github.com/RPTools/maptool/issues/989
[i962]: https://github.com/RPTools/maptool/issues/962
[i943]: https://github.com/RPTools/maptool/issues/943
[i923]: https://github.com/RPTools/maptool/issues/923
[i920]: https://github.com/RPTools/maptool/issues/920
[i892]: https://github.com/RPTools/maptool/issues/892
[i796]: https://github.com/RPTools/maptool/issues/796
[i753]: https://github.com/RPTools/maptool/issues/753
[i739]: https://github.com/RPTools/maptool/issues/739
[i722]: https://github.com/RPTools/maptool/issues/739
[i359]: https://github.com/RPTools/maptool/issues/359
[i272]: https://github.com/RPTools/maptool/issues/272
[i251]: https://github.com/RPTools/maptool/issues/251
[i205]: https://github.com/RPTools/maptool/issues/205

Maptool 1.5.8
=====

**Highlights**
- Macros on copied tokens having duplicate UUIDs problem fixed.
- Toolbar/ribbon in MapTool can now be hidden to give you more screenspace.
- Additional parameters and improvements to various macro functions.
- New translation team members, Deniz Köse and Vitaly Karpenko have bumped our Russian translation up to 74%.
- Fog of War exposure fixes.
- Tooltips added to all menu items.
- Bug fixes!

Enhancements
-----
- [#949][i949] New macro function `getAssetProperties()` returns the properties of assets including size and type.
- [#938][i938] Map name parameter added to Token Location functions. All Token Location functions now report correctly when invalid number of parameters are given.
  - Map name parameter added to: `getTokenX()`, `getTokenY()`, `getTokenDrawOrder()`, `setTokenDrawOrder()`
- [#901][i901] Default stack size increased to 8MB.  See Preferences -> Startup
- [#898][i898] Macro functions `getOwned()` and `getOwnedNames` now take optional map name parameter.
- [#897][i897] Toolbar ribbon now has a hide/show gadget.
- [#742][i742] Macro functions `getTokens()` and `getTokensNames()` have new `owned` `conditions` tag for getting tokens based on ownership.
- [#154][i154] Tooltip for Shown Token Names menu option now includes description of what the colors mean.  Localized Tooltips added for all menu items.

Bug Fixes
-----
- [#952][i952] Macro function `setTokenImage()` was not setting the token native height/width fields. Fixed.
- [#945][i945] Image Chooser for selecting table images would throw an exception when clicked between images.  Fixed.
- [#932][i932] Clients were not being updated if setTokenVBL was used to clear the VBL of a token.  Fixed.
- [#912][i912] Initiative functions `addToInitiative()`, `setInitiative()`, and `setInitiativeHold()` were not reliable.  Fixed.
- [#907][i907] Macros on copied tokens had same UUIDs as the parent token.  This is bad.  Fixed.
- [#887][i887] When using Individual Views and Individual Fog of War, the server was not showing previously exposed areas after the server start.  Fixed.
- [#792][i792] The various methods of exposing only the currently visible areas were not working correctly leaving previously exposed areas still exposed. Fixed.
- [#762][i762] Autosave for campaign was not working correctly.  Fixed.
- [#595][i595] Changing maps while a token move was in process could leave the move path stuck on map and token unable to move.  Fixed.
- [#333][i333] Startup tab in Preferences was missing tooltips for several items.  Tooltips added.  Tooltips on that tab are now localized.
- [#197][i197] Hitting the `esc` key while in the chat window would close chat window.  No longer does this.


[i952]: https://github.com/RPTools/maptool/issues/952
[i949]: https://github.com/RPTools/maptool/issues/949
[i945]: https://github.com/RPTools/maptool/issues/945
[i938]: https://github.com/RPTools/maptool/issues/938
[i932]: https://github.com/RPTools/maptool/issues/932
[i912]: https://github.com/RPTools/maptool/issues/912
[i907]: https://github.com/RPTools/maptool/issues/907
[i901]: https://github.com/RPTools/maptool/issues/901
[i898]: https://github.com/RPTools/maptool/issues/898
[i897]: https://github.com/RPTools/maptool/issues/897
[i887]: https://github.com/RPTools/maptool/issues/887
[i792]: https://github.com/RPTools/maptool/issues/792
[i762]: https://github.com/RPTools/maptool/issues/762
[i742]: https://github.com/RPTools/maptool/issues/742
[i595]: https://github.com/RPTools/maptool/issues/595
[i333]: https://github.com/RPTools/maptool/issues/333
[i197]: https://github.com/RPTools/maptool/issues/197
[i154]: https://github.com/RPTools/maptool/issues/154


Maptool 1.5.7
=====

**Highlights**
- New accessibility features allows the user to apply [Perlin noise](https://en.wikipedia.org/wiki/Perlin_noise) to tiled textures to reduce obvious patterning.
- New user preference (Edit -> Preferences -> Application -> Performance) for Max Frame Rate allows users to set a desired Max Frame Rate.
- More updated macro functions for improved performance and reliability.
- New GM macro panel for GM only macros. These macros are saved as part of the campaign.
- Bug fixes!

Enhancements
-----
- [#878][i878] ISO 8601 time date field added to result of `getInfo("client")`.
- [#872][i872] Metadata added to JSON returned from `getMacroProps` function to add in external managment of macros.
- [#850][i850] New macro function `log.trace` added for log messages at `TRACE` level.
- [#848][i848] File Sync Directory preference setting adding to `getInfo("client")` result.
- [#829][i829] New macro function `capitalize` will change the first character at each word break to upper case.  e.g. "jolly green giant" -> "Jolly Green Giant".
- [#810][i810] New macro function `playClip` more suited for short sound FX clips. New convenience function `defineAudioSource` to allow you to assign a nickname to audio sources and refer to the nickname in the audio functions.  Two existing audio functions were renamed as part of the other changes:  `stopStream` -> `stopSound`, `getStreamProperties` -> `getSoundProperties`.
- [#804][i804] Chat notification flash now picks flash color based on task bar color.
- [#801][i801] Macro function `getTokenStates` now accepts Token ID and Map Name parameters.
- [#790][i790] New `execFunction` macro that works like `execLink` but is used for built-in macro functions and UDFs.
- [#784][i784] Macro function `json.toVars` now accepts JSON arrays as well as objects.
- [#782][i782] Macro function `broadcast` no accepts "not-gm", "not-self" and "not-gm-self" as targets.
- [#766][i766] New perlin noise overlay on tiled textures to reduce visible repetition.
- [#761][i761] Bulk macro function changes.
  - These functions no longer force whole token updates when used: addToInitiative, setInitiative, setInitiativeHold, setBar, setBarVisible, setName, setGMName, setHalo, setTokenOpacity, setTokenImage, setTokenPortrait, getTokenHandout, setLabel, resetProperty, setTerrainModifier, setVisible, setOwnerOnlyVisible, setAlwaysVisible, setTokenVBL
  - These functions now accept additional Token ID and Map Name parameters: setBar, getBar, isBarVisible, setBarVisible, addToInitiative, setInitiative, setInitiativeHold
- [#745][i745] Macro functions `getTokens` and `getTokenNames` now take optional Light condition for getting tokens with lights.
- [#642][i642] New GM macro panel can be opened from the Window menu. Macros on the GM panel are not visible to players.

Bug Fixes
-----
- [#883][i883] Command key shortcuts on MacOS not working.  Fixed.
- [#874][i874] `REST.delete` did not support a header and payload.  Fixed.
- [#846][i846] `getInfo("server")` was returning true/false instead of 1/0 for "hosting server". Fixed.
- [#831][i831] Macro function `json.path.read` was returning numbers as strings.  Fixed.
- [#822][i822] `playStream` was ignoring stream parameters set with `editStream`. Fixed.
- [#820][i820] Functions `execLink` and `execFunction` were not running immediately on local client. Fixed.
- [#814][i814] Some `update` keywords for `copyToken` were producing exceptions. Fixed. Alternate keywords *tokenPortrait* and *tokenHandout* added
- [#803][i803] `getStreamProps` was returning malformed JSON. Fixed.
- [#800][i800] Incorrect tooltip on Chat Notification Background preference. Fixed.
- [#788][i788] Player clients were showing the last campaign file they had loaded in the title bar when connected to servers. Fixed.
- [#786][i786] Bugs with the various bar functions returning incorrect error messages or no error when passed a bad bar name. Fixed.
- [#775][i775] `json.path.read` was returning invalid JSON for JSON arrays of objects. Fixed.
- [#769][i769] Tokens created with `copyToken` could not be modified in same macro without jumping through hoops. Fixed.
- [#767][i767] A recent change to improve program responsiveness had capped frame rate at 30 fps making for jerky map panning. Default is now 60 fps and can be adjusted in preferences under Application -> Performance -> Max Frame Rate.  Note either reloading the current campaign or restarting MapTool is required after making a change.
- [#740][i740] Selecting New Map in the Library image pane with no image underneath would thrown an exception. Fixed.
- [#687][i687] The table functions `addTableEntry`, `createTable`, `setTableImage` and `setTableEntry` if passed an empty `AssetID` string would incorrectly put an empty "Asset://" into the entries asset id field. Fixed.  `getTableImage` would thrown an exeption if no table image was set. Fixed.
- [#640][i640] Workaround for errors which occured when three monitors are in use.  Related exceptions caught and information is logged.
- [#627][i627] Version check on MapTool startup should no longer prompt for updates when using release candidates.
- [#529][i529] Smileys are now working again.

[i883]: https://github.com/RPTools/maptool/issues/883
[i878]: https://github.com/RPTools/maptool/issues/878
[i874]: https://github.com/RPTools/maptool/issues/874
[i872]: https://github.com/RPTools/maptool/issues/872
[i850]: https://github.com/RPTools/maptool/issues/850
[i848]: https://github.com/RPTools/maptool/issues/848
[i846]: https://github.com/RPTools/maptool/issues/846
[i831]: https://github.com/RPTools/maptool/issues/831
[i829]: https://github.com/RPTools/maptool/issues/829
[i822]: https://github.com/RPTools/maptool/issues/822
[i820]: https://github.com/RPTools/maptool/issues/820
[i814]: https://github.com/RPTools/maptool/issues/814
[i810]: https://github.com/RPTools/maptool/issues/810
[i804]: https://github.com/RPTools/maptool/issues/804
[i803]: https://github.com/RPTools/maptool/issues/803
[i801]: https://github.com/RPTools/maptool/issues/801
[i800]: https://github.com/RPTools/maptool/issues/800
[i790]: https://github.com/RPTools/maptool/issues/790
[i788]: https://github.com/RPTools/maptool/issues/788
[i786]: https://github.com/RPTools/maptool/issues/786
[i784]: https://github.com/RPTools/maptool/issues/784
[i782]: https://github.com/RPTools/maptool/issues/782
[i775]: https://github.com/RPTools/maptool/issues/775
[i769]: https://github.com/RPTools/maptool/issues/769
[i767]: https://github.com/RPTools/maptool/issues/767
[i766]: https://github.com/RPTools/maptool/issues/766
[i761]: https://github.com/RPTools/maptool/issues/761
[i745]: https://github.com/RPTools/maptool/issues/745
[i740]: https://github.com/RPTools/maptool/issues/740
[i687]: https://github.com/RPTools/maptool/issues/687
[i642]: https://github.com/RPTools/maptool/issues/642
[i640]: https://github.com/RPTools/maptool/issues/640
[i627]: https://github.com/RPTools/maptool/issues/627
[i529]: https://github.com/RPTools/maptool/issues/529

Maptool 1.5.6
=====
Emergency fix for MacOS.  Otherwise the same as 1.5.5.

Bug Fixes
-----
* [#763][i763] Fixed null-pointer exception thrown on launch for MacOS.

[i763]: https://github.com/RPTools/maptool/issues/763

Maptool 1.5.5
=====
Several contributors have brought us new features including a scroll bar added to the Select Map drop-down, audio macro functions, performance and UI improvements, and, of course, bug fixes.

Enhancements
-----
* [#718][i718] Added new keywords to the `broadcast` macro function: "self", "all", "gm-self" and "none".
* [#716][i716] Added new parameters *targets* and *delim* to macro function `execLink`.  Accepts a list of players or the following keywords: "gm", "self", "all", "gm-self" and "none".
* [#709][i709] New volume slider and mute button now control systems sounds as well as audio streams.
* [#708][i708] Significant performance improvements as well as quality improvements made to Resource Library image panel.
* [#676][i676] New _PropertyType_ option for `getTokens()` to allow getting only tokens with the specified property type.
* [#667][i667] New macro functions for playing audio on the local client.
  * playStream() - plays local or remote audio files (.mp3 & .wav).
  * stopStream() - stops the specified stream.
  * editStream() - modifies a playing stream.
  * getStreamProperties() - get the properties of loaded/playing streams.
* [#665][i665] New server option _GM reveals vision On Movement for Unowned Tokens_. If unchecked (default) GM movement of unowned tokens with vision will not expose FoW.
* [#663][i663] New getInfo("server") info added: _isHostingServer_ and _gmRevealsVision_
* [#649][i649] Zoom macro functions updated to use consolidated code. Bugs fixed.
  * getZoom() & setViewArea() were no displaying errors when too many parameters passed.
  * getViewCenter() was returning extra delimiter
* [#629][i629] Parameter count error messages for macro functions will now include the name of the function.
* [#613][i613] Multiple instances of checkNumberOfParameters() and getTokenFromParam() were declared in different macro function classes.  Consolidated into FunctionUtil class.  No change for end users.
* [#612][i612] New macro JSON functions for deep access to complex JSON objects.
  * json.path.add()
  * json.path.delete()
  * json.path.read()
  * json.path.set()
  * json.path.put()
* [#591][i591] New macro function `json.toVars()` converts the key values of a JSON object to variables holding the associated values.
* [#356][i356] Select Map drop-down now has as scroll bar to handle large numbers of maps.

Bug Fixes
-----
* [#751][i751] PDF extraction of JPEG2000 images was broken.  Fixed.
* [#746][i746] Move to current dicelib version to that multiple parser versions are not being pulled in.
* [#731][i731] Code cleanup so that JavaDoc generation works.
* [#724][i724] Macro functions `getViewArea` and `getViewCenter` were returning _zoomed_ map pixels.  Fixed.
* [#713][i713] Campaign macro changes made after server started were not propagating to clients.  Fixed.
* [#700][i700] Exception thrown when closing MapTool and macro editor is open.  Fixed.
* [#699][i699] `getDistance` macro function was returning erratic results in some cases.  Fixed.
* [#696][i696] MapTool was overly sensitive to mouse movement when trying to right-click on tokens.  Fixed.
* [#694][i694] Resource Library image panel zoom was temporarily broken.  Fixed.
* [#688][i688] Macro distance functions were returning incorrect values if diagonal move metric "Manhattan" is used. Distance functions were ignoring metric on Isometric maps. Fixed.
  * Affected macros: `getDistance()`, `getDistanceToXY()`, `getTokens()`
* [#684][i684] Macro function `getDistanceToXY()` returned incorrect values if **NO_GRID** metric was used.  Fixed.
* [#683][i683] Macro function `getTokens()` was very slow when _distance_ condition was used. On maps with 7000+ tokens the speed increase varies from 3x to 12x depending on grid type.  **Fixed.**
* [#681][i681] Using "*" wildcard in `stopStream()` and `getStreamProperties()` stopped working.  Fixed.
* [#679][i679] Continuous integration builds were failing.  Fixed.
* [#670][i670] Macro function json.set() and json.put() will now put in the actual Java data types `null`, `true` and `false` when passed those strings.
* [#658][i658] Sometimes Send to Back and Bring to Front right-click menu options didn't work.  Fixed.
* [#653][i653] _Select Map_ button did not have i18n translation key.  Fixed and French translation added.
* [#637][i637] Incorrect error message from `asset()` if second parameter is a number.  Fixed.
* [#624][i624] Cut and paste of a token would result in an ID change. ID is now kept for the first paste after a cut action.
* [#621][i621] Paste from the Edit menu was not being enabled after Cut/Copy from the right-click menu or after a drag-n-drop operation.  Fixed.
* [#619][i619] Token IDs were changing after a server start.  Fixed.
* [#601][i601] Switching to Pointer tool from Measuring tool wasn't updating the mouse pointer immediately.  Fixed.
* [#328][i328] Concurrent Modification Exception thrown by AbstractZoneWalker. Synchronization/locking added to partialPaths list handling.  Fixed.
* [#187][i187] GM tokens with vision were exposing map areas to players. Fixed.  See also [#665][i665].


[i751]: https://github.com/RPTools/maptool/issues/751
[i746]: https://github.com/RPTools/maptool/issues/746
[i731]: https://github.com/RPTools/maptool/issues/731
[i724]: https://github.com/RPTools/maptool/issues/724
[i718]: https://github.com/RPTools/maptool/issues/718
[i716]: https://github.com/RPTools/maptool/issues/716
[i713]: https://github.com/RPTools/maptool/issues/713
[i709]: https://github.com/RPTools/maptool/issues/709
[i708]: https://github.com/RPTools/maptool/issues/708
[i700]: https://github.com/RPTools/maptool/issues/700
[i699]: https://github.com/RPTools/maptool/issues/699
[i696]: https://github.com/RPTools/maptool/issues/696
[i694]: https://github.com/RPTools/maptool/issues/694
[i688]: https://github.com/RPTools/maptool/issues/688
[i684]: https://github.com/RPTools/maptool/issues/684
[i683]: https://github.com/RPTools/maptool/issues/683
[i681]: https://github.com/RPTools/maptool/issues/681
[i679]: https://github.com/RPTools/maptool/issues/679
[i676]: https://github.com/RPTools/maptool/issues/676
[i670]: https://github.com/RPTools/maptool/issues/670
[i667]: https://github.com/RPTools/maptool/issues/667
[i665]: https://github.com/RPTools/maptool/issues/665
[i663]: https://github.com/RPTools/maptool/issues/663
[i658]: https://github.com/RPTools/maptool/issues/658
[i653]: https://github.com/RPTools/maptool/issues/653
[i649]: https://github.com/RPTools/maptool/issues/649
[i637]: https://github.com/RPTools/maptool/issues/637
[i629]: https://github.com/RPTools/maptool/issues/629
[i624]: https://github.com/RPTools/maptool/issues/624
[i621]: https://github.com/RPTools/maptool/issues/621
[i619]: https://github.com/RPTools/maptool/issues/619
[i613]: https://github.com/RPTools/maptool/issues/613
[i612]: https://github.com/RPTools/maptool/issues/612
[i601]: https://github.com/RPTools/maptool/issues/601
[i591]: https://github.com/RPTools/maptool/issues/591
[i356]: https://github.com/RPTools/maptool/issues/356
[i328]: https://github.com/RPTools/maptool/issues/328
[i187]: https://github.com/RPTools/maptool/issues/187

Maptool 1.5.4
=====
More bug fixes and enhancements mostly thanks to the tireless efforts of new contributor, Guillaume "Merudo" Filteau.
___

Bug Fixes & Enhancements
-----
* [#617][i617] - Using the right-click Arrange -> Send to Back/Bring to Front functions could undo recent token changes. Fixed.
* [#603][i603] - Attempting to import a campaign file as campaign properties was throwing ClassCastException. Now displays a proper error message.
* [#594][i594] - Dragging tokens on hex grids did not display the blue path line and move count was slightly outside of the hex cell.  Fixed.
* [#589][i589] - Title option for dialog() command only worked on first use.  Subsequent uses did not update the dialog title. Fixed.
* [#587][i587] - Using title option in frame() would keep the frame from being reopened again once closed. Fixed.
* [#585][i585] - The _temporary_ property of frames was not being obeyed.  Fixed.
* [#584][i584] - New functions for getting Frame/Dialog properties.
  * getFrameProperties(frameName)
  * getDialogProperties(dialogName)
  * New property `value` added to frame() and dialog() roll options and can be read with above.  See MapTool wiki for more details.
* [#582][i582] - Modifying tokens was not updating VBL for tokens with attached VBL. This particular bug was not in released builds. Fixed.
* [#578][i578] - Added optional token id and map name parameters to sight macro functions.
  * Functions Affected: canSeeToken(), getSightType(), hasSight(), setHasSight(), setSightType()
* [#574][i574] - Bug fixes and enhancments for several light macro functions
  * hasLightSource(), setLight(), and getLights() no longer causes an NPE if the light type entered doesn't exist
  * FoW now automatically updates after using clearLights() or setLights()
  * functions now take optional token id and map name parameters
  * functions no longer send entire token object on change to clients
* [#573][i573] - MapTool credits in Help -> About window updated.
* [#569][i569] - Documented undocumented parameters of getPropertyDefault and setLayer.  Fixed NPE caused when getPropertyDefault is given only one parameter and there is no current token.
  * getPropertyDefault() - Accepts second parameter for Token (Property) Type and returns the default for that type.
  * setLayer() - A third parameter, forceShape, forces tokens to be of type Top Down if moved to Object layer and to either Circle or Square for the token layer.
* [#563][i563] - Additional parameter map name added to getState, setState, and setAllStates macro functions.
* [#560][i560] - Corrected error message if setZoom() was passed argument.
* [#558][i558] - Popup notes for Hidden/Object tokens did not include the GM name if only player notes field had content and if only the GM notes had content no name was shown.  Fixed.
* [#555][i555] - A number of macro functions that set properties on tokens have been updated to use new internal server commands to only pass the relevant data from server to client instead of the entire token object.
  * Functions:  bringToFront, moveToken, removeTokenFacing, resetProperty, resetSize, sendToBack, setAllStates, setGMNotes, setLayer, setLibProperty, setNotes, setNPC, setOwnedByAll, setOwner, setPC, setProperty, setPropertyType, setSize, setState, setTokenDrawOrder, setTokenFacing, setTokenHeight, setTokenShape, setTokenSnapToGrid, setTokenWidth
* [#552][i552] - A number of macro functions were only making changes to tokens locally and not pushing them out to the other clients.  Fixed.  See also #555.
  * Functions Affected:  setGMNotes, setNotes, setTokenShape, setTokenWidth, setTokenHeight, setTokenSnapToGrid, resetSize
* [#551][i551] - Opening and closing the Edit Token dialog on a Player Client was enabling Visible of FoW even though players did not have access to the setting.  Fixed.
* [#549][i549] - A number of macro functions were internally calling both Zone.putToken() as well as ServerCommand.putToken() which resulted in the token being sent out to clients twice. Fixed.
  * Functions Affected:  setTokenImage, setTokenPortrait, setTokenHandout and setTokenOpacity
* [#547][i547] - Map name added as optional parameter to getName() and setName() functions.
* [#545][i545] - Map name added as optional parameter to the following functions.
  * bringToFront, getGMNotes, getLayer, getMatchingProperties, getNotes, getOwners, getProperty, getPropertyNames, getPropertyNamesRaw, getPropertyType, getRawProperty, getSize, getTokenFacing, getTokenHeight, getTokenNativeHeight, getTokenNativeWidth, getTokenRotation, getTokenShape, getTokenWidth, hasProperty, isNPC, isOwnedByAll, isOwner, isPC, isPropertyEmpty, isSnapToGrid, removeTokenFacing, resetProperty, resetSize, sendToBack, setGMNotes, setLayer, setNPC, setNotes, setOwnedByAll, setOwner, setPC, setProperty, setPropertyType, setSize, setTokenFacing, setTokenHeight, setTokenShape, setTokenSnapToGrid, setTokenWidth
* [#541][i541] - Map name added as optional parameter to the following functions.
  * getTokenImage, getTokenPortrait, getTokenHandout, setTokenImage, setTokenPortrait, setTokenHandout, setTokenOpacity, getTokenOpacity
* [#540][i540] - Non-snap-to-grid tokens were dragging by top-left corner on square grids.  Fixed.
* [#539][i539] - New Macro function getTableEntry() returns raw table entry in JSON format.
* [#538][i538] - Macro function tableImage() was throwing an exception if the table entry did not have an image attached.  Now returns an empty string.
* [#534][i534] - Macro function getMatchingProperties() was not accepting 3rd parameter token ID.  Fixed.
* [#532][i532] - Fog of War macro functions did not return an error message if an invalid map name was passed in.  Fixed.
  * Functions Affected: exposePCOnlyArea, exposeFOW, exposeAllOwnedArea, restoreFoW
* [#531][i531] - New macro function getViewCenter() returns the location in either pixels or cell coordinates.
* [#525][i525] - Macro function exposeFogAtWaypoints() was returning the i18 string name instead of the actual error message.
* [#523][i523] - Various HTML entities that could be used to spoof valid roll results trapped from entry into chat.
* [#522][i522] - Incorrect tooltip for Preferences -> Application -> UPnP -> Discovery Timeout.  Fixed.
* [#519][i519] - With AI enabled, using token.denyMove = 1 in the special onTokenMove macro did not block movement allowing tokens to be moved to inaccessible areas.
* [#513][i513] - Saving a change to a macro from a different map was throwing an NPE and failing to update macro.  Fixed.
* [#510][i510] - Passing invalid parameters to the goto() function was incorrectly blaming moveToken().  Blame properly assigned now.
* [#509][i509] - Optional tokens and delimiter parameters added to exposeFOW() function allowing user to choose tokens that aren't selected.
* [#505][i505] - Bug was preventing the exposeFOW() function from accepting a map name argument.  Fixed.  Wiki page for restoreFOW() updated to note that it also accepts a map name argument.
* [#504][i504] - Result for getInfo("server") call did not include Auto Reveal on Movement and Individual FoW settings.  Fixed.
* [#470][i470] - Show Movement Distance setting was being ignored.  Fixed.
* [#434][i434] - Pinned frames displayed frame name instead of title. Fixed.  New _tabtitle_ property also added to frame() macro function.
* [#404][i404] - Manipulating a token while on a different map could lead to duplicate tokens. Fixed.
* [#357][i357] - Vision for Non-Individual Views was broken.  For a long time.  Fixed!
* [#226][i226] - Attempting to adjust map grid (Ctrl-Shift-A) after a Dialog was opened caused exception. Fixed.
* [#225][i225] - Attempting to load a file other than a campaign file was throwing a Class Cast exception. Open Campaign dialog now defaults to only showing campaign files and CCEs caught with appropriate error message shown.
* [#213][i213] - The following operations do not cause auto-exposure of FoW for tokens.  Documenting current behavior as of 1.5.4.  See ticket for more details.
  * Edit Token Dialog
    * Changing Sight Type
    * Changing Has Sight
  * Right-Click Menu
    * Setting Light On
  * Macro Functions
    * Setting Light On
    * Changing Sight Type
    * Changing Has Sight
* [#152][i152] - Undo (Ctrl-Z) was frequently buggy producing unpredictable results. Fixed(in 1.5.0).
  * Undo buffers are specific to each client. Undo/Redos only affect the client taking action.
* [#150][i150] - Using sendToBack() and bringToFront() macros could break states or bars on tokens.  Fixed(in 1.5.0).
* [#132][i132] - Calling getname() (lower case 'n') was falling through to setName and returned incorrect error message. Fixed with other changes to token macro functions.
* [#116][i116] - strPropFromVars function was creating an extra, empty entry.  Fixed.  Second parameter made optional and defaults to "UNSUFFIXED".

[i617]: https://github.com/RPTools/maptool/issues/603
[i603]: https://github.com/RPTools/maptool/issues/603
[i594]: https://github.com/RPTools/maptool/issues/594
[i589]: https://github.com/RPTools/maptool/issues/589
[i587]: https://github.com/RPTools/maptool/issues/587
[i585]: https://github.com/RPTools/maptool/issues/585
[i584]: https://github.com/RPTools/maptool/issues/584
[i582]: https://github.com/RPTools/maptool/issues/582
[i578]: https://github.com/RPTools/maptool/issues/578
[i574]: https://github.com/RPTools/maptool/issues/574
[i573]: https://github.com/RPTools/maptool/issues/573
[i569]: https://github.com/RPTools/maptool/issues/569
[i563]: https://github.com/RPTools/maptool/issues/563
[i560]: https://github.com/RPTools/maptool/issues/560
[i558]: https://github.com/RPTools/maptool/issues/558
[i555]: https://github.com/RPTools/maptool/issues/555
[i552]: https://github.com/RPTools/maptool/issues/552
[i551]: https://github.com/RPTools/maptool/issues/551
[i549]: https://github.com/RPTools/maptool/issues/549
[i547]: https://github.com/RPTools/maptool/issues/547
[i545]: https://github.com/RPTools/maptool/issues/545
[i541]: https://github.com/RPTools/maptool/issues/541
[i540]: https://github.com/RPTools/maptool/issues/540
[i539]: https://github.com/RPTools/maptool/issues/539
[i538]: https://github.com/RPTools/maptool/issues/538
[i534]: https://github.com/RPTools/maptool/issues/534
[i532]: https://github.com/RPTools/maptool/issues/532
[i531]: https://github.com/RPTools/maptool/issues/531
[i525]: https://github.com/RPTools/maptool/issues/525
[i523]: https://github.com/RPTools/maptool/issues/523
[i522]: https://github.com/RPTools/maptool/issues/522
[i519]: https://github.com/RPTools/maptool/issues/519
[i513]: https://github.com/RPTools/maptool/issues/513
[i510]: https://github.com/RPTools/maptool/issues/510
[i509]: https://github.com/RPTools/maptool/issues/509
[i505]: https://github.com/RPTools/maptool/issues/505
[i504]: https://github.com/RPTools/maptool/issues/504
[i470]: https://github.com/RPTools/maptool/issues/470
[i434]: https://github.com/RPTools/maptool/issues/434
[i404]: https://github.com/RPTools/maptool/issues/404
[i357]: https://github.com/RPTools/maptool/issues/357
[i226]: https://github.com/RPTools/maptool/issues/226
[i225]: https://github.com/RPTools/maptool/issues/225
[i213]: https://github.com/RPTools/maptool/issues/213
[i152]: https://github.com/RPTools/maptool/issues/152
[i150]: https://github.com/RPTools/maptool/issues/150
[i132]: https://github.com/RPTools/maptool/issues/132
[i116]: https://github.com/RPTools/maptool/issues/116

Maptool 1.5.3
=====
More bug fixes and enhancements
___

Bug Fixes
-----
* [#487][i487] Restored previous token dragging behavior as default.
  * [#315][i315] Tokens move less erratically.
  * [#300][i300] Tokens have less drift.
* [#485][i485] VBL changes made with Clear VBL on Edit Token Dialog and via setTokenVBL() were not updating local client view nor propagating to other clients.  Fixed.
* [#481][i481] Token VBL no longer rotated -1 degrees.
* [#476][i476] Added catch for missing/null directories cause an NPE in ImageFileImagePanelModel.getImage()
* [#473][i473] Shutting down server should no longer throw an NPE when closing port with UPnP
* [#467][i467] Updated layout of Start Server dialog so that server setting labels can go across the full dialog instead of being cutoff.
* [#461][i461] GM Notes and GM Name for tokens no longer visible to players.
* [#450][i450] Corrected footprint problems with native-size figure tokens which affected halos and vision.
* [#442][i442] Pathing for 1-1-1 movement corrected to no longer look bad.
* [#376][i376] RPTools Gallery Index button removed from Campaign Properties.  Gallery no longer exists.
* [#288][i288] Added missing Grid shape to Light help and missing Hex and Cone to Sight help of Campaign Properties dialog.
* [#261][i261] Selecting white in the color picker for the background of new maps threw exception.
* [#191][i191] Auto-Resize dialog couldn't handle larger font sizes. Fixed.
* [#166][i166] Tokens dropped on hex grids now pick up pick up map grid size instead of default.

Enhancements
-----
* [#487][i487] New Preferences settings for choosing between snapped and non-snapped token dragging as well as hide/show mouse pointer during dragging.  Further tweaks to token dragging to improve tracking of token image to mouse pointer position.
  * [#479][i479] Snap restored as default.
* [#298][i298] New Preferences selection for Macro editor themes. Themes can be found in C:\Users\\<username\>\\.maptool-rptools\themes\syntax

[i485]: https://github.com/RPTools/maptool/issues/485
[i442]: https://github.com/RPTools/maptool/issues/442
[i487]: https://github.com/RPTools/maptool/issues/487
[i479]: https://github.com/RPTools/maptool/issues/479
[i376]: https://github.com/RPTools/maptool/issues/376
[i298]: https://github.com/RPTools/maptool/issues/298
[i487]: https://github.com/RPTools/maptool/issues/487
[i481]: https://github.com/RPTools/maptool/issues/481
[i476]: https://github.com/RPTools/maptool/issues/476
[i473]: https://github.com/RPTools/maptool/issues/473
[i467]: https://github.com/RPTools/maptool/issues/467
[i461]: https://github.com/RPTools/maptool/issues/461
[i450]: https://github.com/RPTools/maptool/issues/450
[i315]: https://github.com/RPTools/maptool/issues/315
[i300]: https://github.com/RPTools/maptool/issues/300
[i288]: https://github.com/RPTools/maptool/issues/288
[i261]: https://github.com/RPTools/maptool/issues/261
[i191]: https://github.com/RPTools/maptool/issues/191
[i166]: https://github.com/RPTools/maptool/issues/166


Maptool 1.5.2
=====
More bug fixes and enhancements
___

Bug Fixes
-----
* [#362][i362] - README updated to reflect current version.
* [#118][i118] - Fixed console problem with spaces being in the Java path.
* [#441][i441] - Draw Explorer no longer allows attempting to merge a drawing and a template.
* [#395][i395] - Fix for Edit menu issues if language not set to English.
* [#292][i292] - Out-of-date/bad URLs in Help menu updated/corrected.
* [#339][i339] - Fixed Templates not previewing at correct location.
* [#377][i377] - Returned values from getViewArea() corrected to be compatible with setViewArea().
* [#392][i392] - Default Map Preferences updated to allow decimal Units per Cell values.
* [#400][i400] - Selecting a filetype other than an RPTools map file when using Import Map no longer throws exception.
* [#386][i386] - Output of *java -version* no longer included in Help -> Debug output.
* [#398][i398] - Newlines can now be inserted into the middle of command lines in the chat window.
* [#338][i338] - Templates added for Bugs, Features and Technical Questions to MapTool GitHub issues.

Enhancements
-----
* [#335][i335] - New macro functions for controlling logging. [See log.* functions on Wiki](http://www.lmwcs.com/rptools/wiki/Category:Log_Function)
* [#429][i429] - New macro functions for getting/setting terrain modifiers: [getTerrainModifier][igtm](), [setTerrainModifier][istm]()
* [#345][i345] - New dicelib version with new dice expressions and new macro functions for accessing individual die roll values:
  * [getRolled][igrd]()
  * [getNewRolls][ignr]()
  * [clearRolls][icrl]()
* [#406][i406] - New [dice expression](http://www.lmwcs.com/rptools/wiki/Dice_Expressions) **XdYdhZ** (drop highest) and 7 others.
* [#355][i355] - Macro Editor details tab reorganized to give some fields more room. Macro button tooltip entry field made into a larger text area with HTML highlighting.  Checkbox to enable/disable hotkey display on button.  UDFs now show in auto-complete of macro editor with their tooltip as help text.
* [#426][i426] - New Line & Radius templates that start at cells. New icons for all template types.
* [#424][i424] - Auto-completion in macro editor now works even if complete function name has already been entered.
* [#349][i349] - New macro functions for base64 encoding/decoding: [base64.encode][ibe64](), [base64.decode][ibd64]().
* [#416][i416] - New macro function [movedOverDrawing](http://www.lmwcs.com/rptools/wiki/movedOverDrawing)().
* [#407][i407] - New macro function [getDrawingInfo](http://www.lmwcs.com/rptools/wiki/getDrawingInfo)().
* [#384][i384] - Warning message removed when using Import Map option.
* [#365][i365] - Editing token properties now supports word wrap and syntax highlighting.
* [#106][i106] - Reset Size added to right-click menu for tokens/stamps.
* [#299][i299] - Mouse pointer now visible when dragging tokens.
* [#389][i389] - File -> Export -> Campaign File As... now supports converting back to non-decimal map units-per-cell values.
* [#332][i332] - Added support for multiple personal lights and setting color for personal lights.

[igrd]: http://www.lmwcs.com/rptools/wiki/getRolled
[ignr]: http://www.lmwcs.com/rptools/wiki/getNewRolls
[icrl]: http://www.lmwcs.com/rptools/wiki/clearRolls
[ibd64]: http://www.lmwcs.com/rptools/wiki/base64.decode
[ibe64]: http://www.lmwcs.com/rptools/wiki/base64.encode
[igtm]: http://www.lmwcs.com/rptools/wiki/getTerrainModifier
[istm]: http://www.lmwcs.com/rptools/wiki/setTerrainModifier
[i106]: https://github.com/RPTools/maptool/issues/106
[i118]: https://github.com/RPTools/maptool/issues/118
[i292]: https://github.com/RPTools/maptool/issues/292
[i299]: https://github.com/RPTools/maptool/issues/299
[i335]: https://github.com/RPTools/maptool/issues/335
[i338]: https://github.com/RPTools/maptool/issues/338
[i339]: https://github.com/RPTools/maptool/issues/339
[i345]: https://github.com/RPTools/maptool/issues/345
[i349]: https://github.com/RPTools/maptool/issues/349
[i355]: https://github.com/RPTools/maptool/issues/355
[i362]: https://github.com/RPTools/maptool/issues/362
[i377]: https://github.com/RPTools/maptool/issues/377
[i384]: https://github.com/RPTools/maptool/issues/384
[i386]: https://github.com/RPTools/maptool/issues/386
[i389]: https://github.com/RPTools/maptool/issues/389
[i395]: https://github.com/RPTools/maptool/issues/395
[i398]: https://github.com/RPTools/maptool/issues/398
[i400]: https://github.com/RPTools/maptool/issues/400
[i406]: https://github.com/RPTools/maptool/issues/406
[i407]: https://github.com/RPTools/maptool/issues/407
[i416]: https://github.com/RPTools/maptool/issues/416
[i424]: https://github.com/RPTools/maptool/issues/424
[i426]: https://github.com/RPTools/maptool/issues/426
[i429]: https://github.com/RPTools/maptool/issues/429
[i441]: https://github.com/RPTools/maptool/issues/441

MapTool 1.5.1
=====
A minor update to 1.5.0 consisting of bug fixes and small enhancements.

___

Bug Fixes
-----
* [#107][i107] - getTokenNativeHeight/Width Script Function
* [#189][i189] - transferVBL function is misnamed
* [#278][i278] - Fix existing unit tests and enable unit tests in build
* [#326][i326] - defineFunction causes error with macro edit window open
* [#324][i324] - fix output of setViewArea. This might break existing macro using workaround to interpret the broken result before the fix.


Enhancements
-----
* [#50][i50] - Decimal digits in map properties (distance per cell)
* [#255][i255] - Enable Delete Button on Draw Explore
* [#289][i255] - token opacity slider needs preview
* [#332][i332] - Allow to set color for personal lights in campaign Sight types



[i50]: https://github.com/RPTools/maptool/issues/50
[i107]: https://github.com/RPTools/maptool/issues/107
[i189]: https://github.com/RPTools/maptool/issues/189
[i255]: https://github.com/RPTools/maptool/issues/255
[i278]: https://github.com/RPTools/maptool/issues/278
[i255]: https://github.com/RPTools/maptool/issues/255
[i326]: https://github.com/RPTools/maptool/issues/326
[i324]: https://github.com/RPTools/maptool/issues/324
[i332]: https://github.com/RPTools/maptool/issues/332


MapTool 1.5.0
=====
A major update pulling in almost a years worth of enhancements and bug fixes from the Nerps fork back to the main MapTool repository. This will bring us back to a single build for users once again while we continue working on MapTool 2.0.

___

Bug Fixes
-----
* [#113][i113] - *Macro function getMoveCount() does not return terrain modifier costs*. See issue for full details but basically a new parameter is added so getMoveCount(0, 1) will return movement costs taking into account any Terrain Modifiers.
* [#108][i108] - *Fix setTokenOpacity to update token for players.*. Token is now updated with opacity effect on the server after setTokenOpacity macro calls.
* [#92][i92] - *MapTool should default to UTF-8 encoding*. Windows likes to default to it's one file encoding which can cause issues in macros and certain encoded characters. MapTool will now always launch using UTF-8 file encoding! I've also added a 'Encoding Information' section in Gather Debug Info under the help menu to verify what encoding you are using.
* [#80][i80] - *Comparison method violates its general contract in FogUtil.calculateVisibility(FogUtil.java:81)*. This should be fixed now.
* [#81][i81] - *Cell Highlight distance text not sizing for grid sizes*. This is now fixed
* [#76][i76] - Will no longer see errors when attempting to open initiative window on Linux or MacOS.
* [#68][i68] - MapTool's i18n language override enabled via Edit -> Preferences -> Startup tab. This will override your default language set by your OS.
* [#41][i41] - Allow player owned tokens without "sight" to move within currently exposed FoW, e.g. areas other PC token can currently see. Hopefully this is finally squashed properly...
* [#44][i44] - If running the JAR version with your own JRE installed, Edit -> Preferences will no longer throw and error and blow up! Instead the Startup tab will be grayed out (as those values will not be used and you must make your own startup script to set memory settings when using the JAR format)
* [#27][i27] - Code cleanup, .pdf & .por files will no longer throw errors to the log file.
* [#65][i65] - Gradle will once again build and deploy vs throw an error getting the branch name.
* [#179][i179] - Pulled in @Jaggeroth change from main RPTool's Repo; ZOrder sort violation problem by restoring the original comparator and only using the new figure comparator when sorting figure only.
* [#54][i54]  - Missing Preferences menu option on OS X
* [#59][i59]  - App does not check for new versions in 1.4.4.1
* [#5][i5]  - Adding new state causes java.lang.ArrayIndexOutOfBoundsException no more!
* [#6][i6]  - Fixed various Typos
* [#15][i15] - Lighting wasn't immediately forced to connected clients and should be fixed now.
* [#18][i18] - Exporting Campaigns back to 1.4.0.1 was failing due to new objects added. You can now export any campaign back to 1.4.0.1 but as always, this is permanent in that it will strip out new objects as in TokenVBl, new lighting options, etc but macros will not be touched and may fail if they contain new macro functions not available in 1.4.0.1.
* [#19][i19] - Default save location for tokens are now remembered!
* [#20][i20] - Added missing documentation for lights/vision, e.g. 'scale'
* [#21][i21] - Lights are not updating properly based on ownership looked like it was tied to other 'light' bugs and should now be fixed.
* [#23][i23] - Fixed sendToBack & bringToFront macros broke states and bar changes in the macro. This was an OLD one going back to 1.3b63! You can now safely use these functions in your macro now!
* [#30][i30] - Players see NPC movement when there are no lights no more! This was another old bug going back to 1.3b-something and only showed itself if you had NO lights (including personal lights, aka darkvision).
* [#232][i232] - Mouse pointer incorrectly changing to Hand pointer was an oversight which has been corrected, hands down the best bug fix!
* [#210][i210] - *Non-snap-to-grid Tokens have bad last path info* Tokens now walk the straight and narrow again, no more drunken paths shown.

Enhancements
-----
* [#125][i125] - When entering invalid JVM values in Startup Preferences, a dialog will not display explaining why it was not saved. Another dialog will show if any JVM values were changed warning the user that invalid options could prevent MapTool from starting and to confirm changes.
* [#77][i77] - Tweaked the A* algorithm for a more natural and straighter move though open spaces for both square and hex grids. Also tweaked the A* algorithm to find the shortest path more consistently.
* [#49][i49] - Pathfinding! When activated (via new 'AI' toggle button), tokens will find the shortest path to it's destination as you drag them on the token or hidden layers, taking VBL into account. Yes, this means your tokens will no longer walk thru VBL! If no path can be found (or found within several seconds) no path will be shown, however you can still move your token to that location. This can happen because the area is blocked off or you are working with a very complicated or large map. With MapTool allowing unbounded/nearly infinite map space, I had to include a timeout to prevent an infinite search.
A new token config value is also now available called Terrain Modifier. This multiplies the cost of moving into that tokens cell and taken into account with Pathfinding is used. Some examples are:
Setting the multiplier to 2 will cost 10 feet of movement vs 5 feet acting like difficult terrain.
Setting the multiplier to a sufficiently high number, like 99999, will effectively block the movement and make the token go around the obstacle. Useful for things like an arrow slit, window, lava, etc.
Any token can have this modifier, NPC tokens to make PC tokens go around them vs through them, or stamp tokens on the object layer like oil or water. You could also place tokens on the hidden layer over rocky terrain to denote difficult terrain.
Please note, at this time no macros are available to access these new feature, nor server options to force players to use this mode of moving tokens. Due to the complexity of this feature, I felt it better to get this out to you, the user, to play with and use (or not use) and provide feedback. Therefore the Terrain Modifier *could* change in the future or expand in functionality. There could be performance related hits as well, although I did my best to mitigate this, as well as you can turn this feature off and revert to basic movement.
Also note, currently fractional modifiers are not supported at this time nor straight addition modifiers. I plan to expand this functionality over time.
* [#45][i45] - Lighting has been improved to greatly reduce lag when multiple light sources are on a map. It also helps alleviate the slowness you encounter as you reveal more and more FoW.
* [#63][i63] - Spacebar functionality has been restored to it's original behavior, including ctrl+spacebar & shift+spacebar.
A new shift+ctrl+spacebar command along with a new pointer image is now available. When this keystroke combo is pressed, and you are a GM, the pointer will center & zoom all connected clients to that point. When it is released, all clients will return to their previous view point & zoom.
* [#67][i67] - A new vision/light type of GRID has also been added, which is a circle of the specified size but only lights up the affected grid cells within range, much like using the template tool. This is useful in systems like Pathfinder if you want to see exactly which grid cells (squares) are affected by a Light/Aura or can be seen. Aura for Channel Energy for instance, or seeing which grid cells are in dim light for concealment.
* [#71][i71] - The default data directory (USER_HOME/.maptool-{Vendor}, so for Nerps fork, .maptool-Nerps) can now be overridden in Edit -> Preferences -> Startup tab.
* [#43][i43] - Spotless and .appveyor.yml updated. This is purely a build process enhancement to make deployment of the releases better.
* [#66][i66] - Update packaged JRE to Java 10 and verify MapTool runs under Java 10. *Note: Java 9 was a short term release hence the update to Java 10.
* [#8][i8]  - New packaging mechanism! OS specific "installs" are now being generated using Oracles native javapackager tool. It packages the JAR, native executable, and JRE in the following packages: .exe (Windows), .dmg & .pkg (MacOS), & .deb (Linux) as well as unified JAR that can be run manually.
* [#26][i26] - MapTool now checks and alerts user when a new version is available as a GitHub release and allow you to download it! You can "skip" a version to stop alerting until the next release comes out or cancel all auto update checks. It will download the release based on your OS (.exe, .pkg, or .deb)
* [#9][i9]  - New macro function added to change token ownership to 'Owned by All', setOwnedByAll(boolean [,tokenID]) returns boolean
* [#22][i22] - Darkvision changed to Darkvision: circle r60 (removed distance=62.5)
* [#25][i25] - Cone lights now accept 'offset=x' as an option just like vision
* [#33][i33] - Java stack traces are sent automatically to Sentry.io for aggregation and notification. No private info is gathered or sent. This lets me know if an unreported bug shows up and how critical may be so I can get it fixed quicker and with minimal info needed from users.
* New RESTful functions getRequest & postRequest to send GET & POST requests to a URI. *Note: You must first turn access on in Preferences for these macro functions to work.
* New function exportData exportData(FilePath file, String data, boolean appendToFile) which saves string data to external file.
* New function getEnvironmentVariable(String name), Returns the value stored in the Environment Variable.
* New menu option added to the "Connections" window. Right clicking a player will offer a "Whisper" command that prepopulates the chat window with a whisper macro.
* [#237][i237] - Added support to use shift-enter to insert newlines into the command entry box (also known as the chat entry box)
* [#239][i239] - MapToolScriptTokenMaker now handles function names with . notation and dynamically pulls in all functions names. TokenMakerMaker no longer needs to be ran upon changes to MTScript.
* [#240][i240] - Macro Editor now has Auto-Completion for macro functions! A brief description and summary can be displayed (these will be added as time permits)
* [#332][i332] - Allow to set color for personal lights in campaign Sight types
* [#324][i324] - fix output of getViewArea. This might break existing macro using workaround to interpret the broken result before the fix.
* [#326][i326] - defineFunction with open edit window
* [#365][i365] - Support wrap and syntax in property editor

[i332]: https://github.com/RPTools/maptool/issues/332
[i324]: https://github.com/RPTools/maptool/issues/324
[i326]: https://github.com/RPTools/maptool/issues/326
[i365]: https://github.com/RPTools/maptool/issues/365
[i210]: https://github.com/RPTools/maptool/issues/210
[i113]: https://github.com/JamzTheMan/MapTool/issues/113
[i108]: https://github.com/JamzTheMan/MapTool/issues/108
[i92]: https://github.com/JamzTheMan/MapTool/issues/92
[i80]: https://github.com/JamzTheMan/MapTool/issues/80
[i81]: https://github.com/JamzTheMan/MapTool/issues/81
[i41]: https://github.com/JamzTheMan/MapTool/issues/41
[i77]: https://github.com/JamzTheMan/MapTool/issues/77
[i76]: https://github.com/JamzTheMan/MapTool/issues/76
[i44]: https://github.com/JamzTheMan/MapTool/issues/44
[i49]: https://github.com/JamzTheMan/MapTool/issues/49
[i45]: https://github.com/JamzTheMan/MapTool/issues/45
[i44]: https://github.com/JamzTheMan/MapTool/issues/44
[i43]: https://github.com/JamzTheMan/MapTool/issues/43
[i41]: https://github.com/JamzTheMan/MapTool/issues/41
[i27]: https://github.com/JamzTheMan/MapTool/issues/27
[i63]: https://github.com/JamzTheMan/MapTool/issues/63
[i65]: https://github.com/JamzTheMan/MapTool/issues/65
[i66]: https://github.com/JamzTheMan/MapTool/issues/66
[i67]: https://github.com/JamzTheMan/MapTool/issues/67
[i68]: https://github.com/JamzTheMan/MapTool/issues/68
[i71]: https://github.com/JamzTheMan/MapTool/issues/71
[i179]: https://github.com/RPTools/maptool/pull/179
[i8]: https://github.com/JamzTheMan/MapTool/issues/8
[i26]: https://github.com/JamzTheMan/MapTool/issues/26
[i9]: https://github.com/JamzTheMan/MapTool/issues/9
[i22]: https://github.com/JamzTheMan/MapTool/issues/22
[i25]: https://github.com/JamzTheMan/MapTool/issues/25
[i5]: https://github.com/JamzTheMan/MapTool/issues/5
[i6]: https://github.com/JamzTheMan/MapTool/issues/6
[i15]: https://github.com/JamzTheMan/MapTool/issues/15
[i18]: https://github.com/JamzTheMan/MapTool/issues/18
[i19]: https://github.com/JamzTheMan/MapTool/issues/19
[i20]: https://github.com/JamzTheMan/MapTool/issues/20
[i21]: https://github.com/JamzTheMan/MapTool/issues/21
[i23]: https://github.com/JamzTheMan/MapTool/issues/23
[i30]: https://github.com/JamzTheMan/MapTool/issues/30
[i33]: https://github.com/JamzTheMan/MapTool/issues/33
[i54]: https://github.com/JamzTheMan/MapTool/issues/54
[i59]: https://github.com/JamzTheMan/MapTool/issues/59
[i125]: https://github.com/JamzTheMan/MapTool/issues/125
[i237]: https://github.com/RPTools/maptool/issues/237
[i239]: https://github.com/RPTools/maptool/issues/239
[i240]: https://github.com/RPTools/maptool/issues/240
[i232]: https://github.com/RPTools/maptool/issues/232
[i392]: https://github.com/RPTools/maptool/issues/392
