@ECHO OFF

REM Run the Java class that sets up the server
..\..\..\external\jdk-11\bin\java -cp ../../../external/jsonsimple/* ExampleStrategyProvider.java

:END
REM if there was an error than pause this script
IF !ERRORLEVEL! NEQ 0 (
    pause
)