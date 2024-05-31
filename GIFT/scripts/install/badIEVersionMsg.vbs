' This script shows a dialog indicating the user's Internet Explorer version is not the GIFT recommended version
msgbox "You are using an incompatible version of Internet Explorer (version " & WScript.Arguments(0) & ")." & vbCrLf & vbCrLf & _
	"GIFT currently supports Internet Explorer version 11 (other versions of Internet Explorer, Chrome and Firefox can also be used but are not tested against)." & vbCrLf & vbCrLf & _
	"For more information on this topic refer to the forums on gifttutoring.org.", 48, "Unsupported Internet Explorer version found!"