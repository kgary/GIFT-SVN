@echo off
REM
REM This script will check to see if the Window's user executing
REM this script is one of the owners of the directory specified
REM as the only argument to this file.
REM
REM The property "doIOwn" will be set to "true" if the user is one of the owners.
REM Otherwise the property will be empty.
REM
REM Usage:   doIOwnThisFolder.bat c:\work\gift\data\derbyDb\GiftUms\tmp 
REM

set "foldername=%1"
set "owner="
set doIOwn=

REM echo (begin) doIOwn = %doIOwn%

setlocal enableDelayedExpansion
REM Apparently this 'dir /q' command requires increased permissions on a folder
REM that say an admininstrator owns.  In this case it will return a 'file not found' type
REM error.
for /f "tokens=1*delims=\" %%i in ('dir /q %foldername%^|findstr "<DIR>"') do (
	set "owner=%%j"
	REM echo owner = !owner!
	REM echo username = !username!
	
	REM Simple check to see if my username is in the owner string (e.g. "agift       .")
	echo(!owner!|findstr /c:"!username!1" >nul && (
		REM echo found !username! in '!owner!'
		set doIOwn=true
	)
)

REM echo (end) doIOwn = !doIOwn!
endlocal

