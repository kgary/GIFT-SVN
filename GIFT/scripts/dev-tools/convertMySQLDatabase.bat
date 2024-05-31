@ECHO OFF
REM The purpose of this script is to convert the UMS and LMS MySQL databases into UMS and LMS Derby databases

if not exist "%~p0/../../external/ant/bin/ant" (
	echo Unable to find the Ant library.  Did you install/extract the GIFT third parties into the external folder?
	set ERRORLEVEL=1
	GOTO:END
)

REM set java home to static path to GIFT provided JDK
set JDK_64x_HOME=%~p0/../../external/openjdk-11.64x/
if exist "%JDK_64x_HOME%" (
	echo 64x JDK folder exist
	set JAVA_HOME=%JDK_64x_HOME%
)

IF "%JAVA_HOME%"=="" (
	echo JAVA_HOME environment variable has not been set.
	set ERRORLEVEL=1
	GOTO:END
)

set outputDir="%~p0/../../output/dbTemp"

set mySqlUserName=root
set mySqlPassword=password

REM If you are using this script, be sure to set username, password, and MySQL URL to the correct values
call "%~p0/../../external/ant/bin/ant" -f "%~p0/../database/dbExport/database-convert.xml" -DmySqlUrl="jdbc:mysql://localhost:3306" -Dusername="%mySqlUserName%" -Dpassword="%mySqlPassword%" -Doutput.dir="%outputDir%"

REM Debugging 
REM SET /P ANSWER=Keeping window open for debug purposes

:END
if %ERRORLEVEL% GTR 0 pause