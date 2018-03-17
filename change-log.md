MapTool 1.4.4.1 - _Infused with Nerps!_
=====
This update has no functional changes to MapTool and only updates the underlying JRE used to Java
The update has a few improvements from Java 8 but mainly brings a much better HDPI experience under Linux & Windows! Scaling of graphics and text are cleaner and not pixelated or blurry as it was under Java 8.

Since this is only updates the JRE, if you have any issues you should be able to roll back to 1.4.4.0 without issue and any issues should be related to the Java 9 and we can tackle them as such. If you have any issues, please open an issue on GitHub and I will look into it.


MapTool 1.4.4.0 - _Infused with Nerps!_
=====
I'm excited to bring to you the next version of MapTool Nerps! And along with it, a brand new packaging scheme using Oracle's javapackager to create native installer packages and with it, no more Launcher application. *_See Edit -> Preferences -> Startup to adjust JVM parameters._

The Nerps build now uses it's own datadir in your app folder (.maptool-nerps) so you can run it side by side without interference of the main RPTools branch/distribution!

___

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
