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
