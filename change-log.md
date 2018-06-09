MapTool 1.4.5.0 - _Infused with Nerps!_
=====
I'm excited to bring to you the next version of MapTool Nerps! I have some new exciting features to showcase, namely automated Pathfinding for tokens! There is also a huge improvement to performance for Lights as well as several other bug fixes. A new vision/light type of GRID has also been added, which is a circle of the specified size but only lights up the affected grid cells within range, much like using the template tool.

The packaged JRE has also been updated to Java 10 to stay current, although you shouldn't see any changes over Java 9.

___

Enhancements
-----
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

Bug Fixes
-----
* [#68][i68] - MapTool's i18n language override enabled via Edit -> Preferences -> Startup tab. This will override your default language set by your OS.
* [#41][i41] - Allow player owned tokens without "sight" to move within currently exposed FoW, e.g. areas other PC token can currently see.
* [#44][i44] - If running the JAR version with your own JRE installed, Edit -> Preferences will no longer throw and error and blow up! Instead the Startup tab will be grayed out (as those values will not be used and you must make your own startup script to set memory settings when using the JAR format)
* [#27][i27] - Code cleanup, .pdf & .por files will no longer throw errors to the log file.
* [#65][i65] - Gradle will once again build and deploy vs throw an error getting the branch name.
* [#179][i179] - Pulled in @Jaggeroth change from main RPTool's Repo; ZOrder sort violation problem by restoring the original comparator and only using the new figure comparator when sorting figure only.

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

___

MapTool 1.4.4.2 - _Infused with Nerps!_
=====
This is a hotfix release to correct issues with Java 9 and macOS as reported via github as well as fixing the auto update check.

Bug Fixes
-----
* [#54][i54]  - Missing Preferences menu option on OS X
* [#59][i59]  - App does not check for new versions in 1.4.4.1

[i54]: https://github.com/JamzTheMan/MapTool/issues/54
[i59]: https://github.com/JamzTheMan/MapTool/issues/59

___

MapTool 1.4.4.1 - _Infused with Nerps!_
=====
This update has no functional changes to MapTool and only updates the underlying JRE used to Java
The update has a few improvements from Java 8 but mainly brings a much better HDPI experience under Linux & Windows! Scaling of graphics and text are cleaner and not pixelated or blurry as it was under Java 8.

Since this is only updates the JRE, if you have any issues you should be able to roll back to 1.4.4.0 without issue and any issues should be related to the Java 9 and we can tackle them as such. If you have any issues, please open an issue on GitHub and I will look into it.

___

MapTool 1.4.4.0 - _Infused with Nerps!_
=====
I'm excited to bring to you the next version of MapTool Nerps! And along with it, a brand new packaging scheme using Oracle's javapackager to create native installer packages and with it, no more Launcher application. *_See Edit -> Preferences -> Startup to adjust JVM parameters._

The Nerps build now uses it's own datadir in your app folder (.maptool-nerps) so you can run it side by side without interference of the main RPTools branch/distribution!


Enhancements
-----
* [#8][i8]  - New packaging mechanism! OS specific "installs" are now being generated using Oracles native javapackager tool. It packages the JAR, native executable, and JRE in the following packages: .exe (Windows), .dmg & .pkg (MacOS), & .deb (Linux) as well as unified JAR that can be run manually.
* [#26][i26] - MapTool now checks and alerts user when a new version is available as a GitHub release and allow you to download it! You can "skip" a version to stop alerting until the next release comes out or cancel all auto update checks. It will download the release based on your OS (.exe, .pkg, or .deb)
* [#9][i9]  - New macro function added to change token ownership to 'Owned by All', setOwnedByAll(boolean [,tokenID]) returns boolean
* [#22][i22] - Darkvision changed to Darkvision: circle r60 (removed distance=62.5)
* [#25][i25] - Cone lights now accept 'offset=x' as an option just like vision
* [#33][i33] - Java stack traces are sent automatically to Sentry.io for aggregation and notification. No private info is gathered or sent. This lets me know if an unreported bug shows up and how critical may be so I can get it fixed quicker and with minimal info needed from users.


Bug Fixes
-----
* [#5][i5]  - Adding new state causes java.lang.ArrayIndexOutOfBoundsException no more!
* [#6][i6]  - Fixed various Typos
* [#15][i15] - Lighting wasn't immediately forced to connected clients and should be fixed now.
* [#18][i18] - Exporting Campaigns back to 1.4.0.1 was failing due to new objects added. You can now export any campaign back to 1.4.0.1 but as always, this is permanent in that it will strip out new objects as in TokenVBl, new lighting options, etc but macros will not be touched and may fail if they contain new macro functions not available in 1.4.0.1.
* [#19][i19] - Default save location for tokens are now remembered!
* [#20][i20] - Added missing documentation for lights/vision, e.g. 'scale'
* [#21][i21] - Lights are not updating properly based on ownership looked like it was tied to other 'light' bugs and should now be fixed.
* [#23][i23] - Fixed sendToBack & bringToFront macros broke states and bar changes in the macro. This was an OLD one going back to 1.3b63! You can now safely use these functions in your macro now!
* [#30][i30] - Players see NPC movement when there are no lights no more! This was another old bug going back to 1.3b-something and only showed itself if you had NO lights (including personal lights, aka darkvision).

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
