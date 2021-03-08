| Current Development Status | Localization |
| :--:                       |   :--:       |
| ![Build Verification](../../workflows/Build%20Verification/badge.svg?branch=develop) | [![Crowdin](https://badges.crowdin.net/maptool/localized.svg)](https://crowdin.com/project/maptool)|



MapTool
=======

Welcome to the MapTool repository for versions 1.4+. The old version (1.3) still resides on [SourceForge](http://sourceforge.net/p/rptools/svn/HEAD/tree/) but will not be updated going forward.

So what is MapTool? 
-------------------

MapTool is much more than just a mapping program. Included are not only powerful tools for the creation of detailed maps, but also a chat function, detailed token management (allowing you to track properties of the units placed on the map and display health and status information), and an initiative tracker. Functions not being used can be hidden out of sight. These features enable your computer screen to function as a virtual table-top, fulfilling the role of battlemats and dry-erase markers and providing for infinitely scrollable maps and the ability to use a large-screen TV or video projector.  It is _game system agnostic_, meaning that while MapTool has special support for some game systems (like hexes for GURPS or square templates for D&D 4E) there is no requirement for you to use these features.

The best part is all this isn't limited to a single PC. You can utilize the integrated server function to connect to players wherever there's an Internet connection. Miss your buddy in Singapore? Not a problem. Roll up that character, and then your sleeves.

And we don't stop there! Not content with just emulating the tabletop, we seek to improve upon the tabletop experience. This, truly, is where MapTool shines. A set of "topology" and vision features enable the GM to limit the view of his players, revealing the map as they explore it. Walls and other objects can prevent players from seeing what lies behind them. Darkness can be made to fall, blinding players who lack a light. Lights can be set on objects or on tokens that illuminate however much of the map you wish. If the GM wishes, s/he can limit the view of players to what their specific token sees, adding a whole new level to the experience of splitting up the party.  Woe be to the character who can't see around the corner of a wall but steps out into the view of the enemy!

For Users
------------
There are several tutorials on the [MapTool wiki](https://wiki.rptools.info/index.php/Main_Page) on getting started using MapTool.

Requirements
------------

- MapTool from version 1.8+ requires [Java 14](https://github.com/AdoptOpenJDK) and the RPTools builds use AdoptOpenJDK. Installers for MapTool include an embedded JRE that will install along with MapTool
- Building MapTool requires the corresponding Java Development Kit (JDK): [How To Install JDK](doc/How_To_Install_JDK.md)

Version Numbers
---------------

Our plan to start using [Semantic Versioning](https://semver.org/) more closely from 1.6.0 and up.

All the exciting new features will be happening in the development builds.  Major bugs or security fixes will be ported between the stable and development branches so that they are available in both.

Resources
---------

 - **Website:** http://rptools.net/ 
 - **Forums:**  http://forums.rptools.net 
 - **Wiki:**    https://wiki.rptools.info/index.php/Main_Page
 - **Discord:** https://discord.gg/gevEtpC
 

Configuration Steps Prior to Building MapTool
---------------------------------------------

See [the Contributor Setup page](https://github.com/RPTools/maptool/wiki/Contributor-Setup-Instructions-For-MapTool) for instructions on building MapTool and contributing to the project.


Recommended IDE
----------------
We currently recommend [IntelliJ IDEA](https://www.jetbrains.com/idea/) as our editor of choice although Eclipse and other IDE's should work just fine as well. For IntelliJ IDEA, simply open the project folder and it will detect it as a Gradle project and you should be ready to go with minimal effort.


Code Commits and Pull Requests
--------------------------------
We follow GitFlow process for the most part so please work all issues off of our `develop` branch. If you code changes are substantial, me may repoint the pull request to it's own feature branch for testing and further development.

We prefer all pull requests to be preceded and reference an Issue before we accept and merge. If there is not currently an open issue, please create one and leave a comment if you plan to work on the issue. When you commit your code, please reference the issue, e.g. `fixes #1234` in addition to any other comments.


Optional
--------

- [How To Setup User Interface (UI) Tools for MapTool](doc/How_To_Setup_UI_Tools.md)
