@ECHO OFF
REM This script will set a Windows Environment variable using two input arguments:
REM     setEnvVariable.bat  <name>  <value>
REM where... 
REM       <name> is the name of the environment variable to set (e.g. JAVA_HOME) 
REM       <value> is the value of the environment variable (e.g. C:\Program Files (x86)\Java\openjdk-11\).
REM					Note: if there are spaces in this argument make sure to surround the argument with double quotation ("<value>")
REM                 Note: if the value is "" then the environment variable will be cleared/deleted
REM
REM example: setEnvVariable.bat JAVA_HOME "C:\Program Files (x86)\Java\openjdk-11\"
REM

REM check first input arg existence
IF "%~1" == "" (
	echo The first argument must be the name of the windows environment variable ^(e.g JAVA_HOME^).
	set ERRORLEVEL=1
	GOTO:END
)

REM check if the second argument needs to be surrounded with quotation marks.
IF NOT "%~3" == "" (
	echo Found a third, unsupported, argument of "%3". Perhaps the 2nd argument needs to be surrounded with quotation marks because it has spaces/special characters?".
	set ERRORLEVEL=1
	GOTO:END
)


REM check if the variable should be removed, not set
IF "%~2" == "" (

	echo Removing %~1 environment variable.

	setx %~1 ""
	
) ELSE (

	echo Setting %~1 environment variable to %~f2.

	setx %~1 "%~f2"
)


:END
REM if NOT %ERRORLEVEL% == 0 pause
if NOT %ERRORLEVEL% == 0 EXIT %ERRORLEVEL%