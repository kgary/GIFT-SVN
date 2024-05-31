@ECHO off

REM
REM This script will check if the Internet Explorer version on this machine is
REM compatible with GIFT.
REM
REM Currently GIFT officially supports IE 11 (and Chrome, unofficially). 
REM [As of GIFT 2015-1]
REM
REM Please refer to the Forums on gifttutoring.org for more information.
REM
REM Note: to skip this check uncomment the following line by removing "REM".  Then save this file.
REM GOTO:END
REM

setlocal ENABLEEXTENSIONS
set "KEY_NAME=HKEY_LOCAL_MACHINE\Software\Microsoft\Internet Explorer"

REM 'Version' works on IE 9 and earlier - http://connect.microsoft.com/IE/feedback/details/785757/wrong-ie-version-data-in-windows-8-registry
REM now using 'svcVersion'
set "VALUE_NAME=svcVersion"

REM for debugging purposes...
REM REG QUERY "%KEY_NAME%" /v "svcVersion"

REM find the full version number
For /F "tokens=2*" %%A IN ('REG QUERY "%KEY_NAME%" /v "%VALUE_NAME%"') Do (
 set "versionNumber=%%B"
)

REM get only the first number from the version number
for /f "delims=." %%a in ("%versionNumber%") do set "versionNumber=%%a"

ECHO Your IE version number is %versionNumber%

IF %versionNumber% LEQ 10 (
	GOTO:BAD_VERSION
	
) ELSE (
   
   IF %versionNumber% GEQ 12 (
		GOTO:BAD_VERSION
   )
) 

GOTO:END

:BAD_VERSION
REM show error message
cscript badIEVersionMsg.vbs %versionNumber%
set %ERRORLEVEL% = 1

:END
REM Debugging
REM pause