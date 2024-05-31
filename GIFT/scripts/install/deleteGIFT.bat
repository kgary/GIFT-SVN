@echo off
REM
REM This script will delete the file provided as the first argument:
REM      deleteGIFT.bat <file>
REM where...
REM        <file> Either a relative to this script file name or an absolute file name
REM
REM example: deleteGIFT.bat "C:\work\GIFT 2016-1"
REM 

REM check first input arg existence
IF "%~1" == "" (
	echo The first argument must be the path of a file/folder to delete.
	set ERRORLEVEL=1
	GOTO:END
)

echo Deleting %~1

REM the delay is needed in order to allow the GIFT uninstaller to close and release any file locks (e.g. uninstall log file)
timeout /T 10 /NOBREAK

rmdir /s /q "%~1"

REM delete this batch file, 
REM because it should have been copied to the User's temp directory and we don't want it to remain when finished
REM without the '& exit' the command prompt would remain open
(goto) 2>nul & del "%~f0" & exit