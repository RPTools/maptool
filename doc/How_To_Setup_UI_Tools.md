How To Setup User Interface (UI) Tools for MapTool
==================================================

You thought you were done?  Sorry!

There are two extra things you may wish to consider and both are related to managing UI elements within MapTool. The current edition of the tool uses **Abeille** to manage **Swing** elements within the design. Abeille is a WYSIWYG form designer for Swing objects and if you plan on modifying any of the UI forms or panels, you should definitely install the Abeille Designer.  (Unless you like editing XML configuration files by hand!)

As Swing is now deprecated within Java, the plan is to move those elements over to **JavaFX**, although this work is further down the line at the moment. If you want to get to grips with JavaFX, you should probably install the Eclipse Plugin **e(fx)clipse** and the **JavaFX Scene Builder**. Scene Builder is also a WYSIWYG form designer, but this time for JavaFX Objects.  (Don't get confused by old links for Scene Builder -- use the [latest download from Gluon](http://gluonhq.com/products/scene-builder/))


Install Abeille
---------------

1. Go to the [Abeille Downloads page](https://java.net/projects/abeille/downloads/directory/Abeille%20Form%20Designer%202.1.0%20M3) and download the latest binaries. This is a closed project so they are quite old. The binaries are JAR files so you can simply unzip the folder to anywhere on your PC, such as `C:\Program Files (x86)\abeille-2.1.0_M3`.
1. Launch the Designer by running the `designer.jar` file in the `abeille-2.1.0_M3` directory. If you have installed Java as described above, you can just double-click the file.
1. Once the Designer is running, you need to create a new project for the MapTool forms. Call this file `maptool.jfpr` and save it anywhere you like, BUT NOT in the Eclipse MapTool project directory, as you do not want this file to become part of the MapTool project -- it is only used by you locally.
1. Add the MapTool resource directory as a Source path for your Abeille Project.
    * On the Abeille Project Settings screen you should see a button in the Source Tab to **Add Path**. Click it.
    * Navigate to the resource directory.
        * For example, if your Eclipse workspace directory is `C:\Data\Workspace` and your top level MapTool project was called `C:\Workspace\maptool`, you should add the path `C:\Data\Workspace\maptool\maptool\src\main\resources`.
        * This is very important!  Otherwise, the Abeille Designer will save your local path names into the designer files rather than the relative paths that are needed for proper distribution.
    * Select the directory and click **OK**.


Install e(fx)clipse Plugin
--------------------------

1. From within Eclipse, select **Help**, and **Install New Software...**
1. Click the **Add** button.
1. For name, enter **e(fx)clipse** and for location enter `http://download.eclipse.org/efxclipse/updates-released/1.2.0/site/`.
1. Click **OK**.
1. From the **Working with:** field, select the URL you have just added.
1. Eclipse should now search the address for installable modules. Tick all options, the **e(fx)clipse - install** and the **e(fx)clipse - single components** options.
1. Click **Next** and the components should start downloading (this can take awhile).
1. Click **Next** and accept the licence.
1. Click **Finish** and Eclipse will do the final install.


Install JavaFX Scene Builder
----------------------------

The JavaFX Scene Builder application has been handed over to the open source community and is now maintained by Gluon with help from the community.  Be sure to grab the [latest download from Gluon](http://gluonhq.com/products/scene-builder/) that matches the version of Java you're planning to run MapTool with.

1. Download the proper installer for your system.
    * For Windows, this is either the 64-bit or 32-bit version; note that Java 9 only supports 64-bit systems.
    * For Linux, this is either an **rpm** or a **deb**.
    * For macOS, this is a **dmg** file.
    * For all systems, you can download the executable JAR as well.  This will execute when double-clicked using your platform's file explorer, or can be executed from the command line via `java -jar scenebuilder.jar` (the name of the JAR file will inlude a version number).
1. You should now be able to open FXML files from within Eclipse with Scene Builder.  (If you downloaded the executable JAR, you will need to open FXML files yourself; Eclipse uses your platform's file extension mechanism to determine which application to use and the executable JAR isn't registered.)
