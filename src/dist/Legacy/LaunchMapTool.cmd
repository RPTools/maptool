@ECHO OFF
SETLOCAL

REM *******************************************************************
REM **        MapTool Launcher Script for Windows v2013-04-05        **
REM *******************************************************************
REM ** This MapTool launcher script checks the current version of    **
REM ** java to make sure that it matches what is needed for the      **
REM ** current version of MapTool.  It takes a single argument which **
REM ** can be the path to the version of Java that you want to use,  **
REM ** although this argument is not required.                       **
REM **                                                               **
REM ** If the path is not specified on the command line, the script  **
REM ** will check standard locations based on your system and try to **
REM ** find the correct version of java.                             **
REM **                                                               **
REM ** Original script in this thread:                               **
REM **        http://forums.rptools.net/viewtopic.php?f=3&t=21856    **
REM *******************************************************************

REM ===================================
REM === YOU MAY EDIT THESE SETTINGS ===
REM ===================================

REM **NOTE ON EDITING**
REM   The items below set the defaults for MapTool.  If a mt.cfg
REM   file is found (usually from having used the launcher) then
REM   those settings will override these settings.
REM   TLDR:  Set the variables in the mt.cfg file instead of here or delete the mt.cfg file.

REM Path to the JRE folder for the correct version of Maptool:
REM (Note that this is different from the mt.cfg file format, but still gets overwritten if mt.cfg exists)
SET MAPTOOL_JAVA_PATH=C:\Program Files\Java\jre6

REM Maptool memory settings (see docs for explanation)
REM Defaults are:
REM       Max Memory: 1024
REM       Min Memory: 64
REM       Stacksize:  3
SET MAPTOOL_MAX_MEMORY=1024
SET MAPTOOL_MIN_MEMORY=64
SET MAPTOOL_STACKSIZE=3

REM =====================================
REM ==== DO NOT EDIT BELOW THIS LINE ====
REM =====================================

REM Set the compatible Java version
SET MAPTOOL_JAVA=1.6
SET MAPTOOL_JAVA_URL=http://javadl.sun.com/webapps/download/AutoDL?BundleId=73922
SET MAPTOOL_JAVA_URL_X64=http://javadl.sun.com/webapps/download/AutoDL?BundleId=73923

REM Debug mode is useful for seeing what is happening, normally FALSE
SET MAPTOOL_LAUNCHER_DEBUG=FALSE

REM Check to see if the mt.cfg file exists and use those variables instead of the ones in this script
:CheckMTcfg
SET FAILJUMP=CheckArgument
IF NOT EXIST "%~dp0mt.cfg" GOTO %FAILJUMP%
IF %MAPTOOL_LAUNCHER_DEBUG%==TRUE ECHO DEBUG:  Reading variables from "%~dp0mt.cfg"
FOR /f "usebackq delims=^= tokens=1,2" %%a IN ("%~dp0mt.cfg") DO (
   IF /i %%a==MAXMEM SET MAPTOOL_MAX_MEMORY=%%b
   IF /i %%a==MINMEM SET MAPTOOL_MIN_MEMORY=%%b
   IF /i %%a==STACKSIZE SET MAPTOOL_STACKSIZE=%%b
   IF /i %%a==JVM SET MAPTOOL_JAVA_PATH=%%b
)
REM Remove quotes if there are any.
SET MAPTOOL_JAVA_PATH=%MAPTOOL_JAVA_PATH:"=%
IF %MAPTOOL_LAUNCHER_DEBUG%==TRUE ECHO DEBUG:  Variables have been updated from mt.cfg they are as follows:
IF %MAPTOOL_LAUNCHER_DEBUG%==TRUE ECHO DEBUG:         MAPTOOL_MAX_MEMORY = %MAPTOOL_MAX_MEMORY%
IF %MAPTOOL_LAUNCHER_DEBUG%==TRUE ECHO DEBUG:         MAPTOOL_MIN_MEMORY = %MAPTOOL_MIN_MEMORY%
IF %MAPTOOL_LAUNCHER_DEBUG%==TRUE ECHO DEBUG:         MAPTOOL_STACKSIZE  = %MAPTOOL_STACKSIZE%
IF %MAPTOOL_LAUNCHER_DEBUG%==TRUE ECHO DEBUG:         MAPTOOL_JAVA_PATH  = "%MAPTOOL_JAVA_PATH%"
REM The mt.cfg file will likely contain the full path to javaw.exe, but we need to check java.exe
REM to find the version, so we'll remove the path and tack back on the exes.
IF /i %MAPTOOL_JAVA_PATH:~-9%==javaw.exe (
   SET JAVA_PATH=%MAPTOOL_JAVA_PATH:~0,-9%java.exe
   SET JAVAW_PATH=%MAPTOOL_JAVA_PATH:~0,-9%javaw.exe
) ELSE (
   IF /i %MAPTOOL_JAVA_PATH:~-5%==javaw (
      SET JAVA_PATH=%MAPTOOL_JAVA_PATH:~0,-5%java.exe
      SET JAVAW_PATH=%MAPTOOL_JAVA_PATH:~0,-5%javaw.exe
   ) ELSE (
      REM The name wasn't in a format we expected, so give up.
      SET JAVA_PATH=CouldNotParse
   )
)
IF %MAPTOOL_LAUNCHER_DEBUG%==TRUE ECHO DEBUG:  After reading mt.cfg, the path to Java = "%JAVA_PATH%"
IF NOT EXIST "%JAVA_PATH%" GOTO %FAILJUMP%
GOTO CheckJava
 
REM Check to see if the needed version of Java was passed in calling the script
:CheckArgument
SET FAILJUMP=CheckHardcoded
IF NOT EXIST "%1" GOTO %FAILJUMP%
IF %MAPTOOL_LAUNCHER_DEBUG%==TRUE ECHO DEBUG:  Checking path passed as argument -- %1
SET JAVA_PATH=%~1\bin\java.exe
SET JAVAW_PATH=%~1\bin\javaw.exe
IF NOT EXIST "%JAVA_PATH%" GOTO %FAILJUMP%
GOTO CheckJava

REM Check to see if the needed version of Java is specified in the SET command above
:CheckHardcoded
SET FAILJUMP=CheckPath
IF NOT EXIST "%MAPTOOL_JAVA_PATH%" GOTO %FAILJUMP%
IF %MAPTOOL_LAUNCHER_DEBUG%==TRUE ECHO DEBUG:  Checking path in script settings -- %MAPTOOL_JAVA_PATH%
SET JAVA_PATH=%MAPTOOL_JAVA_PATH%\bin\java.exe
SET JAVAW_PATH=%MAPTOOL_JAVA_PATH%\bin\javaw.exe
IF NOT EXIST "%JAVA_PATH%" GOTO %FAILJUMP%
GOTO CheckJava

REM Check the version of Java that is in our path.
:CheckPath
IF %MAPTOOL_LAUNCHER_DEBUG%==TRUE ECHO DEBUG:  Checking Java version in path
SET JAVA_PATH=java
SET JAVAW_PATH=javaw
SET FAILJUMP=GetJava
GOTO CheckJava

:CheckJava
FOR /f "tokens=3" %%i IN ('"%JAVA_PATH%" -version 2^>^&1 ^| FINDSTR /i version') DO (
    SET JAVA_VERSION=%%i
)
SET JAVA_VERSION=%JAVA_VERSION:"=%
IF %MAPTOOL_LAUNCHER_DEBUG%==TRUE ECHO DEBUG:  Java Version -- %JAVA_VERSION%

FOR /f "delims=. tokens=1-3" %%j IN ("%JAVA_VERSION%") DO (
    SET JAVA_MAJOR=%%j
    SET JAVA_MINOR=%%k
    SET JAVA_BUILD=%%l
)
IF %MAPTOOL_LAUNCHER_DEBUG%==TRUE ECHO DEBUG:  Java Version ^(Major^) -- %JAVA_MAJOR%
IF %MAPTOOL_LAUNCHER_DEBUG%==TRUE ECHO DEBUG:  Java Version ^(Minor^) -- %JAVA_MINOR%
IF %MAPTOOL_LAUNCHER_DEBUG%==TRUE ECHO DEBUG:  Java Version ^(Build^) -- %JAVA_BUILD%
IF %MAPTOOL_LAUNCHER_DEBUG%==TRUE ECHO DEBUG:  Checking to see if Java versions match:  %MAPTOOL_JAVA%==%JAVA_MAJOR%.%JAVA_MINOR%
IF %MAPTOOL_JAVA%==%JAVA_MAJOR%.%JAVA_MINOR% GOTO LaunchMapTool
GOTO %FAILJUMP%

:LaunchMapTool
IF %MAPTOOL_LAUNCHER_DEBUG%==TRUE ECHO DEBUG:  Launching Maptool
FOR %%m in ("%~dp0maptool-*.jar") DO SET MAPTOOL_JAR_FILE=%%m
IF %MAPTOOL_LAUNCHER_DEBUG%==TRUE ECHO DEBUG:  Executing command -- START "MapTool" "%JAVAW_PATH%" -Xms%MAPTOOL_MIN_MEMORY%M -Xmx%MAPTOOL_MAX_MEMORY%M -Xss%MAPTOOL_STACKSIZE%M -jar "%MAPTOOL_JAR_FILE%" -Dfile.encoding=UTF8 -DMAPTOOL_DATADIR="%CD%" run
START "MapTool" "%JAVAW_PATH%" -Xms%MAPTOOL_MIN_MEMORY%M -Xmx%MAPTOOL_MAX_MEMORY%M -Xss%MAPTOOL_STACKSIZE%M -jar "%MAPTOOL_JAR_FILE%" -Dfile.encoding=UTF8 -DMAPTOOL_DATADIR="%CD%" run
GOTO EndScript

:GetJava
ECHO You are currently running Java version %JAVA_MAJOR%.%JAVA_MINOR%, but MapTool requires Java
ECHO version %MAPTOOL_JAVA%.  If you already have the correct Java version installed
ECHO then you can tell the launcher in one of three ways:
ECHO     1.)  Edit your mt.cfg file and set the path there:
ECHO               Example:  JVM=C:\Program Files\Java\jre7\bin\javaw.exe
ECHO     2.)  Edit %0 and specify MAPTOOL_JAVA_PATH at the top.
ECHO               Example:  SET MAPTOOL_JAVA_PATH=C:\Program Files\Java\jre7
ECHO               **Note**  This only works if you do not have a mt.cfg file
ECHO     3.)  Run %0 with the path specified as an argument.
ECHO               Example:  %0 "C:\Program Files\Java\jre7"
ECHO.
ECHO If you do not have the correct version installed, this script can launch your
ECHO default internet browser to download it.
CHOICE /T 60 /C YN /D Y /M "Do you want to be taken to the download location for the correct version of Java"
IF ERRORLEVEL==2 GOTO EndScript
IF EXIST "%ProgramFiles(x86)%" (
   IF %MAPTOOL_LAUNCHER_DEBUG%==TRUE ECHO DEBUG:  Launching URL for 64-bit -- %MAPTOOL_JAVA_URL_X64%
   START %MAPTOOL_JAVA_URL_X64%
) ELSE (
   IF %MAPTOOL_LAUNCHER_DEBUG%==TRUE ECHO DEBUG:  Launching URL for 32-bit -- %MAPTOOL_JAVA_URL%
   START %MAPTOOL_JAVA_URL%
)

:EndScript
IF %MAPTOOL_LAUNCHER_DEBUG%==TRUE PAUSE
ENDLOCAL
