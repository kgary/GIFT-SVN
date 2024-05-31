'
' This script will give ownership to the file/folder provided as an argument.
' It will then give the Window's user calling this script full permissions.
'
' Usage: giveOwnershipAndFullPermissions.vbs c:\work\gift\data\DerbyDb\GiftUms\tmp
'
Dim arg
arg = WScript.Arguments(0)

Set objShell = CreateObject("Shell.Application")
	Set objFSO = CreateObject("Scripting.FileSystemObject")
	If objFSO.FolderExists(arg) Then
	
		arg = Chr(34) & arg & Chr(34)
		Wscript.Echo arg
		
		Dim output
		objShell.ShellExecute "takeown", "/F "&arg&"", "", "runas", 0

		objShell.ShellExecute "cacls", ""&arg&" /E /G  "&Chr(34)&"Users"&Chr(34)&":F", "", "runas", 0
		
		'THIS WORKS!!!
		'objShell.ShellExecute "cacls", ""&Chr(34)&"C:\Program Files (x86)\Character Server\Logs"&Chr(34)&" /E /G  "&Chr(34)&"Users"&Chr(34)&":W", "", "runas", 1
		
		If intRunError <> 0 Then
			Wscript.Echo "Error assigning permissions for user to " & arg & ".  Error is " & intRunError
		End If
	End If
