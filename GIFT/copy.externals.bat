@ECHO OFF

REM
REM		***Developer Use Only***
REM 
REM  This script will copy the GIFT third party files needed to build and run GIFT from
REM  a folder named "Thirdparty\GIFT.external" to the GIFT\external\ folder.
REM 
REM  It will recursively search up the directory tree starting from the folder this script resides in.
REM 

set THIRD_PARTY_DIR=ThirdParty
set THIRD_PARTIES_DIR=GIFT.external

REM Find the folder where the third party files are located
pushd "%~dp0"
:loop
	cd ..
	
	REM Check for reaching root of drive letter (e.g. "c:\")
    IF "%cd%" == "%cd:~0,3%" (
		GOTO:THIRD-PARTY-DIR-NOT-FOUND
	)
	
    IF NOT EXIST %THIRD_PARTY_DIR% GOTO loop
	
set thirdPartyDir=%cd%\%THIRD_PARTY_DIR%
popd
REM ECHO found third party directory at %thirdPartyDir%

IF EXIST "%thirdPartyDir%\%THIRD_PARTIES_DIR%" (
	GOTO:COPY-FILES
)

GOTO:THIRD-PARTIES-DIR-NOT-FOUND

REM Unable to find the third party directory
:THIRD-PARTY-DIR-NOT-FOUND
echo Unable to find the Third party directory named "%THIRD_PARTY_DIR%" after walking up the directory tree start at "%~dp0".
echo Are you sure that directory exist?
echo.
pause
GOTO:EOF

REM Unable to find the third party files directory
:THIRD-PARTIES-DIR-NOT-FOUND
echo Unable to find the third party files directory named "%THIRD_PARTIES_DIR%" in "%thirdPartyDir%" after walking up the directory tree start at "%~dp0".
echo Are you sure that directory exist?
echo.
pause
GOTO:EOF

REM Copy the third party files to external directory
:COPY-FILES
ECHO.
ECHO About to copy files found in "%thirdPartyDir%\%THIRD_PARTIES_DIR%" 
ECHO to "%~dp0external"
ECHO.

SET /P ANSWER=Would you like to continue? (Y/N) 
ECHO You chose: %ANSWER%
ECHO.

IF /i {%ANSWER%}=={y} (

	REM Make sure system32 is on path environment variable to use xcopy command
	call "scripts\install\system32PathCheck.bat"

	ECHO.
	ECHO Please Wait...
	ECHO.
	
	REM add argument /f AND remove /q to display a list of files that are to be copied
	REM /q - Suppresses the display of xcopy messages.
	REM /y - Suppresses prompting to confirm that you want to overwrite an existing destination file.
	REM /e - Copies all subdirectories, even if they are empty.
	xcopy /q /y /e "%thirdPartyDir%\%THIRD_PARTIES_DIR%" "%~dp0external"
	
	ECHO.
	ECHO Done.
	ECHO.
	pause
	
) ELSE (
	ECHO Good-bye.
)


