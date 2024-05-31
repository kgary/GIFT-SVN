@ECHO OFF
REM 
REM This script can be used to close any running GIFT Derby Database server (usually launched for accessing the UMS)
REM and is useful when the Derby db server still has a lock on the database.
REM It will simply call the stopNetworkServer.bat file in the external folder of GIFT.
REM 
set STOP_SCRIPT="..\..\external\db-derby-10.15.2.0-bin\bin\stopNetworkServer.bat"

IF EXIST %STOP_SCRIPT% (
	call %STOP_SCRIPT%
) ELSE (
	ECHO Unable to find %STOP_SCRIPT%.
	ECHO Do you have the third parties installed in GIFT\external ?
	ECHO.
	pause
)