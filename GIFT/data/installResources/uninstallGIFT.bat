@ECHO OFF
REM *
REM * This script is used to execute the GIFT uninstaller which will uninstall GIFT.
REM * 

REM Check to make sure running in Windows
SET ERRORLEVEL=0
ver | findstr "Windows" > nul
IF %ERRORLEVEL% NEQ 0 (
	echo Unable to determine if you are running on a Windows Operating System.  
	echo GIFT only supports the Windows Operating System.
	echo.
	pause
	GOTO:EOF
)

REM Check for the existence of third parties (right now just check for ant) which is necessary
REM to install GIFT
if not exist "GIFT/external/ant/bin/ant" (
	echo Do you have the GIFT third parties in the GIFT/external folder?
	set ERRORLEVEL=1
	GOTO:END
)

cd GIFT\scripts\util
call launchProcess.bat start uninstaller

REM return back to the start directory this script was launched from
cd %~dp0

:END
REM if there was an error than pause this script
setlocal enableDelayedExpansion
IF !ERRORLEVEL! NEQ 0 (
    pause
)
endlocal
