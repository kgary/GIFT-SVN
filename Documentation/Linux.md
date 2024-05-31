# Running GIFT on a Linux VM (on top of a Windows machine)

## Dec 15 : 
1) On a Linux VM, I first downloaded the 2023 gift zip.
2) I then built it from cmd using the command:
> GIFT_2023-1/release_2023-1/GIFT/scripts/dev-tools/Linux/installGIFT
3) Next, I tried to launch gift using the command:
GIFT_2023-1/release_2023-1/launchGIFT 
It failed, threw a couple of errors, which basically were:
3.1) Connection to UMS database failed & Connection to LMS database failed.
3.2) Then it also showed a warning msg asking me to download the MediaSemanticsCharacterServer.exe. 
I did that & also added the full path to it in the ``GIFT/config/tutor/server/webapps/tutor.xml`` file.
It still showed me this warning. So, I commented all the lines until I reached the first "End Character Server" line, which is what it asked me to do in this tutor.xml file.
4) Next, I tried to launch GIFT using the launchControlPanel.bat file. It opened the Control Panel as expected. 
4.1) In the control panel, I clicked the GIFT dashboard button.
Outcome: It just loads for a while, & then does nothing. There's nothing running on localhost:8080
Expectation: The GIFT dashboard should be running on localhost:8080
4.2) In the control panel, I clicked the Monitor button. It opened the GIFT monitor window as expected. I then clicked the "Launch All Modules" button. All modules started except the UMS Module, LMS Module & the Gateway module as well. The gateway module was expected to fail as the default configuration settings have to be changed & a training app would have to be connected. But again. I'm not sure why the UMS & LMS modules aren't starting. This is basically the above mentioned errors in (3.1).

## Dec 17
1) This time on a Linux VM, I tried to do everything from the terminal. For starters I reverted back to the original tutor.xml (undoing any changes I did - as per (3.2)).
2)I ran the below install script:
> GIFT_2023-1/release_2023-1/GIFT/scripts/dev-tools/Linux/installGIFT
3) I got a bunch of errors - namely telling me that abunch of random files like launchGIFT.bat, uninstallGIFT.bat, GIFTReadme.txt all had read permissions only. So I  got them write permissions as well. The install command worked smoothly.
4) I then ran the launch GIFT command:
> GIFT_2023-1/release_2023-1/launchGIFT 
5) All modules were up except the UMS, LMS & lastly the Gateway module. Got the same output as above (on Dec 15).


## Summary (Mar 17, 2024)
1) The aim of this file is to run GIFT on a Linux environment.
2) Although each try in this file ends up with errors, these were finally resolved/ unproducable again(pertaining to some minor, unrepeatable host machine issue as I was trying on VMs and Mac machines and not on a Linux-host machine).
3) The steps to run GIFT on a Linux environment are mentioned in the Summary section of [here](./Installation-And-Setup.md).