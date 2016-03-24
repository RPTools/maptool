MapTool
=======

Welcome to the MapTool 1.4 repository. The old version (1.3) still resides on [SourceForge](http://sourceforge.net/p/rptools/svn/HEAD/tree/) but should only be updated with major bug fixes.

So what is MapTool? 
-------------------

MapTool is much more than just a mapping program. Included are not only powerful tools for the creation of detailed maps, but also a chat function, detailed token management (allowing you to track properties of the units placed on the map and display health and status information), and an initiative tracker. Functions not being used can be hidden out of sight. These features enable your computer screen to function as a virtual table top, filling the role of battlemats and dry-erase markers and providing for infinitely scrollable maps and the ability to use a large-screen TV or video projector.  It is "game system agnostic", meaning that while MapTool has special support for some game systems (like hexes for GURPS or square templates for D&D 4E) there is no requirement for you to use these features.

The best part is all this isn't limited to a single PC. You can utilize the integrated server function to connect to players wherever there's an Internet connection. Miss your buddy in Singapore? Not a problem. Roll up that character, and then your sleeves.

And we don't stop there! Not content with just emulating the tabletop, we seek to improve upon the tabletop experience. This, truly, is where MapTool shines. A set of "topology" and vision features enable the GM to limit the view of his players, revealing the map as they explore it. Walls and other objects can prevent players from seeing what lies behind them. Darkness can be made to fall, blinding players who lack a light. Lights can be set on objects or on tokens that illuminate however much of the map you wish. If the GM wishes, s/he can limit the view of players to what their specific token sees, adding a whole new level to the experience of splitting up the party.  Woe be to the character who can't see around the corner of a wall but steps out into the view of the enemy!

Requirements
------------

- MapTool 1.4 requires [Java 1.7+](https://java.com/en/download/)
- Building MapTool requires the Java Development Kit (JDK): [How To Install JDK](doc/How_To_Install_JDK.md)

Resources
---------

 - **Website:** http://rptools.net/ 
 - **Forums:**  http://forums.rptools.net 
 - **Wiki:**    http://lmwcs.com/rptools/wiki/Main_Page 

Building Maptool
----------------

First, [install the JDK](doc/How_To_Install_JDK.md).

[Gradle](http://gradle.org/) is used to build MapTool 1.4. You do not need Gradle installed to perform the build as the repository has a small wrapper that will download and install it in a subdirectory for you. This means that the first time you do a build you will need to be connected to the internet and it will take a while
as it downloads everything it needs.

```Shell
./gradlew build  
```

For Windows, remember to flip the slash:

```Shell
.\gradlew build  
```

Building the Distributable Zip
------------------------------

```Shell
./gradlew clean release
```

For Windows, remember to flip the slash:

```Shell
.\gradlew clean release 
```

This will create a `.zip` file for use on all systems as well as a zipped `.app` for Mac OS X in the `maptool/build/` directory. The build number will be based on the latest tag and latest commit.

Developers
----------

Please follow our [Code Style and Guidelines](doc/Code_Style_and_Guidelines.md) when submitting patches and pull requests.


Optional
--------

- [How To Setup User Interface (UI) Tools for Maptool](doc/How_To_Setup_UI_Tools.md)
- [How To Setup Eclipse for MapTool](doc/How_To_Setup_Eclipse.md)
