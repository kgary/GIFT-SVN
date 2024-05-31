@ECHO OFF

REM removed "-admin" from the command line arguments to prevent TSP scenario trigger hints from showing on screen
REM
REM Args:
REM        -window is used to prevent VBS from using full screen mode
REM
REM Revision History
REM     pre 10/11/17 - VBS versions prior to VBS3 v3.9.2 used LVC for DIS/HLA network traffic, hence the need for '-lvc' as a command line arg. 
REM                  - added check for newest version of VBS (v3.0 and v3.9.2) 
REM
REM         10/11/17 - VBS3 v3.9.2 no longer uses LVC but something called a Gateway, hence the need to replace '-lvc' with '-gateway'.  
REM                    Also need '-admin' for this new gateway server.
REM
REM         01/07/21 = VBS3 v19.1.3 has VBS4.exe file
REM              
REM 

SET VBS_EXE=vbs4.exe
IF NOT EXIST %VBS_EXE% set VBS_EXE=vbs3.exe
IF NOT EXIST %VBS_EXE% set VBS_EXE=vbs3_64.exe
IF NOT EXIST %VBS_EXE% set VBS_EXE=vbs2.exe

ECHO VBS exe is '%VBS_EXE%'.

IF NOT EXIST %VBS_EXE% (
	ECHO ERROR - Unable to find the VBS exe file in the directory of %~pd0 where this script is running in.
	ECHO.
	ECHO Please run the GIFTVBSGame.bat file in the VBS installation directory where vbs exe is located ^(e.g. vbs3.exe^).
	ECHO.
	PAUSE
) ELSE (
	ECHO starting VBS...
	start "" %VBS_EXE% -window -gateway -admin -autoassignside=west -autostart -multicast=0

)