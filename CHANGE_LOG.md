# MapTool 1.11.0
Feature release using OpenJDK 16

## Highlights
- New VBL Modes - new VBL modes Hill and Pit.
- Initial support for "Add-On" Libraries (not lib:tokens).
  - See https://rptools-doc.craigs-stuff.net/blog/add-on-libraries/
  - And https://github.com/RPTools/maptool/pull/3158
- New and updated macro functions.
- Long-time Java bug causing MapTool to hang when dropping images into Edit Token dialog fixed with new Java release.

## Enhancements & Features

- [#3200][i3200] New MapTool Easy Connect™ option for servers to allow players to submit their Public Key information at connectiong for approval by GM. 
- [#3171][i3171] Extend `@this` behaviour to the data.getStaticData function.
- [#3164][i3164] New macro function `data.getStaticData(namespace, path)` for accessing static data in Add-On libraries.
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
- [#3057][i3057] New `getInfo("theme")` option returns the color information for the various UI elements.
- [#2984][i2984] Campaign Panel now part of default layout.
- [#2982][i2982] Use of Direct3D by Java disabled by default to avoid display issues.
- [#2935][i2935] `getInfo("server")` now includes `corner` info for Corner Image types, `grid` for the Grid * types and the `group` name is included in each state entry.
- [#2777][i2777] New server option to disable Map Selection on player clients.
- [#2755][i2755] New VBL types and changes:
  - Hill - Can see into but not through areas enclosed in Hill VBL.
  - Pit - Tokens outside of Pit VBL can see over but tokens inside can only see within.
  - Regular VBL is now called Wall. No changes to functionality.
- [#2431][i2431] New macro functions to show/hide Overlays:
  - `[r: setOverlayVisible(OverlayName, {1|0})]`
  - `[r: visible = isOverlayVisible(OverlayName)] // returns 1|0`
- [#2001][i2001] New server options to disable Lock Player Movement & Lock Token Editor on Player clients.
- [#1385][i1385] New macro functions to create tokens.
  - `createToken(tokenValues)` to create a token from a JsonObject
  - `createTokens(arrayofTokenValues)`  to create multiple tokens from a JsonArray of JsonObjects.
- [#1104][i1104] New server option to disable Player access to Resource Library. 
 
## Bug Fixes
- [#3191][i3191] `isNumber()` was returning false (0) for numeric strings with whitespace padding or a `+` sign. Fixed.
- [#3183][i3183] Exporting lib:token as Add-On failing under Windows. Fixed.
- [#3176][i3176] `getLibraryProperty()` not handling numeric values correctly. Fixed.
- [#3175][i3175] Errors thrown when deleting lib:tokens. Fixed.
- [#3174][i3174] `onCampaignLoad` not being called consistently for lib:tokens. Fixed.
- [#3159][i3159] Allow URI Access fag was being reset on server start. Fixed.
- [#3140][i3140] Unable to add/edit Bars if Type was localized. Fixed.
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
- [#3204][i3204] Some i18n text strings reworked for new features. 
- [#3100][i3100] Builds updated to use Adoptium JDK 16.0.2_7.
- [#3062][i3062] Map -> Import Dungeondraft VTT... changed to Map -> Import Unversal VTT...
- [#1348][i1348] Internal code work to support adding HTML, CSS, JavaScript, Audio, etc. in campaign files.
- [#1347][i1347] Several classes added to facilitate using JFX panels in Maptool.
- [#1346][i1346] Internal code work to extend Asset class to support data other than images.


[Change Log for 1.10.4](https://github.com/RPTools/maptool/blob/1.10.4/CHANGE_LOG.md)

[i]: https://github.com/RPTools/maptool/issues/
[i3200]: https://github.com/RPTools/maptool/issues/3200
[i3204]: https://github.com/RPTools/maptool/issues/3204
[i3191]: https://github.com/RPTools/maptool/issues/3191
[i3183]: https://github.com/RPTools/maptool/issues/3183
[i3176]: https://github.com/RPTools/maptool/issues/3176
[i3175]: https://github.com/RPTools/maptool/issues/3175
[i3174]: https://github.com/RPTools/maptool/issues/3174
[i3171]: https://github.com/RPTools/maptool/issues/3171
[i3170]: https://github.com/RPTools/maptool/issues/3170
[i3164]: https://github.com/RPTools/maptool/issues/3164
[i3159]: https://github.com/RPTools/maptool/issues/3159
[i3140]: https://github.com/RPTools/maptool/issues/3140
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
[i3057]: https://github.com/RPTools/maptool/issues/3057
[i3013]: https://github.com/RPTools/maptool/issues/3013
[i1385]: https://github.com/RPTools/maptool/issues/1385
[i1348]: https://github.com/RPTools/maptool/issues/1348
[i1347]: https://github.com/RPTools/maptool/issues/1347
[i1346]: https://github.com/RPTools/maptool/issues/1346
[i2984]: https://github.com/RPTools/maptool/issues/2984
[i2982]: https://github.com/RPTools/maptool/issues/2982
[i2935]: https://github.com/RPTools/maptool/issues/2935
[i2781]: https://github.com/RPTools/maptool/issues/2781
[i2777]: https://github.com/RPTools/maptool/issues/2777
[i2755]: https://github.com/RPTools/maptool/issues/2755
[i2431]: https://github.com/RPTools/maptool/issues/2431
[i]: https://github.com/RPTools/maptool/issues/
[i2001]: https://github.com/RPTools/maptool/issues/2001
[i1104]: https://github.com/RPTools/maptool/issues/1104
[i233]: https://github.com/RPTools/maptool/issues/233


