# MapTool 1.10.0

Feature release using OpenJDK 16.

## Highlights

- User database with encrypted passwords and public key support for logging in to MapTool servers.
- WebRTC supported for connecting to servers without using port-forwarding.

## Enhancements & Features
- [#2994][i2994] Reserved namespaces added to avoid future conflicts: 
  - `.`, `_`, `builtin`, `builtin.`, `internal`, `internal.`, `maptool`, `maptool.`, `maptools`, `maptools.`, `net.rptools.`, `rptools`, `rptools.`, `standard`, `standard.`, `tokentool.`

- [#2964][i2964] New macro functions for Dialogs/Frames/overlays allow retrieving the content directly from lib:tokens with a URI. They otherwise function as the previous versions.
  - `html.dialog(name, liburi [, options ])`
  - `html.dialog5(name, liburi [, options ])`
  - `html.frame(name, liburi [, options ])`
  - `html.frame5(name, liburi [, options ])`
  - `html.overlay(name, liburi [, options ])`
- [#2963][i2963] Support for accessing CSS/JS/HTML from Lib:Tokens via URI in Dialog/Frame/Overlays added.
  - `lib://<tokename>/macro/<macro name>`
  - `lib://<tokename>/property/<property name>`
- [#2961][i2960] Support JavaScript UDFs via `MTScript.createFunction(funcName, jsFunction)`.
- [#2960][i2960] New macro support for multiple Graalvm JavaScript Scopes.
- [#2943][i2943] Experimental WebRTC supported for connecting to servers without port-forwarding.
- [#2919][i2919] Loading GIF anims from tokens or tables into Frame5 windows and Overlays now working. Only first frame loads with old Frame() windows.
- [#2915][i2915] Public/private key support for player login.
- [#2913][i2913] New UI (File -> Player Database) for creating/maintaining user password database.
- [#2912][i2912] New macro functions for accessing/managing player account/password database.
  - `player.getInfo(name)`, `player.getName()`, `player.getPlayers()`, `player.getConnected()`
- [#2908][i2908] New API for accessing account/password database via macros.
- [#2906][i2906] Optional, encrypted, per-user passwords now supported. 
- [#2885][i2885] Tokens now have Speech Names that will be used in speech bubbles.
- [#2879][i2879] New macro functions to get/set the flipped status of a token.
  - `flipTokenX()`, `flipTokenY()`, `flipTokenIso()`, `isFlippedX()`, `isFlippedY()`, `isFlippedIso()`
- [#2775][i2775] New macro function `getMapName()` to returns the GM Name from the Display Name.
- [#2774][i2774] MapTool no longer prompts to *Save Campaign* when no changes have been made.
- [#2801][i2801] New macro function `setDrawingName()`. Updated `findDrawings()` to return all drawings if passed `*` for drawing name.
- [#2519][i2519] JavaScript support moved to GraalVM-JS and previous functionality of `js.eval()` restored.
- [#2155][i2155] More performance improvements. Moving a token across a large map with very complex VBL and then Exposing Last Path results in a 2x+ performance improvement. 

## Bug Fixes
- [#3001][i3001] Missing I18n tag used in error when missing lib:token is used in URI. Fixed.  
- [#2986][i2986] URI access denied for players if containing token is unowned. Fixed.
- [#2970][i2970] Adding a hex-shaped light definition to campaign properties would cause an exception when reopening the campaign properties. Fixed.
- [#2955][i2955] Placing a grid aura on a token on a gridless map would cause repeating exceptions. Fixed.
- [#2916][i2916] Connect to Server dialog lacked a checkbox to specify using a public key to login. Fixed.
- [#2888][i2888] Speech bubbles didn't adapt to long nong names. Fixed.
- [#2887][i2887] Startup time for MapTool with a large asset cache could take several minutes. Fixed.
- [#2875][i2875] Restful functions passed variables or JSON for headers would throw errors. Fixed.
- [#2861][i2861] Bug causing an NPE with translated Bar locations. Fixed. 
- [#2775][i2775] `setMapDisplayName()` no longer allows setting duplicate names. 
- [#2741][i2741] Missing symbols under Linux/MacOS caused `<select>` with `multiple` to fail. Fixed.
- [#446][i446] Macro hotkeys not working when macro panels are hidden or floating. Fixed with caveat that they still won't work if a `Dialog`, `Dialog5` or `Frame5` are open and have focus.

## Other
- [#2931][i2931] Updated spotless plugin for support of Java 16 features. 
- [#2955][i2955]
- [#][i]

[Change Log for 1.9.3](https://github.com/RPTools/maptool/blob/1.9.3/CHANGE_LOG.md)

[i3001]: https://github.com/RPTools/maptool/issues/3001
[i2994]: https://github.com/RPTools/maptool/issues/2994
[i2986]: https://github.com/RPTools/maptool/issues/2986
[i2970]: https://github.com/RPTools/maptool/issues/2970
[i2964]: https://github.com/RPTools/maptool/issues/2964
[i2963]: https://github.com/RPTools/maptool/issues/2963
[i2961]: https://github.com/RPTools/maptool/issues/2961
[i2960]: https://github.com/RPTools/maptool/issues/2960
[i2955]: https://github.com/RPTools/maptool/issues/2955
[i2943]: https://github.com/RPTools/maptool/issues/2943
[i2931]: https://github.com/RPTools/maptool/issues/2931
[i2919]: https://github.com/RPTools/maptool/issues/2919
[i2916]: https://github.com/RPTools/maptool/issues/2916
[i2915]: https://github.com/RPTools/maptool/issues/2915
[i2913]: https://github.com/RPTools/maptool/issues/2913
[i2912]: https://github.com/RPTools/maptool/issues/2912
[i2908]: https://github.com/RPTools/maptool/issues/2908
[i2906]: https://github.com/RPTools/maptool/issues/2906
[i2888]: https://github.com/RPTools/maptool/issues/2888
[i2887]: https://github.com/RPTools/maptool/issues/2887
[i2885]: https://github.com/RPTools/maptool/issues/2885
[i2879]: https://github.com/RPTools/maptool/issues/2879
[i2875]: https://github.com/RPTools/maptool/issues/2875
[i2861]: https://github.com/RPTools/maptool/issues/2861
[i2801]: https://github.com/RPTools/maptool/issues/2801
[i2775]: https://github.com/RPTools/maptool/issues/2775
[i2774]: https://github.com/RPTools/maptool/issues/2774
[i2741]: https://github.com/RPTools/maptool/issues/2741
[i2519]: https://github.com/RPTools/maptool/issues/2519
[i2155]: https://github.com/RPTools/maptool/issues/2155
[i446]: https://github.com/RPTools/maptool/issues/446
