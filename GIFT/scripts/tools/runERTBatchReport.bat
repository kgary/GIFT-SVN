@ECHO OFF
REM
REM This script is used to run the ERT Batch Report utility which will automatically
REM merge all GIFT created domain session files (e.g. sensor data, domain session message log)
REM and execute an ERT report for that data.
REM
REM optional script input: file containing configuration parameters
REM                        (default if not provided: GIFT\config\tools\ert\batchReportUtil.config)
REM

set BaseDir=%~dp0..\..
set BinDir=%BaseDir%\bin
set ExternalLibsDir=%BaseDir%\external

REM !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
REM change to your 64x Java for more memory (heap space) size
set JDK_64x_HOME=%ExternalLibsDir%\openjdk-11.64x
if exist "%JDK_64x_HOME%" (
	set GIFT_JAVA_HOME=%JDK_64x_HOME%
)
REM set JAVA_XMX=8192m	
set JAVA_XMX=1024m
REM !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

set DEBUG_PORT=49999
set LibraryPathExtension=
set "ClasspathExtension=%BinDir%\gift-ert-batch.jar"
set Class="mil.arl.gift.tools.ert.server.BatchReportUtil"

set "JavaClasspath=config\tools\ert;%BinDir%\gift-common.jar;%BinDir%\gift-commongwt.jar;%BinDir%\gift-sensor.jar;%BinDir%\gift-ums.jar;%BinDir%\jaxb_generated.jar;%ExternalLibsDir%\slf4j\*;%ExternalLibsDir%\jsonsimple\json_simple-1.1.jar;%ExternalLibsDir%\gwt\gwt-2.7.0\gwt-servlet.jar;%ExternalLibsDir%\jetty-9.4.41\lib\servlet-api-3.1.jar;%ExternalLibsDir%\SuperCSV-with_src-1.52.jar;%ExternalLibsDir%\commons-lang-2.4.jar;%ExternalLibsDir%\commons-cli-1.2.jar;%ExternalLibsDir%\hibernate\commons-collections-3.1.jar;%ExternalLibsDir%\vecmath.jar"
set "JavaDebugArgs=-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=%DEBUG_PORT%"

cd "%BaseDir%"
"%GIFT_JAVA_HOME%\jdk-11\bin\java" -Xmx%JAVA_XMX% %JavaArgs% %JavaDebugArgs% -classpath "%JavaClasspath%;%ClasspathExtension%" -Djava.library.path=%LibraryPathExtension% -Dgift.home="%BaseDir%" -Dserver.logs=output/logger/tools %Class% %1

REM allow the user to see the error on the command line
REM Note: has to be the next command executed or errorlevel might be changed
if %ERRORLEVEL% GTR 0 pause

REM go back to starting directory
cd %~dp0

exit