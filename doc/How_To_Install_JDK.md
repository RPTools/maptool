How to Install Java Development Kit (JDK)
======================= 

You will need the *Java software development kit (SDK)*, if you want to do any coding of RPTools applications. You may also want to install multiple versions of Java, for compatibility testing (but note that as of the 1.4.1.9 development build, MapTool requires Java 9+).  You may have multiple versions of the *Java runtime environment (JRE)* installed for running applications, but the JDK contains the compiler and other language tools that are needed to build the application from source code.

**Note:** You need to know if your computer is 64-bit or 32-bit (only 64-bit systems can use Java 9 and later):

* For Windows:
    1. Open **Windows Explorer**.
    1. Right-click on **Computer**.
    1. Select **Properties**.
    1. Look in the *System* section against the *System Type* for either `32-bit` or `64-bit` when describing the operating system.

* For Linux:
    1. Open a command prompt window (typically by right-clicking on the desktop and choosing **Open in Terminal**).
    1. Run the command `uname -m`.
    1. If the output says `x86_64`, you have a 64-bit machine; otherwise, you have a 32-bit system.

* For macOS:
    1. Click on the Apple logo in the top-left of the menu bar.
    1. Select **About This Mac**.
    1. If the *Version* displayed is `10.10` or greater, you have a 64-bit machine; otherwise, you have a 32-bit system.

Install latest Java Standard Edition Development Kit
----------------------------------------------------

1. Visit the [Oracle download page](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
1. Along the right side of the page are **Download** buttons directly below the text "**JDK**" for each version.  Click the button for the version you want (currently, there are sections for Java 7, Java 8, and Java 9).
1. Click the **Accept License Agreement** radio button.
1. Click the link for your operating system to start the download.
    * Linux users can choose between downloading an **rpm** installer file (for Red Hat-based versions) or just a plain **tar** file (for all other Linux versions).
1. Unpack the installer file you have just downloaded.  You may need to disable antivirus software.
    * On Windows, double-click on the downloaded file to execute the installer.
    * On Linux, if you downloaded the **rpm** file, double-click it.
    * On Linux, if you downloaded the **tar** file, you will need to do a web search for instructions for your version of Linux.
    * On macOS, double-click the **dmg** file to open it, then double-click the package file in the window that opens.  (Don't forget to right-click the disk image on your desktop and choose **eject** when the installer is done.)
