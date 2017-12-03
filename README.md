[![Build status](https://ci.appveyor.com/api/projects/status/o4hmpca4mert1k0i/branch/1.4.4.0?svg=true)](https://ci.appveyor.com/project/JamzTheMan/maptool/branch/1.4.4.0)
[![Build Status](https://travis-ci.org/JamzTheMan/MapTool.svg?branch=1.4.4.0)](https://travis-ci.org/JamzTheMan/MapTool)

MapTool
=======

Welcome to my fork of MapTool, now infused with Nerps!. The old version (1.3) still resides on [SourceForge](http://sourceforge.net/p/rptools/svn/HEAD/tree/) but should only be updated with major bug fixes.


What's with the Infused with Nerps?
-----
It's simply my way of telling you, the user, this is my fork of MapTool and not the official RPTools version.  I've been a passionate user and contributer of MapTool for over 7 years now. I enjoy coding in Java as a hobby and use MapTool in my weekly game (currently Pathfinder) and plan to do so indefinetly. _My gaming group has been going strong since 1991!_

Why am I telling random people on the internet this? So you know this is not just some one off fork that will be dead in 6 months. If there is a bug and it's hampering my game...or there is some enhancement that I simply must have to make my game better, I'm going to code it. And as a byproduct, you the user also get those fixes/enhancements for the low low price of *free*!

Lastly, all code I write is for fun and as such, open source for consumption upstream to RPTools. Feel free to fork from the main repository or mine and submit contributions! If you find any bug/flaws or have a really cool idea for an enhancement, feel free to create an issue on GitHub. It's the best way to catch my attention! You can also join fellow users on Discord: https://discord.gg/gevEtpC

___

So what is MapTool?
-----

MapTool is much more than just a mapping program. Included are not only powerful tools for the creation of detailed maps, but also a chat function, detailed token management (allowing you to track properties of the units placed on the map and display health and status information), and an initiative tracker. Functions not being used can be hidden out of sight. These features enable your computer screen to function as a virtual table top, filling the role of battlemats and dry-erase markers and providing for infinitely scrollable maps and the ability to use a large-screen TV or video projector.  It is "game system agnostic", meaning that while MapTool has special support for some game systems (like hexes for GURPS or square templates for D&D 4E) there is no requirement for you to use these features.

The best part is all this isn't limited to a single PC. You can utilize the integrated server function to connect to players wherever there's an Internet connection. Miss your buddy in Singapore? Not a problem. Roll up that character, and then your sleeves.

And we don't stop there! Not content with just emulating the tabletop, we seek to improve upon the tabletop experience. This, truly, is where MapTool shines. A set of "topology" and vision features enable the GM to limit the view of his players, revealing the map as they explore it. Walls and other objects can prevent players from seeing what lies behind them. Darkness can be made to fall, blinding players who lack a light. Lights can be set on objects or on tokens that illuminate however much of the map you wish. If the GM wishes, s/he can limit the view of players to what their specific token sees, adding a whole new level to the experience of splitting up the party.  Woe be to the character who can't see around the corner of a wall but steps out into the view of the enemy!

Requirements
------------

- MapTool 1.4.4.x comes with it's own JRE. Only download and install java if you want to run the JAR file manually. [Java 1.8+](https://java.com/en/download/)
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

Running Maptool from source
----------------
```Shell
./gradlew run
```

For Windows, remember to flip the slash:

```Shell
.\gradlew run
```

Building a Native Executable
------------------------------

```Shell
./gradlew clean deploy
```

For Windows, remember to flip the slash:

```Shell
.\gradlew clean deploy
```

This will create an installable file for use on your operating system. Look in the 'releases' folder for the ouput. *Note: Additional requirements may be needed, ie Inno setup. See the offical documentation for more information:* https://docs.oracle.com/javase/8/docs/technotes/guides/deploy/self-contained-packaging.html


Developers
----------

Please follow our [Code Style and Guidelines](doc/Code_Style_and_Guidelines.md) when submitting patches and pull requests.


Optional
--------

- [How To Setup User Interface (UI) Tools for Maptool](doc/How_To_Setup_UI_Tools.md)
- [How To Setup Eclipse for MapTool](doc/How_To_Setup_Eclipse.md)
