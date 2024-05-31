@ECHO OFF
REM Set this property to the error code that GIFT returns when it handles the error
set GIFT_ERROR_CODE=101

REM NOTE: This script is intended to be used in conjunction with the Ant configuration file to 
REM receive the command line arguments passed in from the <exec> task. Ideally:
REM %*: the command used to launch the JVM for running the applications invoked by Ant. 
REM      i.e. (from Ant) JAVA_HOME\bin\java ${JmxArgs} ${JavaArgs} ${JavaDebugArgs} -classpath ${toString:classpath} -Djava.library.path=${toString:libraryPath} -Djava.io.tmpdir=${GIFT_TEMP} ${Class}
set Command=%*
%Command%

REM Use the following 2 lines to debug the command
REM echo %Command%
REM pause

if %ERRORLEVEL% GTR 0 ( IF %ERRORLEVEL% NEQ %GIFT_ERROR_CODE% pause )
exit