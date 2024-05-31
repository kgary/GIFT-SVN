@ECHO OFF
REM
REM This script is used to move domain session message log files referenced by GIFT experiment records and LTI records in the 
REM UMS database to a new folder for each domain session.  This will also update those file paths in the database.
REM It will also create the GIFT\output\domainSessions folder if it doesn't exist.
REM
REM The tables affected include:
REM experimentsubject
REM datacollectionresultslti 
REM

ECHO This script will move domain session message log files referenced by GIFT experiments to GIFT/output/domainSessions/.
ECHO.
ECHO Pre-requisites:
ECHO .... 1) GIFT is built
ECHO .... 2) UMS database has been extracted and exists
ECHO .... 3) the third parties are in the GIFT/external directory
ECHO .... 4) Derby database server is running (check with "netstat -aon | find "1527" " command)
ECHO ....    To manually start the server: GIFT\external\db-derby-10-15.2.0-bin\bin\startNetworkServer.bat
ECHO.

set BaseDir=%~dp0..\..
set ExternalLibsDir=%BaseDir%\external
set DERBY_HOME=%ExternalLibsDir%\db-derby-10.15.2.0-bin
set DataDir=%BaseDir%\data
set BinDir=%BaseDir%\bin

SET "EXTRACT_ZIP_VBS=%BaseDir%\scripts\install\extractZip.vbs"
set "JDK_64x_ZIP=%ExternalLibsDir%\openjdk-11.64x.GIFT.zip"
set "JDK_EXTRACT_TO=%ExternalLibsDir%\"
set JDK_64x_HOME=%ExternalLibsDir%\openjdk-11.64x

REM check for 64x JDK zip
if exist "%JDK_64x_ZIP%"  (
	echo 64x JDK zip exist
	set JDK64xZipExists=1
) else if exist "%JDK_64x_HOME%" (
	echo 64x JDK folder exist
	set JDK64xZipExists=1
) else (
	echo 64x JDK zip or folder DOES NOT exist
	set JDK64xZipExists=0
)

REM set JDK variables according to which JDK bit version to use
if %JDK64xZipExists% == 1 (
	REM prioritize 64x JDK
	set JDK_HOME=%JDK_64x_HOME%
	set JDK_ZIP=%JDK_64x_ZIP%
)


REM Check for jdk 8 - the installer will not launch unless it is there.
if not exist "%JDK_HOME%" (

	if not exist "%JDK_ZIP%" (
	
		echo ERROR: Unable to find "%JDK_ZIP%".
		echo Do you have the GIFT third parties in the %JDK_EXTRACT_TO% folder?
		
		set ERRORLEVEL=1
		GOTO END
	)
	
	cscript "%EXTRACT_ZIP_VBS%" "%JDK_ZIP%" "%JDK_EXTRACT_TO%"
				
	if %ERRORLEVEL% neq 0 (
		echo ERROR: There was a problem extracting %JDK_ZIP%.
		set ERRORLEVEL=1
		GOTO END
	)
	
	if not exist "%JDK_HOME%" (
		echo ERROR: There was a problem extracting %JDK_ZIP% because the folder %JDK_HOME% doesn't exist.
		GOTO END
	)
)

set Class="mil.arl.gift.ums.db.UMSDatabaseManager"
set ClasspathExtension="%BinDir%\gift-ums.jar;%BinDir%\gift-ums-db.jar;%ExternalLibsDir%\derby\derby.jar"

set JavaClasspath="%ExternalLibsDir%\slf4j\*;%ExternalLibsDir%\*;%ExternalLibsDir%\activemq\activemq-all-5.18.3.jar;%ExternalLibsDir%\hibernate\*;%ExternalLibsDir%\jsonsimple\json_simple-1.1.jar;%DERBY_HOME%\lib\derbyclient.jar;%BinDir%\gift-common.jar"

ECHO.
SET /P ANSWER=Would you like to continue? [y/n] 
echo You chose: %ANSWER%

if /i {%ANSWER%}=={y} (

	cd "%BaseDir%"
	"%JDK_HOME%\jdk-11\bin\java" -Dderby.system.home="%DataDir%" -classpath %JavaClasspath%;%ClasspathExtension% %Class% directLogFiles

)

ECHO.
pause