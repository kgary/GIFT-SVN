@REM
@REM Copyright © 2010-2014 Nokia
@REM
@REM Licensed under the Apache License, Version 2.0 (the "License");
@REM you may not use this file except in compliance with the License.
@REM You may obtain a copy of the License at
@REM
@REM      http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing, software
@REM distributed under the License is distributed on an "AS IS" BASIS,
@REM WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@REM See the License for the specific language governing permissions and
@REM limitations under the License.
@REM

@echo off

REM Need to adjust this call to use the local Java exe
REM java -jar "%~dp0/../lib/jsonschema2pojo-cli-1.2.1.jar" %*

if exist "%~dp0..\..\jdk-11\" (
	echo 64x JDK exists
	set "_JAVACMD=%~dp0..\..\jdk-11\bin\java.exe"
) else (
	echo 64x JDK DOES NOT exist, using 32 bit
	set "_JAVACMD=%~dp0..\..\jdk1.8.0_101\jre\bin\java.exe"
)
if "%_JAVACMD%"=="" goto error

"%_JAVACMD%" -jar "%~dp0/../lib/jsonschema2pojo-cli-1.2.1.jar" %*
goto end

:error
echo Unable to find Java at %_JAVACMD%.  Do you have the GIFT Third Party libraries correctly placed in GIFT\external\?
pause
goto end

:end
set _JAVACMD=
