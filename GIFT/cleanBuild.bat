@ECHO OFF

REM set java home to static path to GIFT provided JDK
set BaseDir="%~dp0"
set JDK_64x_HOME="%BaseDir%\external\jdk-11\"
if exist "%JDK_64x_HOME%" (
	set JAVA_HOME=%JDK_64x_HOME%
)

call "external/ant/bin/ant" clean
if %ERRORLEVEL% GTR 0 pause