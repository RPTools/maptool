# MapTool 1.11.0
Feature release using OpenJDK 16

## Highlights
- Terrain VBL - new VBL type allows vision to see into but not through an area enclosed with Terrain VBL.
- Initial support for "Add-On" Libraries (not lib:tokens).
- New and updated macro functions.
- Long-time Java bug causing MapTool to hang when dropping images into Edit Token dialog fixed with new Java release.

## Enhancements & Features
- [#3103][i3103] Additional properties added to output of `getFrameProperties()`
  - `tabtitle`, `html5`, `temporary`, `visible`, `docked`, `floating`, `autohide`, `undocked_x`, `undocked_y`, `undocked_h`, `undocked_w`
- [#3098][i3098] Universal VTT import now uses Terrain VBL for Object LOS Blocking.  
- [#3077][i3077] Initial support for "Add-On" libraries that can contain MTScript, JavaScript, HTML, CSS, assets and more in a zip file. New macro functions in support:
  - library.listAddOnLibraries() Lists the add on libraries
  - library.getInfo(namespace) Gets information about a library (either Add-On or lib:token)
  - library.listTokenLibraries(namespace) Lists the Lib:tokens in the campaign
  - library.getContents(namespace) Lists the contents of a library
- [#3073][i3073] Macro function `getInfo("server")` now returns additional properties:
  - `useWebRTC`, `usePasswordFile`, `server name`, `port number`
- [#2982][i2982] Use of Direct3D by Java disabled by default to avoid display issues.
- [#2755][i2755] New VBL type, Terrain VBL, added. Can see into but not through areas enclosed in Terrain VBL.
- [#2431][i2431] New macro functions to show/hide Overlays:
  - `[r: setOverlayVisible(OverlayName, {1|0})]`
  - `[r: visible = isOverlayVisible(OverlayName)] // returns 1|0`
 
## Bug Fixes
- [#3114][i3114] MBL/VBL/TVBL misaligned on imported UVTT maps that were cropped during export. Fixed.
- [#3112][i3112] Possible exceptions when multiple instances of MapTool tried to backup campaigns at the same time. Fixed.
- [#3093][i3093] MapTool hanging when opening a campaign whose assets were not already in assetcache. Not in released code. Fixed.
- [#3088][i3088] Player-editable macros on an unowned token should be treated as trusted. Fixed.
- [#3081][i3081] NPE when using `library.listTokenLibraries()` that were missing/unset properties. Fixed.
- [#3062][i3062] Preferences dialog had a mispelled and pointless tooltip. Removed.
- [#3061][i3061] Bad Universal VTT files causing Null Pointer Exceptions. Code added to catch issue. 
- [#3013][i3013] Various macro functions were using case-sensitive function name comparisons. Changed to use case-insensitive comparisons.
- [#2781][i2781] MapTool could freeze when dropping images into Edit Token dialog. Fixed.
- [#233][i233] Users could inadvertently advance initiative with Spacebar or Enter keys. Fixed.

## Other
- [#3100][i3100] Builds updated to use Adoptium JDK 16.0.2_7.
- [#3062][i3062] Map -> Import Dungeondraft VTT... changed to Map -> Import Unversal VTT...
- [#1347][i1347] Several classes added to facilitate using JFX panels in Maptool.

[Change Log for 1.10.4](https://github.com/RPTools/maptool/blob/1.10.4/CHANGE_LOG.md)

[i]: https://github.com/RPTools/maptool/issues/
[i]: https://github.com/RPTools/maptool/issues/
[i]: https://github.com/RPTools/maptool/issues/
[i]: https://github.com/RPTools/maptool/issues/
[i3114]: https://github.com/RPTools/maptool/issues/3114
[i3112]: https://github.com/RPTools/maptool/issues/3112
[i3103]: https://github.com/RPTools/maptool/issues/3103
[i3100]: https://github.com/RPTools/maptool/issues/3100
[i3098]: https://github.com/RPTools/maptool/issues/3098
[i3093]: https://github.com/RPTools/maptool/issues/3093
[i3088]: https://github.com/RPTools/maptool/issues/3088
[i3081]: https://github.com/RPTools/maptool/issues/3081
[i3077]: https://github.com/RPTools/maptool/issues/3077
[i3073]: https://github.com/RPTools/maptool/issues/3073
[i3062]: https://github.com/RPTools/maptool/issues/3062
[i3061]: https://github.com/RPTools/maptool/issues/3061
[i3013]: https://github.com/RPTools/maptool/issues/3013
[i2982]: https://github.com/RPTools/maptool/issues/2982
[i2781]: https://github.com/RPTools/maptool/issues/2781
[i2755]: https://github.com/RPTools/maptool/issues/2755
[i2431]: https://github.com/RPTools/maptool/issues/2431
[i1347]: https://github.com/RPTools/maptool/issues/1347
[i233]: https://github.com/RPTools/maptool/issues/233


