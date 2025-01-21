How To Setup User Interface (UI) Tools for MapTool
==================================================

You thought you were done? Sorry, there are two extra things you may wish to consider and both are related to managing UI elements within Maptool. The current edition of the tool uses **IntelliJ** to manage **Swing** elements within the design. Just open the `.form` files and install the plugin when prompted.

As Swing is now deprecated within Java the plan is to move those elements over to **JavaFX**, although this work is further down the line at the moment. If you want to get to grips with JavaFX, you should probably install the Eclipse Plugin **e(fx)clipse** and the **JavaFX Scene Builder**. Scene Builder is also a WYSIWYG form designer, but this time for JavaFX Objects.

Install e(fx)clipse Plugin
--------------------------

1. From within Eclipse, select Help, and Install New Software...
2. Click the **Add** button
3. For name enter **e(fx)clipse** and for location enter `http://download.eclipse.org/efxclipse/updates-released/1.2.0/site/` then click OK
4. From the **Working with:** field select the e(fx)clipse address you have just added.
5. Eclipse should now search the address for installable modules. Tick all options, the **e(fx)clipse - install** and the **e(fx)clipse - single components** option.
6. Click **Next** and the components should start installing (this takes a while). Then click **Next**, accept the license and **Finish** and Eclipse will do the final install.

Install JavaFX Scene Builder
----------------------------

The JavaFX Scene Builder application has been handed over to the open source community and as a consequence it can be hard to find the Scene Builder install. The latest version as of this writing is SceneBuilder 10.0.0 and is available from [GluonHQ.com](https://gluonhq.com/products/scene-builder/)

Download and install SceneBuilder.  It can now be used to open **.fxml** files which are the JavaFX equivalent to `.form` files (above).
