How To Setup Eclipse for MapTool
================================

Before you install Eclipse you may wish to consider whether or not you want to maintain separated Eclipse configurations and instances. Why would you do that? Well, it's perfectly possible for different IDE plugins to disagree with each other. So if you put a lot of work into configuring an IDE for an important project, you may want a separate IDE install for messing around with MapTool ;) If you are only installing Eclipse for MapTool, it doesn't matter. You can run multiple configurations from one Eclipse install directory or separate ones if you want. But each configuration should have its own workspace. This is because Eclipse does store some configuration data in the workspace folder.

I would also recommend specifying exactly which version of Java is used to run Eclipse (the JVM). If you don't, the current default version will be used, which is often the last one you installed. If you decided to install an older version, this can get annoying. So I always tell Eclipse exactly which version to use, leaving no surprises. The version of Java used to run Eclipse is separate from the version used to run any of your projects, so there is no real reason not to use the latest for your MapTool IDE.

1. Go to the [Eclipse Downloads Page](http://www.eclipse.org/downloads/eclipse-packages/)
1. Download the **Eclipse IDE for Java EE Developers** not the basic **Eclipse IDE for Java Developers** (grab the 64-bit or 32-bit as appropriate; the web page should determine that for you and emphasize the correct one). At the time of writing the latest build is **Oxygen**.
1. Specify a directory to store your Eclipse configuration.  You can choose the default unless you want to install multiple versions of Eclipse and keep them separate.
1. Decide where you want to install Eclipse.  It can be installed in any directory, but each operating system has its own set of preferences:
    * for Windows, it is typically `C:\Program Files\eclipse`
    * for Linux, it is often `/opt/eclipse`
    * for macOS, it is `/Applications`
1. Create a shortcut for Eclipse, if you wish.
    * for Linux and macOS, copy the `eclipse` script in the install directory to your desktop (typically `~/Desktop`)
    * for Windows:
        1. Copy the `eclipse.exe` file from your Eclipse directory and paste it as a shortcut onto your desktop,
        1. Create a new top-level folder to hold your Eclipse configuration (previous step) and your Workspace (where projects will be stored by default).  For example, use `C:\Data` and then put your Eclipse configuration in that directory as well as your Workspace directory.  So my config directory is `C:\Data\.eclipse` and my workspace folder is `C:\Data\Workspace`.  (Windows will not let you begin a folder name with a period, so create the folder under some other name and then drop to the command line to rename it.)
        1. Right-click your Eclipse shortcut, click **Properties** and edit the **Target** line. Add `-configuration C:\Data\.eclipse` where the path matches the directory you created in the last step. If the path contains spaces, you can surround it in double quotes.
1. Specify the Eclipse JVM (in the examples below, replace `JAVA_HOME` with wherever you installed Java).
    1. Determine where your Java executable is stored.
        * for Windows, this is `JAVA_HOME\bin\javaw.exe`
        * for Linux and macOS, this is `JAVA_HOME\bin\java`
    1. Open the Eclipse directory (such as `C:\Program Files\eclipse` on Windows or `/Applications/Eclipse.app/Contents/Eclipse/` on macOS) and edit the `eclipse.ini` file. Add the lines below, where the path matches your newest Java release.  (If there's already a line containing `-vm`, just replace the following line.)
        ```INI
        -vm
        JAVA_HOME\bin\javaw.exe
        ```
        1. For example, the file might contain this on Windows:
            ```INI
            -vm
            C:\Program Files\Java\jdk1.8.0_121\bin\javaw.exe
            ```
        1. On Unix systems, you can choose the `java` that is in your `$PATH` (as shown in the Linux example) or you can use an absolute path (as shown in the macOS example).  The former wouldn't require updating when you upgrade your Java version, but the latter _never_ changes when you upgrade Java meaning that you _know_ which version is used to run Eclipse.
           1. On Linux (you can use `update-alternatives` to find all versions):
            ```INI
            -vm
            /usr/bin/java
            ```
           1. On macOS (all JREs can be found by executing `/usr/libexec/java_home -V`, just be sure you pick one with `jdk` in the name!):
            ```INI
            -vm
            /Library/Java/JavaVirtualMachines/jdk-9.0.1.jdk/Contents/Home/bin/java
            ```

You can now start Eclipse.


Install Eclipse Gradle Plugins
-------------------------------

1. From within Eclipse, select **Help**, and **Install New Software...**
1. Click the **Add** button
1. For name, enter **Gradle** and for location enter `http://dist.springsource.com/release/TOOLS/gradle`
1. Click **OK**
1. From the **Working with:** field, select the Gradle address you have just added.
1. Eclipse should now search the address for installable modules. Tick all options, the **Extensions / Gradle Integrations** and the **Uncategorised** option.
1. Click **Next** and the components should start downloading (this can take a while).
1. Click **Next**, accept the licence
1. Click **Finish** and Eclipse will do the final install.


Getting MapTool from GitHub
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

Build the MapTool Gradle Project
--------------------------------

1. Open Eclipse.
2. Select File and Import
3. Select **Gradle Project** and click **Next**
4. Browse to your `\maptool` directory
5. Click the **Build Model** button. This should search the directory and find the parent **MapTool** project and the two child projects, **launcher** and **maptool**
6. Select all projects (click the parent **MapTool** project)
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

Run MapTool from within Eclipse
-------------------------------

1. Select your **maptool** project
2. Click **Run** and **Run Configurations...** which will open a dialogue window.
3. Select **Java Application** from the left and then create a new configuration by clicking the **new** button.
4. Make sure the project field says **maptool**
5. In the Main Class field enter `net.rptools.maptool.client.MapTool` or click the **Browse** button and search for **maptool**
6. Click **Apply**
7. Click **Run**, this should now launch MapTool 1.4

If you got through all that, congrats!
