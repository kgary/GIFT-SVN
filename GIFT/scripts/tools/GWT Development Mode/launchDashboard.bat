@ECHO OFF

REM This script file executes the ERT tool in GWT development mode using Ant.
REM This is done by simply executing the "DevelopmentMode" target within the tool's build.xml file.
REM
REM Development Mode can be used to debug the GWT client code after it has been built and converted to Javascript.

set "BASE=%~dp0..\..\.."

if not exist "%BASE%\external\ant\bin\ant" (
	echo Unable to find the Ant library. Did you install/extract the GIFT third parties into the external folder?
	set ERRORLEVEL=1
	GOTO:END
)

set ANT="%BASE%\external\ant\bin\ant"

REM Pathname is the location of build.xml
set Pathname="%BASE%\src\mil\arl\gift\tools\dashboard"

REM Set Java Home to the path of GIFT-provided JDK
set JDK_64x_HOME=%BASE%\external\jdk-11\
if exist "%JDK_64x_HOME%" (
	echo 64x JDK folder exist
	set JAVA_HOME=%JDK_64x_HOME%
)

REM Check for the Java library to build with
IF "!JAVA_HOME!" == "" (
	echo JAVA_HOME environment variable has not been set.
	set ERRORLEVEL=1
	GOTO:END
)

REM Set specific properties related to dev mode. This mainly involves changing the target WAR directory of the running
REM GAS Jetty server to point to the uncompressed WAR folder in build/war rather than the .war file that the server normally
REM runs with. This is needed to replace some files within the WAR to support automatic recompiling with dev mode.
set ANT_OPTS=-DDevModeArgs="-Dtarget.war=/build/war/dashboard"

cd "%BASE%/scripts/util"

REM Launch the GAS normally, albeit with the above dev mode argumens.
start cmd /c %ANT% -file launchProcess.xml start gas

cd %Pathname%

REM Launch the GWT dev mode process.
call %ANT% DevelopmentMode

:END
IF NOT %ERRORLEVEL% == 0 (
	REM There was an error
	pause
)
