# MapTool 1.9.0 Beta 1
## Highlights
The MapTool community continues to push MapTool forward with code patches, localization and providing support to each other on the various social media platforms.  A huge thanks goes out to everyone who takes part in making MapTool better.

MapTool currently has support for 13 languages in various stages of completion: Chinese, Danish, Dutch, French, German Italian, Japanese, Polish, Portuguese, Russian, Spanish, Swedish and Ukranian.  
  
- Development moved to AdoptOpen Java 16.
- Moved from Nashorn to GraalVM JavaScript engine.
- More image types supported along with beginnings of support for non-image assets.
- Quality of Life improvements: pan map with arrow keys, select drawings on map for deletion, hotkeys for image flip.
- More bug fixes.
- More localization for the UI.

## Enhancements
- [#2550][i2550] Internally sent macro commands no longer added to Chat command history.
- [#2524][i2524] Activating experimental webapp server shows warning dialog and sends warning to chat.
- [#2519][i2519] Moved to GraalVM-JS engine from Nashorn. GraalVM provides an ECMAScript-compliant (ES11) runtime to execute JavaScript.
- [#2494][i2494] Support added for WebP, SVG, ICO, TGA images.
- [#2466][i2466] Drawings can now be selected with the mouse and deleted via delete key.
- [#2452][i2452] Additional UI elements added to fullscreen view
- [#2366][i2366] Improved tooltips and error messages from Start Server dialog. 
- [#2360][i2360] Map view can now be panned with Ctrl + arrow keys.
- [#2349][i2349] Shortcut keys added to flip token images. F for horizontal and Shift-F for vertical.
- [#2234][i2234] New macro functions for drawing MBL: `drawMBL()`, `eraseMBL()`, `getMBL()`.
- [#1496][i1496] Common Macros group now shows on Selected panel even when no macros are in common.
- [#1420][i1420] HTML links in displayed token notes will now open default browser.
- [#1234][i1234] New macro function for retrieving Bar & State images: `getBarImage()` and `getStateImage()`
- [#369][i369] Dragging macro buttons now supports both move and copy depending on source/destination and active modifier keys. 

## Bug Fixes
- [#2601][i2601] Replaced Java native filetype detection with Apache Tika for better coverage of asset types.
- [#2596][i2596] Assert output a stack trace when called from a macro executed by a macro link. Fixed.
- [#2560][i2560] Layer names lacked i18n support. Fixed.
- [#2558][i2558] Exception thrown with keyboard input on undocked frames. Fixed.
- [#2555][i2555] Names for Dockable frames didn't use localized names. Fixed.
- [#2551][i2551] Various UI and macro actions could clear in progress text in the Chat panel. Fixed.
- [#2548][i2548] Light icons broken on imported Dungeondraft UVTT maps. Fixed.
- [#2527][i2527] Unsupported assets types are now filtered in drag-and-drop to map.
- [#2493][i2493] Images using the size parameter weren't scaling correctly. Fixed.
- [#2334][i2334] Using a bad size parameter when displaying assets in HTML could hang client. Fixed.
- [#2288][i2288] Non-alphanumeric chars in the key of a String Property could break `getStrProp()`. Fixed.
- [#2249][i2249] Javascript in Overlays would keep running after overlay closed. Fixed.
- [#2214][i2214] Moving to Java 16 fixed startup issues with Japanese locale.
- [#706][i706] A State & Bar with the same name can activate/deactivate together. Duplicate names no longer allowed.
- [#685][i685] Empty asset URL in HTML `<input>` would lock up MapTool. Fixed.

## Other
- [#2601][i2601] Added Apache Tika for filetype recognition. 
- [#2538][i2538] Builds moved to AdoptOpen Java 16. 
- [#2519][i2519] Migrated to GraalVM-JS engine from Nashorn.

[i2601]: https://github.com/RPTools/maptool/issues/2601
[i2596]: https://github.com/RPTools/maptool/issues/2596
[i2560]: https://github.com/RPTools/maptool/issues/2560
[i2558]: https://github.com/RPTools/maptool/issues/2558
[i2555]: https://github.com/RPTools/maptool/issues/2555
[i2551]: https://github.com/RPTools/maptool/issues/2551
[i2550]: https://github.com/RPTools/maptool/issues/2550
[i2548]: https://github.com/RPTools/maptool/issues/2548
[i2538]: https://github.com/RPTools/maptool/issues/2538
[i2527]: https://github.com/RPTools/maptool/issues/2527
[i2524]: https://github.com/RPTools/maptool/issues/2524
[i2519]: https://github.com/RPTools/maptool/issues/2519
[i2494]: https://github.com/RPTools/maptool/issues/2494
[i2493]: https://github.com/RPTools/maptool/issues/2493
[i2466]: https://github.com/RPTools/maptool/issues/2466
[i2452]: https://github.com/RPTools/maptool/issues/2452
[i2366]: https://github.com/RPTools/maptool/issues/2366
[i2360]: https://github.com/RPTools/maptool/issues/2360
[i2349]: https://github.com/RPTools/maptool/issues/2349
[i2334]: https://github.com/RPTools/maptool/issues/2334
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
