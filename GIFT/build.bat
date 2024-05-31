@ECHO OFF

ECHO Executing build...

REM Check to make sure running in Windows
ver | findstr "Windows" > nul
if %ERRORLEVEL% NEQ 0 (
	echo Unable to determine if you are running on a Windows machine.  GIFT only supports the Windows OS.
	set ERRORLEVEL=1
	GOTO:END
)

REM Check for GWT library
if not exist "%~dp0/external/gwt" (
	echo Unable to find the GWT library.  Did you install/extract the GIFT third parties into the external folder?
	set ERRORLEVEL=1
	GOTO:END
)
SET GWT_HOME=%~dp0/external/gwt

REM Check for ActiveMQ library
if not exist "%~dp0/external/activemq" (
	echo Unable to find the ActiveMQ library.  Did you install/extract the GIFT third parties into the external folder?
	set ERRORLEVEL=1
	GOTO:END
)
SET ACTIVEMQ_HOME=%~dp0/external/activemq

REM Check for Ant
if not exist "external/ant/bin/ant" (
	echo Unable to find the Ant library.  Did you install/extract the GIFT third parties into the external folder?
	set ERRORLEVEL=1
	GOTO:END
)

set ExternalLibsDir=%~dp0\external
SET "EXTRACT_ZIP_VBS=%~dp0\scripts\install\extractZip.vbs"
set "JDK_64x_ZIP=%ExternalLibsDir%\openjdk-11.64x.GIFT.zip"
set "JDK_EXTRACT_TO=%ExternalLibsDir%\"
set JDK_64x_HOME=%ExternalLibsDir%\jdk-11

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

REM set JDK 64 bit variables
if %JDK64xZipExists% == 1 (
	REM prioritize 64x JDK
	set JDK_HOME=%JDK_64x_HOME%
	set JDK_ZIP=%JDK_64x_ZIP%
)

REM Check for jdk 8 - the installer will not launch unless it is there.
if not exist "%JDK_HOME%" (

	if not exist "%JDK_ZIP%" (
	
		echo ERROR: Unable to find %JDK_ZIP%
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

REM
REM Determine if path to GIFT is too long to build GIFT
REM

set pathToGIFT=%~dp0
REM echo %pathToGIFT%

REM get path length including drive letter and GIFT folder
ECHO %pathToGIFT%> tempfile.txt
FOR %%? IN (tempfile.txt) DO ( SET /A currentLength=%%~z? - 2 )
del tempfile.txt

REM the max length (as of GIFT 3.0) is 216 characters between the drive letter and GIFT folder (therefore add 7 for "C:\" and "GIFT")
set maxlength=223
REM echo clength = %currentLength%

IF %currentLength% gtr %maxlength% (

	setlocal enabledelayedexpansion
	set /a numberToRemove=%currentLength%-%maxlength%
    echo There are too many characters in your path to the GIFT folder.  Please remove !numberToRemove! characters from the path between the drive letter and the GIFT folder.
	set ERRORLEVEL=1
    GOTO:END
)

IF "%1"=="" (
	REM no build argument, therefore build release version which includes javadocs
	GOTO:RELEASE
) ELSE ( 
	IF "%1"=="exitOnError"  (
		REM the argument relates to what to do at the end of this script, therefore build release version which includes javadocs
		GOTO:RELEASE
	) ELSE (
		GOTO:SPECIAL
	)
)

:RELEASE
ECHO Building release version
TITLE %~dp0 release
REM Use the following line (and comment the 'ant' call after it) when you want the 
REM output of the build to go to the file named build.out.txt for debugging purposes
REM call "external/ant/bin/ant" release > build.out.txt 2>&1
call "external/ant/bin/ant" release
GOTO:END
  
:SPECIAL
ECHO Using build file argument of %1
TITLE %~dp0 %1
REM Use the following line (and comment the 'ant' call after it) when you want the 
REM output of the build to go to the file named build.out.txt for debugging purposes
REM call "external/ant/bin/ant" "%1" > build.out.txt 2>&1
call "external/ant/bin/ant" "%1"
GOTO:END

REM Debugging 
REM SET /P ANSWER=Keeping window open for debug purposes (error level = %ERRORLEVEL%)

:END
IF NOT %ERRORLEVEL% == 0 (
	REM There was an error
	
	REM Execute a pause if the exitOnError argument was NOT used
	IF NOT "%1"=="exitOnError" pause
) ELSE (
	REM There was no error
	
	REM Cause the command window to exit if the exitOnError argument was used
	IF "%1"=="exitOnError" Exit 0
) 