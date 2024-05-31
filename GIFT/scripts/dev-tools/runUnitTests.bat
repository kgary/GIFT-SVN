@ECHO OFF

echo WARNING: This script will temporarily shut down the network server, which may
echo		prevent applications like the UMS module, the single process launcher, 
echo		and the authoring tools from accessing the database if they are left 
echo		running when the network server is shut down. 
echo.
pause

cd ..\..\
set LaunchDerbyServerScript="%~dp0..\install\launchDerbyServer.vbs"

REM Stop a currently running derby server which maybe connected to a different
REM GIFT UMS db (i.e. this can happen when you have multiple GIFTs on your machine)
call "%~dp0..\..\external\db-derby-10.15.2.0-bin\bin\stopNetworkServer.bat"

REM Launch the derby server
cscript %LaunchDerbyServerScript%

REM This script runs the GIFT Unit Tests
start "" /B /WAIT build.bat test
pause

