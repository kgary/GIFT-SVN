@ECHO OFF
REM Run the export ant script with the given output directory

REM set java home to static path to GIFT provided JDK
set JDK_64x_HOME=%~dp0..\..\external\openjdk-11.64x\
if exist "%JDK_64x_HOME%" (
	set JAVA_HOME=%JDK_64x_HOME%
)
REM echo %JAVA_HOME%

external\ant\bin\ant -f scripts\dev-tools\export.xml -Dbase.path=%1 -Doutput.path=%2