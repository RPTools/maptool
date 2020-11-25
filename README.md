| Current Development Status | Localization |
| :--:                       |   :--:       |
| ![Build Verification](../../workflows/Build%20Verification/badge.svg?branch=develop) | [![Crowdin](https://badges.crowdin.net/maptool/localized.svg)](https://crowdin.com/project/maptool)|



MapTool
=======

Welcome to the MapTool repository for versions 1.4+. The old version (1.3) still resides on [SourceForge](http://sourceforge.net/p/rptools/svn/HEAD/tree/) but will not be updated going forward.

So what is MapTool? 
-------------------

MapTool is much more than just a mapping program. Included are not only powerful tools for the creation of detailed maps, but also a chat function, detailed token management (allowing you to track properties of the units placed on the map and display health and status information), and an initiative tracker. Functions not being used can be hidden out of sight. These features enable your computer screen to function as a virtual table top, filling the role of battlemats and dry-erase markers and providing for infinitely scrollable maps and the ability to use a large-screen TV or video projector.  It is "game system agnostic", meaning that while MapTool has special support for some game systems (like hexes for GURPS or square templates for D&D 4E) there is no requirement for you to use these features.

The best part is all this isn't limited to a single PC. You can utilize the integrated server function to connect to players wherever there's an Internet connection. Miss your buddy in Singapore? Not a problem. Roll up that character, and then your sleeves.

And we don't stop there! Not content with just emulating the tabletop, we seek to improve upon the tabletop experience. This, truly, is where MapTool shines. A set of "topology" and vision features enable the GM to limit the view of his players, revealing the map as they explore it. Walls and other objects can prevent players from seeing what lies behind them. Darkness can be made to fall, blinding players who lack a light. Lights can be set on objects or on tokens that illuminate however much of the map you wish. If the GM wishes, s/he can limit the view of players to what their specific token sees, adding a whole new level to the experience of splitting up the party.  Woe be to the character who can't see around the corner of a wall but steps out into the view of the enemy!

Requirements
------------

- MapTool 1.5 requires [Java 10](https://www.oracle.com/technetwork/java/javase/archive-139210.html) although current iterations have an embedded JRE that will install along with MapTool
- Building MapTool requires the Java Development Kit (JDK): [How To Install JDK](doc/How_To_Install_JDK.md)

Version Numbers
---------------

The RPTools team intends to use the "1.5" moniker until such time as changes are made that break backward compatibility with reading user data files (campaigns, maps, tokens, etc).  At that point, the version number will be bumped to "1.6".  (Note that the format of exported data may change without bumping the version number, as long as the older file format can still be read and used). Our plan to start using [Semantic Versioning](https://semver.org/) more closely from 1.6.0 and up.

All of the exciting new features will be happening in the development builds.  Major bugs or security fixes will be ported between the stable and development branches so that they are available in both.

Resources
---------

 - **Website:** http://rptools.net/ 
 - **Forums:**  http://forums.rptools.net 
 - **Wiki:**    http://lmwcs.com/rptools/wiki/Main_Page 
 - **Discord:** https://discord.gg/gevEtpC
 

Configuration Steps Prior to Building MapTool
---------------------------------------------

First, [install the JDK](doc/How_To_Install_JDK.md).

Second, clone the GitHub repository (this one or one that you have forked) to your local system.  If you are cloning your own fork, change the URL as appropriate.

```
git clone git@github.com:RPTools/maptool.git
```

From here on, it is expected that you are running these commands from within the directory that was created when you cloned the repository (referred to as the _working directory_ in Git-speak).

[Gradle](http://gradle.org/) is used to build MapTool. You do not need Gradle installed to perform the build as the repository has a small wrapper that will download and install it in a subdirectory for you. This means that the first time you run Gradle, you will need to be connected to the Internet and it will download and cache the version of gradle our project is currently using as well as any dependencies.

Building MapTool from Terminal*
---------------------------------
```
./gradlew clean build
```

*On Windows, depending on the terminal you use, you may need to use `\` instead of `/`. We recommend using the new Windows Terminal and Powershell 7 or WSL 2.*

Contributors
===
Please follow our [Code Style and Guidelines](doc/Code_Style_and_Guidelines.md) when committing your code and submitting pull requests. We enforce code style using spotless, to insure your build passes our automatic checks, you can run the following gradle command before committing:
```
./gradlew spotlessApply
```
Also read the Wiki: https://github.com/RPTools/maptool/wiki/Contributor-Setup-Instructions-For-MapTool. Valuable information on contributing is only found there currently.

Recommended IDE
----------------
We currently recommend [IntelliJ IDEA](https://www.jetbrains.com/idea/) as our editor of choice although Eclipse and other IDE's should work just fine as well. For IntelliJ IDEA, simply open the project folder and it will detect it as a Gradle project and you should be ready to go with minimal effort.


Code Commits and Pull Requests
--------------------------------
We follow GitFlow process for the most part so please work all issues off of our `develop` branch. If you code changes are substantial, me may repoint the pull request to it's own feature branch for testing and further development.

We prefer all pull requests to be preceded and reference an Issue before we accept and merge. If there is not currently an open issue, please create one and leave a comment if you plan to work on the issue. When you commit your code, please reference the issue, e.g. `fixes #1234` in addition to any other comments.


Native Installers
===
Normally not required or needed during development, but if you need to create and test a native distribution, you must build it on the OS that you wish to create the native distribution for. We currently use Travis CI & AppVeyor to test, build, & deploy or natives for us at release time. You can set up your own CI process on your fork if you want, please review the relative yml files for those services and their documentation. This is currently out of scope and not required for development but may be documented at a later date.

The command to create a release locally is:
```
 ./gradlew deploy
```

Additional Requirements for Windows
------------------------------------
To create .exe installs, you will need [Inno Setup](https://www.jrsoftware.org/isinfo.php).
To create .msi installs, you will need [Wix](https://wixtoolset.org/)

Make sure your JAVA_HOME is set to point to JDK 10.

Installers (both .msi and .exe) will be created in the releases subdirectory.
If you installed a more recent version of Inno Setup than is currently used for MapTool you may have to update the MapTool.iss file to set the proper MinVersion. (Inno Setup will complain if you do not.)


Additional Requirements for Linux
----------------------------------
Review the [documentation](https://docs.oracle.com/javase/10/deploy/self-contained-application-packaging.htm#JSDPG1089) for any additional requirements depending on your target.

Depending if you wish to create a .rpm or .deb file, you may or may not have everything required.


Building the Distributable Zip
------------------------------
*This method of distribution has been deprecated but you are free to use for your own purposes*

* On Linux and macOS (and other Unix systems):
```
./gradlew clean release
```

* On Windows, remember to use the backslash instead:
```
.\gradlew clean release 
```

This will create a `.zip` file for use on all systems (as well as a zipped `.app` for macOS) in the `maptool/build/` subdirectory. The build number will be based on the latest tag and latest commit.


Optional
--------

- [How To Setup Eclipse for MapTool](doc/How_To_Setup_Eclipse.md)
- [How To Setup User Interface (UI) Tools for MapTool](doc/How_To_Setup_UI_Tools.md)
