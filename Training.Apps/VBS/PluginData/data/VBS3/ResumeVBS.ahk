#NoEnv  ; Recommended for performance and compatibility with future AutoHotkey releases.
SendMode Input  ; Recommended for new scripts due to its superior speed and reliability.
SetWorkingDir %A_ScriptDir%  ; Ensures a consistent starting directory.

IfWinExist ahk_class VBS3
{
    WinActivate
	Send {Escape}
}
IfWinExist ahk_class VBS3 19.1.6.6
{
	WinActivate
	Send {Escape}
}