' This script is used to launch the derby server
'
Set WshShell = CreateObject("WScript.Shell")
WshShell.Run chr(34) & "external\db-derby-10.15.2.0-bin\bin\startNetworkServer.bat" & Chr(34), 0
Set WshShell = Nothing