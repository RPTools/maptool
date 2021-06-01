# MapTool 1.9.0 Beta 3
The MapTool community continues to push MapTool forward with code patches, localization and providing support to each other on the various social media platforms.  A huge thanks goes out to everyone who takes part in making MapTool better.

MapTool currently has support for 13 languages in various stages of completion: Chinese, Danish, Dutch, French, German Italian, Japanese, Polish, Portuguese, Russian, Spanish, Swedish and Ukranian.  

## Highlights 
- Development moved to AdoptOpen Java 16.
- Moved from Nashorn to GraalVM JavaScript engine.
- More image types supported along with beginnings of support for non-image assets.
- Quality of Life improvements: pan map with arrow keys, select drawings on map for deletion, hotkeys for image flip.
- Additional UI elements available in full-screen mode. 
- More bug fixes.
- More localization for the UI.

## Enhancements
- [#2657][i2657] New `relativeto` updates option for `copyToken()` accepts `map`, `current` token, &amp; `source` token.
- [#2651][i2651] Default grid size increased to 100.
- [#2608][i2608] Updated to DiceLib 1.8.0.  New macro function `rollSubWithUpper()` and two new dice expressions `XdYaZlW` & `XdYsZuW`.
- [#2595][i2595] To facilitate localization and consistency, several macro commands changed to accept/return enum values instead of the English display names. Functions affected: `setTokenVBL()`, `getTokenShape()`, `setTokenShape()` and `getInfo("map")`.
- [#2583][i2583] Large number of additional UI strings have been localized.
- [#2550][i2550] Internally sent macro commands no longer added to Chat command history.
- [#2546][i2546] MapTool no longer uses OS User Name by default in title bar. New preference setting added for Default Username which defaults to Anonymous User.
- [#2524][i2524] Activating experimental webapp server shows warning dialog and sends warning to chat.
- [#2519][i2519] Moved to GraalVM-JS engine from Nashorn. GraalVM provides an ECMAScript-compliant (ES11) runtime to execute JavaScript.
- [#2494][i2494] Support added for WebP, SVG, ICO, TGA images.
- [#2466][i2466] Drawings can now be selected with the mouse and deleted via delete key.
- [#2452][i2452] Additional UI elements added to fullscreen view with new View menu toggle.
- [#2366][i2366] Improved tooltips and error messages from Start Server dialog. 
- [#2360][i2360] Map view can now be panned with Ctrl + arrow keys.
- [#2349][i2349] Shortcut keys added to flip token images. F for horizontal and Shift-F for vertical.
- [#2234][i2234] New macro functions for drawing MBL: `drawMBL()`, `eraseMBL()`, `getMBL()`.
- [#1496][i1496] Common Macros group now shows on Selected panel even when no macros are in common.
- [#1420][i1420] HTML links in displayed token notes will now open default browser.
- [#1234][i1234] New macro function for retrieving Bar & State images: `getBarImage()` and `getStateImage()`
- [#369][i369] Dragging macro buttons now supports both move and copy depending on source/destination and active modifier keys. 

## Bug Fixes
- [#2684][i2684] Retrieving external IP address could cause MT to seemingly hang up. Fixed.
- [#2675][i2675] Message returned when a macro-generated frame used a reserved name wasn't helpful. Fixed.
- [#2661][i2661] Changes made to drawings via Draw Explorer were not immediately reflected on map. Fixed.
- [#2656][i2656] Clients forced off map if GM changed map properties. Fixed.
- [#2652][i2652] `getMapVisible()` returned strings instead of numbers. Fixed.
- [#2637][i2637] Tree view in Draw Explorer was showing single drawings as groups and groups as single drawings. Fixed. 
- [#2630][i2630] Map grid type `None` was missing from `New Map Grid Type` in Preferences. Fixed. 
- [#2601][i2601] Replaced Java native filetype detection with Apache Tika for better coverage of asset types.
- [#2596][i2596] Assert output a stack trace when called from a macro executed by a macro link. Fixed.
- [#2560][i2560] Layer names lacked i18n support. Fixed.
- [#2558][i2558] Exception thrown with keyboard input on undocked frames. Fixed.
- [#2555][i2555] Names for Dockable frames didn't use localized names. Fixed.
- [#2551][i2551] Various UI and macro actions could clear in progress text in the Chat panel. Fixed.
- [#2548][i2548] Light icons broken on imported Dungeondraft UVTT maps. Fixed.
- [#2527][i2527] Unsupported asset types are now filtered in drag-and-drop to map.
- [#2493][i2493] Images using the size parameter weren't scaling correctly. Fixed.
- [#2485][i2485] Macro-generated frames were not restored to previous positions. Fixed.
- [#2482][i2482] `playStream()` failing on MacOS and Linux. Fixed with update to Java 16.
- [#2379][i2379] Pathfinding could fail with certain configurations of VBL/MBL structures. Fixed.
- [#2334][i2334] Using a bad size parameter when displaying assets in HTML could hang client. Fixed.
- [#2325][i2325] Error message returned when `/reply` is used without a prior `/whisper` has been localized.
- [#2288][i2288] Non-alphanumeric chars in the key of a String Property could break `getStrProp()`. Fixed.
- [#2249][i2249] Javascript in Overlays would keep running after overlay closed. Fixed.
- [#2214][i2214] Moving to Java 16 fixed startup issues with Japanese locale.
- [#706][i706] A State & Bar with the same name can activate/deactivate together. Duplicate names no longer allowed.
- [#685][i685] Empty asset URL in HTML `<input>` would lock up MapTool. Fixed.

## Other
- [#2654][i2654] Abeille form designer added to [How to Setup UI Tools page](https://github.com/RPTools/maptool/blob/develop/doc/How_To_Setup_UI_Tools.md)
- [#2601][i2601] Added Apache Tika for filetype recognition. 
- [#2538][i2538] Builds moved to AdoptOpen Java 16. 
- [#2519][i2519] Migrated to GraalVM-JS engine from Nashorn.

[i2684]: https://github.com/RPTools/maptool/issues/2684
[i2675]: https://github.com/RPTools/maptool/issues/2675
[i2661]: https://github.com/RPTools/maptool/issues/2661
[i2657]: https://github.com/RPTools/maptool/issues/2657
[i2656]: https://github.com/RPTools/maptool/issues/2656
[i2652]: https://github.com/RPTools/maptool/issues/2652
[i2651]: https://github.com/RPTools/maptool/issues/2651
[i2647]: https://github.com/RPTools/maptool/issues/2647
[i2637]: https://github.com/RPTools/maptool/issues/2637
[i2630]: https://github.com/RPTools/maptool/issues/2630
[i2608]: https://github.com/RPTools/maptool/issues/2608
[i2601]: https://github.com/RPTools/maptool/issues/2601
[i2596]: https://github.com/RPTools/maptool/issues/2596
[i2595]: https://github.com/RPTools/maptool/issues/2595
[i2583]: https://github.com/RPTools/maptool/issues/2583
[i2560]: https://github.com/RPTools/maptool/issues/2560
[i2558]: https://github.com/RPTools/maptool/issues/2558
[i2555]: https://github.com/RPTools/maptool/issues/2555
[i2551]: https://github.com/RPTools/maptool/issues/2551
[i2550]: https://github.com/RPTools/maptool/issues/2550
[i2548]: https://github.com/RPTools/maptool/issues/2548
[i2546]: https://github.com/RPTools/maptool/issues/2546
[i2538]: https://github.com/RPTools/maptool/issues/2538
[i2527]: https://github.com/RPTools/maptool/issues/2527
[i2524]: https://github.com/RPTools/maptool/issues/2524
[i2519]: https://github.com/RPTools/maptool/issues/2519
[i2494]: https://github.com/RPTools/maptool/issues/2494
[i2493]: https://github.com/RPTools/maptool/issues/2493
[i2485]: https://github.com/RPTools/maptool/issues/2485
[i2482]: https://github.com/RPTools/maptool/issues/2482
[i2466]: https://github.com/RPTools/maptool/issues/2466
[i2452]: https://github.com/RPTools/maptool/issues/2452
[i2379]: https://github.com/RPTools/maptool/issues/2379
[i2366]: https://github.com/RPTools/maptool/issues/2366
[i2360]: https://github.com/RPTools/maptool/issues/2360
[i2349]: https://github.com/RPTools/maptool/issues/2349
[i2334]: https://github.com/RPTools/maptool/issues/2334
[i2325]: https://github.com/RPTools/maptool/issues/2325
[i2288]: https://github.com/RPTools/maptool/issues/2288
[i2249]: https://github.com/RPTools/maptool/issues/2249
[i2234]: https://github.com/RPTools/maptool/issues/2234
[i2214]: https://github.com/RPTools/maptool/issues/2214
[i1496]: https://github.com/RPTools/maptool/issues/1496
[i1420]: https://github.com/RPTools/maptool/issues/1420
[i1234]: https://github.com/RPTools/maptool/issues/1234
[i706]: https://github.com/RPTools/maptool/issues/706
[i685]: https://github.com/RPTools/maptool/issues/685
[i369]: https://github.com/RPTools/maptool/issues/369
[i]: https://github.com/RPTools/maptool/issues/
[i]: https://github.com/RPTools/maptool/issues/

[Change Log for 1.8.x](https://github.com/RPTools/maptool/blob/1.8.5/CHANGE_LOG.md)
