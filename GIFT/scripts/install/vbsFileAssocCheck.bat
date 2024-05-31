@ECHO OFF
REM
REM This script will check the Windows file association for ".vbs" file extensions.
REM GIFT executes VBS files and therefore needs Windows to understand how to handle
REM files of that type.  If the association is incorrect, this script will change
REM the association appropriately (after asking for admin privileges).
REM

for /f "delims=" %%a in ('assoc ^| FINDSTR ".vbs"') do set result=%%a

REM Debug purposes
REM echo result = %result%

IF "%result%" == "" (
	REM The File association is missing, 
	REM therefore need to prompt user and then set the appropriate file association.
	
	GOTO:ASK-FOR-PERMISSIONS
	
) ELSE (

	REM Replace the 'VBSFile' string with empty string and if the association
	REM is different than the association was correct.
	@setlocal enableextensions enabledelayedexpansion
	set REPLACE=%result:VBSFile=%
	REM ECHO replace = !REPLACE!
	
	IF "%result%" == "!REPLACE!" (
		REM The file association is incorrect,
		REM therefore need to prompt user and then set the appropriate file association.		
		
		ECHO found incorrect .vbs file association,
		ECHO therefore setting correct file association...
		ECHO.
		GOTO:ASK-FOR-PERMISSIONS
	) ELSE (
	
		ECHO vbs file association is correct.
	)
)

GOTO:END

:ASK-FOR-PERMISSIONS
ECHO GIFT requires Admininstration privileges to set the file association
ECHO for '.vbs' files.  (In order for GIFT to work you will need to proceed)
ECHO.
SET /P ANSWER=Would you like to continue? (Y/N) 
ECHO You chose: %ANSWER%
ECHO.

IF /i {%ANSWER%}=={y} (
	
	ECHO setting file association...
	
	GOTO:SET-FILE-ASSOCIATION
) ELSE (
	GOTO:ERROR
)

GOTO:END

:SET-FILE-ASSOCIATION
REM Normally I would call a '.vbs' file to use admin privileges, however the .vbs file 
REM extension association is the problem this script is trying to fix.  Therefore the
REM other windows solution is to use a shortcut that is configured to be run as admin.
call "vbsFileAssocSet.bat.Admin.lnk"

IF %ERRORLEVEL% neq 0 (
	GOTO:ERROR
) ELSE (
	ECHO .vbs file association set successfully.
)

GOTO:END

:ERROR
set ERRORLEVEL=1
ECHO Failed to set .vbs file association.  GIFT may have issues running.
GOTO:END

:END
ECHO End vbs file association check.
REM pause
