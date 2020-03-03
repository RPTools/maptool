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
