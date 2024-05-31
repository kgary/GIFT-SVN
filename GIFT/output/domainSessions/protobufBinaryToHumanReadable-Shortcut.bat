@ECHO OFF

REM This script serves as a shortcut to the protobufBinaryToHumanReadable.bat.
REM To use, drag a protobuf binary log file onto this script.
REM The converted log will be put in output\converted

set BaseDir=%~dp0..\..
cd "%BaseDir%\scripts\dev-tools"
protobufBinaryToHumanReadable.bat %*