How To Setup User Interface (UI) Tools for MapTool
==================================================

You thought you were done? Sorry, there are two extra things you may wish to consider and both are related to managing UI elements within Maptool. The current edition of the tool uses **Albeille** to manage **Swing** elements within the design. Albeille is a WYSIWYG form designer for Swing objects and if you plan on modifying any of the UI elements you should definitely install the Albeille Designer. 

As Swing is now depreciated within Java the plan is to move those elements over to **JavaFX**, although this is work is further down the line at the moment. If you want to get to grips with JavaFX, you should probably install the Eclipse Plugin **e(fx)clipse** and the **JavaFX Scene Builder**. Scene Builder is also a WYSIWYG form designer, but this time for JavaFX Objects.

Install Albeille
----------------

1. Go to the [Albeille Downloads page](https://java.net/projects/abeille/downloads/directory/Abeille%20Form%20Designer%202.1.0%20M3) and download the latest binaries. This is a close project so they are quite old. The binaries are jar files so you can simply unzip the folder to anywhere on your PC, such as `C:\Program Files (x86)\abeille-2.1.0_M3`
2. Launch the Designer by running the `designer.jar` file in the `abeille-2.1.0_M3` directory. If you have installed Java as described above, you can just right click the file and select **Open**
3. Once the Designer is running, you need to create a new project for the Maptool forms. Call this file `maptool.jfpr` and save it anywhere you like, BUT NOT in the Eclipse Maptool project directory, as you do not want this file to become part of the Maptool project and is only used by you locally.
4. Add the Maptool resource directory as a Source path for your Abeille Project. On the Abeille Project Settings screen you should see a button in the Source Tab to **Add Path**. Click this and navigate to the resource directory. For example, if your Eclipse workspace directory was `C:\Workspace` and your top level Maptool project was called `C:\Workspace\maptool` you should add the path `C:\Workspace\maptool\maptool\src\main\resources`. This is very important, otherwise the Albeille Designer will save the local path names into the designer files rather than the relative paths that are needed for proper distribution.

Install e(fx)clipse Plugin
--------------------------

1. From within Eclipse, select Help, and Install New Software...
2. Click the **Add** button
3. For name enter **e(fx)clipse** and for location enter http://download.eclipse.org/efxclipse/updates-released/1.2.0/site/ then click OK
4. From the **Working with:** field select the e(fx)clipse address you have just added.
5. Eclipse should now search the address for installable modules. Tick all options, the **e(fx)clipse - install** and the **e(fx)clipse - single components** option.
6. Click **Next** and the components should start installing (this takes a while). Then click **Next**, accept the licence and **Finish** and Eclipse will do the final install.

Install JavaFX Scene Builder
----------------------------

The JavaFX Scene Builder application has been handed over to the open source community and as a consequence it can be hard to find the Scene Builder install. The last Oracle version can be found on the [JavaFX Scene Builder Archive](http://www.oracle.com/technetwork/java/javafxscenebuilder-1x-archive-2199384.html) page. 

1. Download the **Windows 32/64 bit (msi)** from the **JavaFX Scene Builder 2.0 Related Downloads** section found towards the bottom of the page.
2. Execute the msi file as normal. This will install the application and make a file association to the FXML extension.
3. You should now be able to open FXML files from within Eclipse with Scene Builder.
