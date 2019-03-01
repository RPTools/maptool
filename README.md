[![Build Status](https://travis-ci.org/RPTools/maptool.svg?branch=merge-from-nerps)](https://travis-ci.org/RPTools/maptool)

MapTool
=======

Welcome to the MapTool 1.4 repository. The old version (1.3) still resides on [SourceForge](http://sourceforge.net/p/rptools/svn/HEAD/tree/) but will not be updated going forward.

So what is MapTool? 
-------------------

MapTool is much more than just a mapping program. Included are not only powerful tools for the creation of detailed maps, but also a chat function, detailed token management (allowing you to track properties of the units placed on the map and display health and status information), and an initiative tracker. Functions not being used can be hidden out of sight. These features enable your computer screen to function as a virtual table top, filling the role of battlemats and dry-erase markers and providing for infinitely scrollable maps and the ability to use a large-screen TV or video projector.  It is "game system agnostic", meaning that while MapTool has special support for some game systems (like hexes for GURPS or square templates for D&D 4E) there is no requirement for you to use these features.

The best part is all this isn't limited to a single PC. You can utilize the integrated server function to connect to players wherever there's an Internet connection. Miss your buddy in Singapore? Not a problem. Roll up that character, and then your sleeves.

And we don't stop there! Not content with just emulating the tabletop, we seek to improve upon the tabletop experience. This, truly, is where MapTool shines. A set of "topology" and vision features enable the GM to limit the view of his players, revealing the map as they explore it. Walls and other objects can prevent players from seeing what lies behind them. Darkness can be made to fall, blinding players who lack a light. Lights can be set on objects or on tokens that illuminate however much of the map you wish. If the GM wishes, s/he can limit the view of players to what their specific token sees, adding a whole new level to the experience of splitting up the party.  Woe be to the character who can't see around the corner of a wall but steps out into the view of the enemy!

Requirements
------------

- MapTool 1.4 requires [Java 1.7+](https://java.com/en/download/) although current iterations have an embedded JRE that will install along with MapTool
- Building MapTool requires the Java Development Kit (JDK): [How To Install JDK](doc/How_To_Install_JDK.md)

Version Numbers
---------------

The RPTools team intends to use the "1.4" moniker until such time as changes are made that break backward compatibility with reading user data files (campaigns, maps, tokens, etc).  At that point, the version number will be bumped to "1.5".  (Note that the format of exported data may change without bumping the version number, as long as the older file format can still be read and used.)  In any case, the next digit will be even-numbered for stable releases and odd-numbered for development builds.

All of the exciting new features will be happening in the development builds.  Major bugs or security fixes will be ported between the stable and development branches so that they are available in both.

Resources
---------

 - **Website:** http://rptools.net/ 
 - **Forums:**  http://forums.rptools.net 
 - **Wiki:**    http://lmwcs.com/rptools/wiki/Main_Page 

Configuration Steps Prior to Building MapTool
---------------------------------------------

First, [install the JDK](doc/How_To_Install_JDK.md).

Second, clone the GitHub repository (this one or one that you have forked) to your local system.  If you are cloning your own fork, change the URL as appropriate.

```
git clone git@github.com:RPTools/maptool.git
```

From here on, it is expected that you are running these commands from within the directory that was created when you cloned the repository (referred to as the _working directory_ in Git-speak).

[Gradle](http://gradle.org/) is used to build MapTool 1.4. You do not need Gradle installed to perform the build as the repository has a small wrapper that will download and install it in a subdirectory for you. This means that the first time you run Gradle, you will need to be connected to the Internet and it will take a while as it downloads everything it needs.

(Note that Java 9 requires Gradle 4.x+ -- older versions of Gradle _will not work_!)

* On Linux and macOS (and other Unix systems):
```
./gradlew wrapper --gradle-version=4.5.1 --distribution-type=bin
```

* On Windows, remember to use the backslash instead:
```
.\gradlew wrapper --gradle-version=4.5.1 --distribution-type=bin
```

You now have Gradle updated to version 4.5.1 with any updates applied to
the wrapper script itself as well.

Building MapTool
----------------

* On Linux and macOS (and other Unix systems):
```
./gradlew build
```

* On Windows, remember to use the backslash instead:
```
.\gradlew build
```

Building the Distributable Zip
------------------------------

* On Linux and macOS (and other Unix systems):
```
./gradlew clean release
```

* On Windows, remember to use the backslash instead:
```
.\gradlew clean release 
```

This will create a `.zip` file for use on all systems (as well as a zipped `.app` for macOS) in the `maptool/build/` subdirectory. The build number will be based on the latest tag and latest commit.

Contributors
------------

Please follow our [Code Style and Guidelines](doc/Code_Style_and_Guidelines.md) when submitting patches and pull requests.


Optional
--------

- [How To Setup Eclipse for MapTool](doc/How_To_Setup_Eclipse.md)
- [How To Setup User Interface (UI) Tools for MapTool](doc/How_To_Setup_UI_Tools.md)
