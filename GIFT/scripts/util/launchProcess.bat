@ECHO OFF

REM
REM This script file helps facilitate the launching of various GIFT processes such as modules or tools.
REM
REM absolute path to GIFT\
set AbsBaseDir=%~dp0..\..
REM relative path to GIFT\ from GIFT\scripts\util\
set BaseDir=..\..

set GIFT_ERROR_CODE=101

REM Check for close parenthesis in directory path to GIFT because they will cause
REM issues with the rest of this script (FYI. spaces in this path are already handled)
set "search=<*.*.*)"
setlocal enableDelayedExpansion

echo(!AbsBaseDir!|findstr /r /c:"!search!" >nul && (
	echo.
	echo ERROR: Please remove all "^)" in the GIFT path of '!AbsBaseDir!', otherwise the various GIFT scripts will not work.
	echo.
    REM have to endlocal here in order for this error level value to persist outside of this script
    endlocal
	set ERRORLEVEL=1
	GOTO:END
)

endlocal

REM Before you can run certain commands (e.g. 'findstr'), 
REM the system32 folder must be on your Path environment variable
REM echo %PATH%
call "%AbsBaseDir%\scripts\install\system32PathCheck.bat"
REM echo %PATH%

REM Check to make sure running in Windows

ver | findstr "Windows" > nul
if %ERRORLEVEL% NEQ 0 (
	echo Unable to determine if you are running on a Windows machine.  GIFT only supports the Windows OS.
	set ERRORLEVEL=1
	GOTO:END
)

REM
REM Make sure the Windows configuration is correct
REM	- Check the .vbs file association
call "%AbsBaseDir%\scripts\install\vbsFileAssocCheck.bat"

set GIFT_TEMP=temp
REM Note: The absolute path to external folder
set AbsExternalLibsDir=%AbsBaseDir%\external
REM Note: The relative path to external folder is relative to GIFT/
set ExternalLibsDir=external
SET "EXTRACT_ZIP_VBS=%AbsBaseDir%\scripts\install\extractZip.vbs"
set "JDK_64x_ZIP=%AbsExternalLibsDir%\openjdk-11.64x.GIFT.zip"
set "JDK_EXTRACT_TO=%AbsExternalLibsDir%\"
set JDK_64x_HOME=%AbsExternalLibsDir%\jdk-11

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
		set ERRORLEVEL=1
		GOTO END
	)
)

REM set java home to static path to GIFT provided JDK
setlocal enabledelayedexpansion
set JAVA_HOME=%JDK_HOME%
echo using %JAVA_HOME%

REM Check for the Java library to build with
IF "!JAVA_HOME!" == "" (
	echo JAVA_HOME environment variable has not been set.
	set ERRORLEVEL=1
	GOTO:END
)

IF NOT EXIST "!JAVA_HOME!" (
	echo The Java specified by JAVA_HOME environment variable value of "%JAVA_HOME%" does not exist.  
	echo Is the path correct? If not, please correct it.  
	echo Refer to the GIFT installation instructions for additional JAVA_HOME help.
	set ERRORLEVEL=1
	GOTO:END
)

REM Use an Ant configuration file to parse the launch arguments, configure any needed classpaths or 
REM other settings, and then execute the appropriate Java processes. Doing this through Ant instead of
REM doing it in this script allows the bulk of GIFT's launching logic to be agnostic of the OS platform
REM
REM Note: Adding -v before the -file can be used to print verbose output from the Ant launch process, which
REM can be useful for determining how Ant is building the final command line. That said, be careful using this
REM with the Control Panel and Monitor, since enabling verbose output in Windows can sometimes cause modules
REM and applications launched by these tools to be suspended until the parent process ends.
call "%AbsBaseDir%\external\ant\bin\ant" -file "%~dp0/launchProcess.xml" checkTargets %*

REM return the command prompt to the directory that started this script
cd %WorkingDir%
GOTO End

REM This is the ending condition
:End
REM pause

