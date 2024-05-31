#NoEnv  ; Recommended for performance and compatibility with future AutoHotkey releases.
#Warn  ; Recommended for catching common errors.
SendMode Input  ; Recommended for new scripts due to its superior speed and reliability.
SetWorkingDir %A_ScriptDir%  ; Ensures a consistent starting directory.

; This AutoHotKey script is used by GIFT to control Training Application windows.  
; Look at the mil.arl.gift.gateway.GatewayModuleUtils.java for usage.

; Use the following variable to emulate the script args coming from GatewayModuleUtils.java 
; for testing withing SciTE4AutoHotkey application
; 0 = 3
; 1 = setAlwaysOnTop
; Choose one of the following:
; 2 = WindowsForms10.Window.8.app.0.378734a
; 2 = WindowsForms10.Window.8.app.0.bf7771
;2 = *
; 4 = Simple Example Training Application
; Choose one of the following:
; 3 = On
; 3 = Off

If %0% > 0
{
	command = %1%

	if (command = "setAlwaysOnTop") and (%0% > 2)
	{
		WindowClass = %2%
		
		; Optional window title
		WindowTitle = %4%
		
		AlwaysOnTopValue = %3%
		
		WindowFound = false
		If (WindowClass = "*")
		{
			IfWinExist, %WindowTitle%
			{
				WindowFound = true
			}
			
		}else{

			IfWinExist ahk_class %WindowClass% 
			{
				WindowFound = true
			}
		}
				
		; The first matching window with the AHK class value will be selected
		if (WindowFound)
		{
			
			if (WindowTitle = "")
			{
				; The last found window (from the WinExist call above) will be updated
				WinSet, AlwaysOnTop, %AlwaysOnTopValue%
				; MsgBox Debug: Did it work?
				return
			}
			else
			{
				; The first matching window with the AHK class AND window title value will be selected
				IfWinExist %WindowTitle% 
				{
					; The last found window (from the WinExist call above) will be updated
					WinSet, AlwaysOnTop, %AlwaysOnTopValue%
					; MsgBox Debug: Did it work?
					return
				}
				else
				{
					MsgBox Found a window that matched the "ahk_class" criteria but not the window title of %WindowTitle%, therefore unable to set window always on top value to %AlwaysOnTopValue%.
					return
				}
			}
		} 
		else
		{
			MsgBox Did not find a window that matched the "ahk_class" of %WindowClass%, therefore unable to set window always on top value to %AlwaysOnTopValue%.
			return
		}

	}
	
	if (command = "giveFocus") and (%0% > 1) {
	
		WindowClass = %2%
		
		; Optional window title
		WindowTitle = %3%
		
		WindowFound = false
		If (WindowClass = "*")
		{
			IfWinExist, %WindowTitle%
			{
				WindowFound = true
			}
			
		}else{

			IfWinExist ahk_class %WindowClass% 
			{
				WindowFound = true
			}
		}
		
		If (WindowFound)
		{
			
			if (WindowTitle = "")
			{
				WinActivate ; Uses the last found window
		
				WinGetPos ,,,Width,Height,ahk_class %WindowClass%
				MidX := round(0.5*Width)
				MidY := round(0.5*Height)
				MouseMove, %MidX%, %MidY%, 0
				return
			}
			else
			{
				; The first matching window with the AHK class AND window title value will be selected
				IfWinExist %WindowTitle% 
				{
					WinActivate ; Uses the last found window
		
					WinGetPos ,,,Width,Height,ahk_class %WindowClass%
					MidX := round(0.5*Width)
					MidY := round(0.5*Height)
					MouseMove, %MidX%, %MidY%, 0
					return
				}
				else
				{
					MsgBox Found a window that matched the "ahk_class" criteria but not the window title of %WindowTitle%, therefore unable to give the window focus.
					return
				}
			}
		}
		else
		{
			MsgBox Did not find a window that matched the "ahk_class" of %WindowClass%, therefore unable to give the window focus.
			return
		}
	}
	
	if (command = "minimizeWindow") and (%0% > 1) {
	
		WindowClass = %2%
		
		; Optional window title
		WindowTitle = %3%
		
		WindowFound = false
		If (WindowClass = "*")
		{
			IfWinExist, %WindowTitle%
			{
				WindowFound = true
			}
			
		}else{

			IfWinExist ahk_class %WindowClass% 
			{
				WindowFound = true
			}
		}
		
		If (WindowFound)
		{	
			
			if (WindowTitle = "")
			{
				WinGetPos ,X,Y,,,ahk_class %WindowClass%
				Click, %X%, %Y%, 0
				
				WinSet, Bottom
							
				WinMinimize
				
				return
			}
			else
			{
				; The first matching window with the AHK class AND window title value will be selected
				IfWinExist %WindowTitle% 
				{
					WinGetPos ,X,Y,,,ahk_class %WindowClass%
					Click, %X%, %Y%, 0
					
					WinSet, Bottom
								
					WinMinimize
					
					return
				}
				else
				{
					MsgBox Found a window that matched the "ahk_class" criteria but not the window title of %WindowTitle%, therefore unable to minimize the window.
					return
				}
			}
		}
		else
		{
			MsgBox Did not find a window that matched the "ahk_class" of %WindowClass%, therefore unable to minimize the window.
			return
		}
	}
} 
else{
	MsgBox The script requires at least 1 argument to work correctly.
}