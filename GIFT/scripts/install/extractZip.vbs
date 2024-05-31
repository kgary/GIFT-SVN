' This script is used to unzip a zip file to a destination folder.
' If the destination folder doesn't exist it will be created.
'
' Arguments:
'	Argument 0: The location of the zip file.
'	Arugment 1: The folder the contents should be extracted to.

if not WScript.Arguments.Count = 2 then
    WScript.Echo "Missing parameters"
	WScript.Quit 1
end if

Wscript.Echo "Unzipping " & WScript.Arguments(0)

'If the extraction location does not exist create it.
Set fso = CreateObject("Scripting.FileSystemObject")
If NOT fso.FolderExists(fso.GetAbsolutePathName(WScript.Arguments(1))) Then
   fso.CreateFolder(fso.GetAbsolutePathName(WScript.Arguments(1)))
End If

'Extract the contants of the zip file.
set objShell = CreateObject("Shell.Application")
set FilesInZip=objShell.NameSpace(fso.GetAbsolutePathName(WScript.Arguments(0))).items
objShell.NameSpace(fso.GetAbsolutePathName(WScript.Arguments(1))).CopyHere(FilesInZip)

''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
'
' Some code that could be used to count the number of files unzipped...
'
'Dim count
'count = CountFiles(fso.GetAbsolutePathName(WScript.Arguments(1)))

'set count = CountFiles(objShell.NameSpace(fso.GetAbsolutePathName(WScript.Arguments(0))))
'Wscript.Echo "count " & count
'Do Until objShell.NameSpace(fso.GetAbsolutePathName(WScript.Arguments(0))).Items.Count <= objShell.NameSpace(fso.GetAbsolutePathName(WScript.Arguments(1))).Items.Count
'	Wscript.Echo "waiting " & objShell.NameSpace(fso.GetAbsolutePathName(WScript.Arguments(0))).Items.Count & " " & objShell.NameSpace(fso.GetAbsolutePathName(WScript.Arguments(1))).Items.Count
'    WScript.Sleep(300)
'Loop

''''''''''''''''''''''''''''''''''''''''''''''''''''''''''

' Allowing the CopyHere to clean up
'WScript.Sleep(2000)

Set fso = Nothing
Set objShell = Nothing

Function CountFiles (ByVal StrFolder)
Dim ParentFld
Dim SubFld
Dim IntCount

Set ParentFld = fso.GetFolder (StrFolder)

' count the number of files in the current directory
IntCount = ParentFld.Files.Count

For Each SubFld In ParentFld.SubFolders
' count all files in each subfolder
IntCount = IntCount + CountFiles(SubFld.Path)
'If you just want the one directory, you can comment the following line.
wscript.echo wscript.arguments(0) & "\" & ParentFld.Name & "\" & SubFld.Name & ": " & intcount
Next

'Returns the counted files, remove the comment if you just want the one directory
CountFiles = IntCount
End Function 