# How To Setup Eclipse for MapTool

Before you install Eclipse you may wish to consider whether or not
you want to maintain separated Eclipse configurations and instances.
Why would you do that? Well, it's perfectly possible for different
IDE Plugins to disagree with each other, third party developers
cannot always be relied upon. So if you put a lot of work into
configuring an IDE for an important project, you may want a separate
IDE install for messing around with MapTool. ;) If you are only
installing Eclipse for MapTool, it doesn't matter. You can run
multiple configurations from one Eclipse install directory or
separate ones if you want. But each configuration should have its
own workspace. This is because Eclipse does store some configuration
data in the Workspace folder.

I would also recommend specifying exactly which version of Java is
used to run Eclipse (the JVM).  If you don't, the current default
version will be used, which is often the last one you installed.
If you decided to install an older version, this can get annoying.
So I always tell Eclipse exactly which version to use, leaving no
surprises. The version of Java used to run Eclipse, is separate
from the version used to run any of your projects, so there is no
real reason not to use the latest (Java 11 at this time).

Last, if you're already familiar with Eclipse and are browsing this page
looking to see what else you might need, fear not:  the default Eclipse
install is all that's required (plus the Gradle plugin if you want to
perform Gradle tasks from inside Eclipse instead of from the command
line; if so, see the section below, **Install Eclipse Gradle Plugins**).

If you're otherwise already setup and ready to go, just do this at the
command prompt:
```
git clone https://github.com/RPTools/maptool.git
cd maptool
```
and either `.\gradlew run` (Windows) or `./gradlew run` (everywhere
else).

## Install and Configure Eclipse

1.  Go to the [Eclipse Downloads Page](http://www.eclipse.org/downloads/packages/)
1.  Download the **Eclipse IDE for Enterprise Java Developers** or
    the more basic **Eclipse IDE for Java Developers** (the former is
    larger and comes with more plugins, but the latter comes with all
    plugins we need). At the time of writing the latest build is
    **2018-12**.
1.  Decide where you want to install Eclipse.
    *   On Windows, install Eclipse as you would any other application.
    *   On macOS, copy the application to the **Applications** directory.
    *   On Linux, unpacked the compressed tar file (like a zip file) by executing
        `tar -xf eclipse-jee-*.tar.gz`.
1.  Specify a directory to store your Eclipse configuration. By
    default, Windows will store this in `C:\User\`_username_`\.eclipse` but
    you should override this if you want to use multiple configurations.
    *   Create a new folder.  I recommend storing it at the same level
        as your workspace folder but not _in_ your workspace folder.
    *   My workspace folder is `C:\Data\Workspace` and my configuration directory
        is `C:\Data\.eclipse`.
    *   Windows will not let you begin a folder name with a period, so
        create the folder and use the command line and rename it.
1.  Right click your Eclipse shortcut, choose **Properties**, and edit
    the target line.  Add the following: `-configuration "C:\Data\.eclipse"`,
    where the path matches the directory you created in the previous step. 
    If the path contains any spaces, you _must_ surround the path in
    quotes.
1.  You may wish to specify the JRE used to execute Eclipse itself
    (such as when you have multiple JREs installed).  To do this:
    *   Open the Eclipse directory.
        *   On Windows, usually `C:\Program Files\eclipse`.
        *   On macOS, right-click the `.app` and choose **Show Package
            Contents**.  Now, navigate into `Contents/Eclipse` and you'll
            find the file.
    *   Edit the `eclipse.ini` file.
    *   Add the lines below (or if the `-vm` option already appears,
        replace the line following it), where the path on the second line
        matches your newest Java release.
        ```ini
        -vm
        C:\Program Files\Java\jdk1.8.0_40\bin\javaw.exe
        ```
1.  You can now start Eclipse and it will use the Java program you specified.

## Install Eclipse Gradle Plugins

1.  From within Eclipse, select **Help**, and **Install New Software...**
2.  Click the **Add** button
3.  For name, enter **Gradle**, and for location, enter
    `http://dist.springsource.com/release/TOOLS/gradle`, then click **OK**.
4.  From the **Working with:** field select the Gradle address you have just added.
5.  Eclipse should now search the address for installable modules. Tick all options, the
    **Extensions / Gradle Integrations** and the **Uncategorised** option.
6.  Click **Next** and the components should start installing (this takes a while).
7.  Then click **Next**, accept the license(s) and **Finish**, and
    Eclipse will do the final install.


## Getting MapTool from GitHub

1.  Go to [GitHub](https://github.com)
1.  Register yourself, then sign in.
1.  Click [this link](https://github.com/RPTools/maptool) to visit the
    official repository's main page.
1.  Click the **Fork** button (upper right) to make your own copy of the project.
    Make a note of the URL for this new repository.
1.  Return to your desktop and execute Eclipse.
1.  Continue to choose the default values until you get to the main workspace window with
    the **Project Explorer** in the left panel (might also be **Package Explorer**).
1.  From the Eclipse **File** menu, select **Import...**.
1.  Look for the **Git** folder.  Inside it, select **Projects from Git** and click **Next >**.
1.  Select **Clone URI** from the list and click **Next >**.
1.  Enter the URL of the project you created in Step 4, above.  You
    will need to enter authentication information in the bottom half of
    this dialog.  If you don't, you'll be able to download from GitHub, but
    you won't be able to push your changes back.  (Choosing to let Eclipse
    securely store your credentials is convenient, but is not appropriate
    when using a shared computer.)
1.  Now you can right-click on the project in the **Project Explorer**
    and use the Team** options to manage your GitHub repository from within
    Eclipse.
    *  Read up on how to use Git!  Here's a short summary:
    *  You will most likely use **Switch to > New branch** when you're going to work on
	something in MapTool.
    *  **Commit** your changes periodically (multiple times a day is common, but whenever
	you've made significant code changes is typical).
    *  When you're ready to contribute your code back to the RPTools team, **Push** to
	upload your changes to your repository.
    *  Now, visit the GitHub web site to issue a _pull request_ (usually abbreviated "PR").
	This packages up your changes and asks the RPTools team to review them for possible
	inclusion into the next build of MapTool.  It helps to reference the issue number your
	code is related to in your PR comments (just put a hash tag and the issue number;
	GitHub will convert it into a link).


## Build MapTool with Eclipse (via Gradle)

1.  Quit Eclipse.
1.  Configure Eclipse to know about your Gradle installation:
    *  Open a command prompt window and navigate to the git working directory.
    *  On Windows:  execute `.\gradlew cleanEclipse eclipse`
    *  On macOS and Linux:  execute `./gradlew cleanEclipse eclipse`
1.  Open Eclipse.
1.  Expand your MapTool project.  You should see **JRE System Library**
    as one of the project components.  It should also say which version;
    ensure it matches the Java you have installed and want to use to build
    MapTool (MapTool currently requires Java 10 because Java 11 has removed
    the JavaFX libraries).
1.  If you ever need to change this (perhaps because you installed a
    newer Java and want to try building MapTool with it):
    *  Right-click your **maptool** project.
    *  Select **Build Path > Configure Build Path...**.
    *  Click the **Libraries** tab and select the **JRE System Library**.  Click **Edit**.
    *  Click the **Alternate JRE** option and then press the **Installed JREs...** button.
    *  Click **Search...** and navigate to the top-level directory
       where your new/updated Java is installed.
    *  Click **Open** and Eclipse will search looking for any JREs
       that it doesn't already know about.
    *  Now, you can select the new JRE for Eclipse to use for the MapTool project.
    *  Click **Apply** and **Close** buttons until the dialogs are all closed.
1.  Build the project by right-clicking the **maptool** project, then **Gradle** and
    **Tasks Quick Launcher**
    *  Note that the gradle configuration file, `build.gradle`, has its
       own configuration that determines which JRE will be used.
1.  A small dialogue window should pop up. Type **Build** into the **Tasks** field and
    press Enter. The project should now build and you should see something like:

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

## Build MapTool with Eclipse (without Gradle)

1.  You should use the previous section, **Build MapTool with Eclipse
    (via Gradle)** at least once, since Gradle knows which library
    dependencies are needed and it downloads them automatically.
1.  Eclipse has its own build system.  By default, it incrementally
    builds all classes that are changed as you change them.  This means
    you can execute the application at any time using the **Run** menu.
1.  See the next section for how to execute the applciation that Eclipse
    has built.

## Run MapTool from within Eclipse

1.  Select your **maptool** project.
1.  Click **Run** on the menu, and then **Run Configurations...** which
    will open a dialogue window.
1.  Select **Java Application** on the left and create a new configuration
    by clicking the **New** button.
1.  Make sure the project field says **maptool**.
1.  In the **Main Class** field, enter `net.rptools.maptool.client.MapTool`
    or click the **Browse** button and search for **MapTool**.
1.  Click **Apply**.
1.  Click **Run** to launch Maptool!
1.  In the future, you can right-click the project and use the **Run**
    menu option, or use the various buttons in the Eclipse user interface.

If you got through all that, congratulations!
