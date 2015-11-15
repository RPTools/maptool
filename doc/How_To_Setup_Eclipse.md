How To Setup Eclipse for MapTool
================================

Before you install Eclipse you may wish to consider whether or not you want to maintain separated Eclipse configurations and instances. Why would you do that? Well its perfectly possible for different IDE Plugins to disagree with each other, third party developers cannot always be relied upon. So if you put a lot of work into configuring an IDE for an important project, you may want a separate IDE install for messing around with MapTool ;) If you are only installing Eclipse for Maptool, it doesn't matter. You can run multiple configurations from one Eclipse install directory or separate ones if you want. But each configuration should have its own workspace. This is because Eclipse does store some config data in the Workspace folder.

I would also recommend specifying exactly which version of Java is used to run Eclipse (the JVM). If you don't, the current default version will be used, which is often the last one you installed. If you decided to install an older version, this can get annoying. So I always tell Eclipse exactly which version to use, leaving no surprises. The version of Java used to run Eclipse, is separate from the version used to run any of your projects, so there is no real reason not to use the latest (Java 8).

1. Go to the [Eclipse Downloads Page](http://www.eclipse.org/downloads/)
2. Download the **Eclipse IDE for Java EE Developers** not the basic **Eclipse IDE for Java Developers**, grabbing the 64bit or 32bit as appropriate. At the time of writing the latest build is **Luna** and the 64bit file is `eclipse-jee-luna-SR2-win32-x86_64.zip`
3. Decide where you want to install Eclipse. The Eclipse install is nothing more than unzipping the zip file. The default location would usually be `C:\Program Files\eclipse`. If you simply extract the zip file, it will create the top level `eclipse` directory.
4. Create a shortcut for Eclipse. Copy the `eclipse.exe` file from your Eclipse directory and paste it as a shortcut onto your desktop.
5. Specify a directory to store your Eclipse configuration. By default Windows will store this in `C:\User\Username\.eclipse` but you should override this if you want to use multiple configurations.
6. Create a new folder. I recommend storing it at the same level as your workspace folder but not in your workspace folder. So my workspace folder is `C:\Data\Workspace` and my config directory is `C:\Data\.eclipse`. Windows will not let you begin a folder name with a period, so create the folder and drop to the command line and rename it.
7. Right click your Eclipse shortcut, click **Properties** and edit the target line. Add the following `-configuration F:\Data\.eclipse` where the path matches the directory you just created. If the path contains spaces you can surround the path in quotes.
8. Specify the Eclipse JVM.
9. Open the Eclipse directory (usually `C:\Program Files\eclipse`) and edit the `eclipse.ini` file. Add the lines below, where the path matches your newest Java release.

```INI
-vm
C:\Program Files\Java\jdk1.8.0_40\bin\javaw.exe
```

You can now start Eclipse.

Install Eclipse Gradle Plugins
-------------------------------

1. From within Eclipse, select Help, and Install New Software...
2. Click the **Add** button
3. For name enter **Gradle** and for location enter `http://dist.springsource.com/release/TOOLS/gradle` then click **OK**
4. From the **Working with:** field select the Gradle address you have just added.
5. Eclipse should now search the address for installable modules. Tick all options, the **Extensions / Gradle Integrations** and the **Uncategorised** option.
6. Click **Next** and the components should start installing (this takes a while). Then click **Next**, accept the licence and **Finish** and Eclipse will do the final install.


Getting Maptool from GitHub
---------------------------

1. Go to [GitHub](https://github.com)
2. Register yourself, then sign in.
3. In the **Search GitHub** field type **maptool** and enter. This will bring up the a few repositories, select **RPTools/maptool** which is Craig's master repository.
4. Click the **Fork** button to make your own copy of the project.
5. Download and install **GitHub for Windows** from https://windows.github.com/
6. Start **GitHub for Windows**
7. Configure Git for Windows with your GitHub username and password. Once your client can connect to GitHub you can make a local clone.
8. Click the + Icon, in the top left.
9. Click **Clone** and you should see your username and your **maptool** project, which you should select.
10. Click the **Clone** option towards the bottom of the screen, a directory dialog box should appear.
12. Select your Eclipse workspace directory. This will create a maptool project directory in your workspace.

Build the Maptool Gradle Project
--------------------------------

1. Open Eclipse.
2. Select File and Import
3. Select **Gradle Project** and click **Next**
4. Browse to your `\maptool` directory
5. Click the **Build Model** button. This should search the director and find the parent **Maptool** project and the two child projects, **launcher** and **maptool**
6. Select all projects (click the parent **Maptool** project)
7. Click the **Finish** button. This will start building your project in Eclipse. I had to click a few **Run In Background** boxes at this point, but it completed okay. You should now have a **launcher** and a **maptool** project in your Eclipse workspace.
8. Expand your **maptool** project. You should see **JRE System Library** as one of the project components. It should also say which version and if you have followed the steps exactly as described above, it will be **JRE System Library [jdk1.8.0_40]** where as we actually want a Java1.7 version.
9. To change this, select your **maptool** project, the select **Project** and **Properties** which should open the project properties window.
10. Select **Java Build Path** from the options on the left.
11. Click the **Libraries** tab, select the **JRE System Library** and then click **Edit**
12. Click the **Alternate JRE** option and then press the **Installed JREs...** button.
13. Click **Add**, **Standard VM** and **Next**. Then click the **Directory** button to navigate to your Java 1.7 directory. Usually `C:\Program Files\Java\jdk1.7.75`. Then click **Finish** and **Ok**
14. You should now be able to select **jdk1.7.0_75** as your project JRE and click **Finish** and close the properties window.
15. Now build the project by right clicking the **maptool** project, then **Gradle** and **Tasks Quick Launcher**
16. A small dislogue window should pop up, type **Build** into the Tasks field and press enter. The project should now build and you should see something like:

```
[sts] -----------------------------------------------------
[sts] Starting Gradle build for the following tasks: 
[sts]      Build
[sts] -----------------------------------------------------
Creating Release
:maptool:copyLibs UP-TO-DATE
:maptool:compileJava UP-TO-DATE
:maptool:processResources UP-TO-DATE
:maptool:classes UP-TO-DATE
:maptool:jar UP-TO-DATE
:maptool:assemble UP-TO-DATE
:maptool:compileTestJava UP-TO-DATE
:maptool:processTestResources UP-TO-DATE
:maptool:testClasses UP-TO-DATE
:maptool:test UP-TO-DATE
:maptool:check UP-TO-DATE
:maptool:build UP-TO-DATE

BUILD SUCCESSFUL

Total time: 2.434 secs
[sts] -----------------------------------------------------
[sts] Build finished succesfully!
[sts] Time taken: 0 min, 2 sec
[sts] -----------------------------------------------------
```

Run Maptool from within Eclipse
-------------------------------

1. Select your **maptool** project
2. Click **Run** and **Run Configurations...** which will open a dialogue window.
3. Select **Java Application** from the left and then create a new configuration by clicking the **new** button.
4. Make sure the project field says **maptool**
5. In the Main Class field enter `net.rptools.maptool.client.MapTool` or click the **Browse** button and search for **maptool**
6. Click **Apply**
7. Click **Run**, this should now launch Maptool 1.4

If you got through all that, congrats!