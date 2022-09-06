# MapTool 1.12.0
Feature release using OpenJDK 17

## Highlights
- The different VBL types and MBL can now be added to tokens via the Edit Token dialog and a number of macro functions for manipulating VBL/MBL have been added as well.
- More optimizations for lighting and vision.
- Improvements to new Add-On functionality.

## Enhancements & Features
- [#3567][i3567] New theme support using [FlatLaf](https://www.formdev.com/flatlaf/).
- [#3542][i3542] `setTokenImage()` was throwing Null Pointer Exception. Fixed.
- [#3540][i3540] UVTT import now supports file extensions used by other map creation tools (`.df2vtt` and `.uvtt`).
- [#3515][i3515] User language now availabe through `getInfo("client")` and `MapTool.clientInfo.getUserLanguage()` in JavaScript.
- [#3519][i3519] Improved lighting performance.
- [#3506][i3506] Updated `MapTool.log()` to varargs.
- [#3503][i3503] html5 javascript contexts now know their own identity and can get their user data.
- [#3464][i3464] New functions to determine if client is hosting or not. Info also available through `getInfo("client")` now.
- [#3459][i3459] Improvements to visibility calculation for large and complex geometry.
- [#3453][i3453] Add-On libraries can now be loaded/updated by dragging and dropping `.mtlib` files on map.
- [#3360][i3360] Handshake cipher change to AES/CBC/PKCS5Padding for improved security.
- [#3356][i3356] `player.getConnectedPlayers()` returns empty array when server started. Fixed.
- [#3346][i3346] Darkness (i.e. lights with negative lumens) now affect vision on maps set to Day or Off.
- [#3322][i3322] Numbers stored in Add-On library properties as strings. Fixed.
- [#3342][i3342] Improved performance on maps with many lights.
- [#3254][i3254] Moved from Hessian serialization to Protobuf for enhanced security and improved interoperability.
- [#3052][i3052] Tokens can now have Hill & Pit VBL. Settable via macro or VBL tab.
- [#2730][i2730] Tokens can now have MBL. Settable via macro or VBL tab.

## Bug Fixes
- [#3574][i3574] Properties section of Edit Token dialog throwing exceptions. Fixed.
- [#3571][i3571] Some sections of Token/Map Preferences not visible with new theme engine. Fixed.
- [#3561][i3561] Some VBL triggers error regarding orientation. Fixed.
- [#3558][i3558] `setBarVisible()` throwing NPE. Fixed.
- [#3534][i3534] Tokens with multi-image bars producing ArrayIndexOurOfboundsExceptions. Fixed.
- [#3530][i3530] Tokens with LastPath data saved were producing a ClassCastException when loaded. Fixed.
- [#3521][i3521] Vision broken on maps where VBL points had very small differences in location. Fixed. 
- [#3514][i3514] `setAllStates()` was throwing a `ArrayIndexOutOfBoundsException`. Fixed.
- [#3510][i3510] `createTable()` function throwing exception. Fixed.
- [#3500][i3500] JavaFX 17 causing crashes to desktop on Mac M1 hardware. Updated to JavaFX 18.
- [#3498][i3498] Add-On libraries with undefined fields in the `library.json` would fail to load. Fixed.
- [#3489][i3489] Using Arrange->Send to Back on Drawings would cause ClassCastException. Fixed.
- [#3488][i3488] Drawings on Player clients rendered over Darkness sources. Fixed.
- [#3481][i3481] Non-GMs should not be able to drag and drop add-on libs. Fixed.
- [#3479][i3479] Darkness sources without color specified were not rendering on Player view. Fixed.
- [#3477][i3477] MapTool won't start on Apple Silicon Macs. Fixed.
- [#3466][i3466] VBL precision problem could cause vision to break on some maps. Fixed.
- [#3462][i3462] `player.getName()` produces NPE if server not using player database. Fixed.
- [#3457][i3457] Clearing of JavaScript contexts and overlays were being cleared at the wrong time during campaign load.  Fixed.
- [#3440][i3440] Player clients not updated with VBL, MBL or Light tokens on imported UVTT maps if server is already running. Fixed.
- [#3457][i3457] Cleanup for Javascript contexts and overlays occuring in wrong place when loading a new campaign. Fixed.
- [#3436][i3436] Clients unable to connect to server when existing tokens did not have shape type set. Fixed.
- [#3434][i3434] Vision could be broken on clients for imported UVTT maps because of incomplete/missing initialization of tokens. Fixed. See also [#3440][i3440].
- [#3425][i3425] Importing macros with bad asset IDs for macro icon could hang up MapTool. Fixed.
- [#3387][i3387] `getAllPropertyNames()` now returns empty string/JSON arry if type invalid.
- [#3359][i3359] Player clients unable to connect to a server that had an Add-On loaded without loading the same Add-On locally. Fixed.
- [#3350][i3350] Passing insufficient parameters to some VBL functions returned a translation key instead of the error message. Fixed.
- [#3334][i3334] Fixed an issue with creating user defined functions in add-on library onInit function.
- [#3320][i3320] Starting a server with WebRTC enabled could randomly fail with an NPE. Fixed.
- [#3311][i3311] Missing error message for Player Already Connected error. Fixed.
- [#3298][i3298] WebRTC connections were not reconnecting after temporary disconnect. Fixed.
- [#3293][i3293] AI Pathfinding could send tokens through unexposed areas if a valid path exists potentially exposing those areas. Fixed.
- [#3274][i3274] Drag-n-drop of a HeroLab token from Library not working. Fixed.
- [#3256][i3256] Starting a server could delete existing templates on map. Fixed.
- [#3234][i3234] Edit Token dialog was hanging on to VBL generated for previously edited tokens and adding that to others. Fixed.
- [#3231][i3231] Javascript UDFs receive JSON arguments as empty arguments. Fixed.
- [#3218][i3218] MapTool hangs when launched from (uber)jar. Fixed.
- [#3146][i3146] Code was trying to load "default.xml" for the macro editor theme but file was "Default.xml" which would fail on case-sensitive filesystems. Fixed.
- [#3102][i3102] Javascript `Token.setProperty()` cannot take numerical value. Fixed.
- [#3101][i3101] Map grid color was not preserved over server start. Fixed.
- [#2764][i2764] Clients were unable to toggle tools in full-screen mode. Fixed.
- [#2763][i2763] Attempting to set VBL on token with VBL property retrieved from token produces errors. Fixed.
- [#2348][i2348] --pointermap CSS did not permit whitespace. Fixed (in 1.11.5).
- [#2094][i2094] Default white light is painted over darkness sources with color. Fixed.
- [#1931][i1931] With dark theme, macro group names are black on dark bg. New L&F library with new themes now used.
- [#1904][i1904] With dark theme, text in chat is black on black. New L&F library with new themes now used.
- [#1550][i1550] Overlapping colored lights not producing the correct result. Fixed.

## Other Changes
- [#3398][i3398] ImageIO libs updated to 3.8.2.
- [#3331][i3331] Update WebRTC lib to 0.5.0, update Java-WebSocket lib to 1.5.2.
- [#3286][i3286] Builds updated to OpenJDK 17.

[Change Log for 1.11.5](https://github.com/RPTools/maptool/blob/1.11.5/CHANGE_LOG.md)

[i]: https://github.com/RPTools/maptool/issues/
[i3574]: https://github.com/RPTools/maptool/issues/3574
[i3571]: https://github.com/RPTools/maptool/issues/3571
[i3567]: https://github.com/RPTools/maptool/issues/3567
[i3561]: https://github.com/RPTools/maptool/issues/3561
[i3558]: https://github.com/RPTools/maptool/issues/3558
[i3542]: https://github.com/RPTools/maptool/issues/3542
[i3540]: https://github.com/RPTools/maptool/issues/3540
[i3534]: https://github.com/RPTools/maptool/issues/3534
[i3530]: https://github.com/RPTools/maptool/issues/3530
[i3521]: https://github.com/RPTools/maptool/issues/3521
[i3519]: https://github.com/RPTools/maptool/issues/3519
[i3515]: https://github.com/RPTools/maptool/issues/3515
[i3514]: https://github.com/RPTools/maptool/issues/3514
[i3510]: https://github.com/RPTools/maptool/issues/3510
[i3506]: https://github.com/RPTools/maptool/issues/3506
[i3503]: https://github.com/RPTools/maptool/issues/3503
[i3500]: https://github.com/RPTools/maptool/issues/3500
[i3498]: https://github.com/RPTools/maptool/issues/3498
[i3489]: https://github.com/RPTools/maptool/issues/3489
[i3488]: https://github.com/RPTools/maptool/issues/3488
[i3481]: https://github.com/RPTools/maptool/issues/3481
[i3479]: https://github.com/RPTools/maptool/issues/3479
[i3477]: https://github.com/RPTools/maptool/issues/3477
[i3466]: https://github.com/RPTools/maptool/issues/3466
[i3464]: https://github.com/RPTools/maptool/issues/3464
[i3462]: https://github.com/RPTools/maptool/issues/3462
[i3459]: https://github.com/RPTools/maptool/issues/3459
[i3457]: https://github.com/RPTools/maptool/issues/3457
[i3453]: https://github.com/RPTools/maptool/issues/3453
[i3440]: https://github.com/RPTools/maptool/issues/3440
[i3436]: https://github.com/RPTools/maptool/issues/3436
[i3434]: https://github.com/RPTools/maptool/issues/3434
[i3425]: https://github.com/RPTools/maptool/issues/3425
[i3398]: https://github.com/RPTools/maptool/issues/3398
[i3387]: https://github.com/RPTools/maptool/issues/3387
[i3360]: https://github.com/RPTools/maptool/issues/3360
[i3359]: https://github.com/RPTools/maptool/issues/3359
[i3356]: https://github.com/RPTools/maptool/issues/3356
[i3350]: https://github.com/RPTools/maptool/issues/3350
[i3346]: https://github.com/RPTools/maptool/issues/3346
[i3342]: https://github.com/RPTools/maptool/issues/3342
[i3334]: https://github.com/RPTools/maptool/issues/3334
[i3331]: https://github.com/RPTools/maptool/issues/3331
[i3322]: https://github.com/RPTools/maptool/issues/3322
[i3320]: https://github.com/RPTools/maptool/issues/3320
[i3311]: https://github.com/RPTools/maptool/issues/3311
[i3298]: https://github.com/RPTools/maptool/issues/3298
[i3293]: https://github.com/RPTools/maptool/issues/3293
[i3286]: https://github.com/RPTools/maptool/issues/3286
[i3274]: https://github.com/RPTools/maptool/issues/3274
[i3256]: https://github.com/RPTools/maptool/issues/3256
[i3254]: https://github.com/RPTools/maptool/issues/3254
[i3234]: https://github.com/RPTools/maptool/issues/3234
[i3231]: https://github.com/RPTools/maptool/issues/3231
[i3218]: https://github.com/RPTools/maptool/issues/3218
[i3146]: https://github.com/RPTools/maptool/issues/3146
[i3102]: https://github.com/RPTools/maptool/issues/3102
[i3101]: https://github.com/RPTools/maptool/issues/3101
[i3052]: https://github.com/RPTools/maptool/issues/3052
[i2764]: https://github.com/RPTools/maptool/issues/2764
[i2763]: https://github.com/RPTools/maptool/issues/2763
[i2730]: https://github.com/RPTools/maptool/issues/2730
[i2348]: https://github.com/RPTools/maptool/issues/2348
[i2094]: https://github.com/RPTools/maptool/issues/2094
[i1931]: https://github.com/RPTools/maptool/issues/1931
[i1904]: https://github.com/RPTools/maptool/issues/1904
[i1550]: https://github.com/RPTools/maptool/issues/1550
