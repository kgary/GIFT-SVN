@ECHO OFF
REM
REM This script will add the "System32" folder to the Path environment variable
REM which is needed to run various batch commands during the execution of GIFT.
REM

REM Debugging purposes
REM ECHO %PATH%

REM This for loop will take the command line executable path (e.g. "C:\Windows\system32\cmd.exe")
REM retrieved from %comspec% and remove the "\cmd.exe" extension from the filename string.  Then 
REM the remaining string is the path to the system32 folder which is appended to the path env variable.
REM We don't need to check if it's already in the path because this addition only last for the
REM duration of the session anyway. 
for /f "delims=" %%a in ("%comspec%") do set "PATH=%PATH%;%%~DPa"
ECHO System32 is on the Path environment variable.

GOTO:END

:END
ECHO End system32 Path check.
REM pause