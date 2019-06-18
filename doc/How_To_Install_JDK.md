How to Install Java Development Kit (JDK)
=======================

You will need to install a Java Development Kit in order to build MapTool from source.  At the time of writing, MapTool 1.5 is built against Java JDK 10. Neither older nor newer Java versions will work.  Only 64-bit builds are supported.

Install Java SE Development Kit 10
----------------------------------------

1. Download the current Java 10 JDK from the Oracle Java Archives page.
   https://www.oracle.com/technetwork/java/javase/downloads/java-archive-javase10-4425482.html
   You will need an Oracle account to download.
2. Click the "Accept License Agreement" radio button for the JDK.
3. Download the Java SE Development Kit. At the time of writing the file is `jdk-10.0.2_windows-x64_bin.exe`
4. Run the installer file you have just downloaded. This will try and install to a directory like: `C:\Program Files\Java\jdk1.7.75`, which is fine. If the directory says JRE instead of JDK then you have downloaded the wrong file. Don't worry if you have both, the JDK contains a JRE.
5. Check that running `java -version` from a command line shows Java 10.  Also check your command path to verify that the Java 10 JDK is the only one in your path.
