@ECHO OFF
set JDK_64x_HOME=%~dp0..\..\external\jdk-11\bin
if exist "%JDK_64x_HOME%" (
	set JDK_HOME=%JDK_64x_HOME%
)
set PATH=%JDK_HOME%;%PATH%
start /min cmd /k call  "%~dp0..\..\external\activemq\bin\win64\activemq.bat"
