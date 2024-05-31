@echo off

@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at

@REM   http://www.apache.org/licenses/LICENSE-2.0

@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.

CALL "%~dp0derby_common.bat" %*

REM GIFT: The derby scripts require a Java, therefore use the JRE in case the JAVA_HOME environment variable is not set to the JDK
REM check for 64x JDK
if exist %~dp0..\..\openjdk-11.64x\ (
	echo 64x JDK exists
	set "_JAVACMD=%~dp0..\..\openjdk-11.64x\jdk-11\bin\java.exe"
)

if "%_JAVACMD%"=="" goto end

if "%_USE_CLASSPATH%"=="no" goto runNoClasspath
if not "%CLASSPATH%"=="" goto runWithClasspath

REM GIFT: Added '-h 0.0.0.0' derby command line argument to allow connections from any location, not just localhost

REM GIFT: 1/7/2015 Added -Dderby.storage.useDefaultFilePermissions=true argument to all calls to start the network server. This argument
REM 	tells the Derby system to use the JVM's default user permissions when generating Derby files rather than using the operating system's 
REM 	user permissions. This is done in order to prevent an issue where logging out, restarting, or shutting down Windows while the 
REM 	network server was running would render all other users on the same Windows system unable to start the network server.

set JmxArgs=-Dcom.sun.management.jmxremote.port=7021 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false

:runNoClasspath
"%_JAVACMD%" %JmxArgs% %DERBY_OPTS% "-Dderby.system.home=%~dp0..\..\..\data" "-Dderby.storage.useDefaultFilePermissions=true" -classpath "%LOCALCLASSPATH%" org.apache.derby.drda.NetworkServerControl start -h 0.0.0.0 %DERBY_CMD_LINE_ARGS% 
goto end

:runWithClasspath
"%_JAVACMD%" %JmxArgs% %DERBY_OPTS% "-Dderby.system.home=%~dp0..\..\..\data" "-Dderby.storage.useDefaultFilePermissions=true" -classpath "%CLASSPATH%;%LOCALCLASSPATH%" org.apache.derby.drda.NetworkServerControl start -h 0.0.0.0 %DERBY_CMD_LINE_ARGS%
goto end

:end
set _JAVACMD=
set DERBY_CMD_LINE_ARGS=