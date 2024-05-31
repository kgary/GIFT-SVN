# Running GIFT From Scratch


## Nov 9 :

- On my windows machine, unzipped the GIFT file(downloaded from site).

 - Fixed bugs:  sun-security-jaas.jar package was missing, so downloaded it externally & added on eclipse. 
   
- Project built successfully - no bugs. 
- Ran the installGIFT.bat file
- Downloaded the virtual human character server, media semantics character server exe & the media semantics character builder exe as well as powerpoint 2016.
- Don't think GIFT was able to find the ppt-2016 location, but it found the other 2. and remaining of the installation process.
- Then I ran launchGIFT.bat, which I presumed would start up the GIFT application, but nope, it just ran a bunch of commands checked version & then just ended.
- Dr. Gary then told about the launchControlPanel.bat (in GIFT/scripts), which I then ran, it opened up the GIFT Control panel from where we had options for opening up GIFT Dashboard(on Browser) & the button to monitor all admin actions like check the status of each module independently whether it is running and on what address. It shows up an address for each module. And by the naming convention it seems like this address is that of a Queue per module.
- I played around and killed some modules, then tried to relaunch them. Everything came back up, except the Tutor Module, it still shows up a red button beside the Tutor module, I believe which says that there is no active tutor-module-queue.
-  The control panel also had a button to open up the GIFT Dashboard on the browser. This essentially opened up the  GIFT Web dashboard. I had to log in & we could see all the courses available in there.
- On clicking on 1 random course, it threw up an error, essentially saying that the course couldn't be started because 

> a connection to the tutor couldn't be established.
- It asked me to check the common.properties file (in GIFT/common/config), & asked me to update the TutorURL which would look like *localhost:8090* to *MySystemIPAddr/tutor*.
- I did that, and reopened the control panel & launched the web dashboard to see If I'll be able to access the course this time, still was unable to access. In the monitor window of the control panel, it still showed the tutor module to be off (killed). And I can't launch it no matter what.

- Moving on, I decided to work on **connecting** an external app(like their sample app) to GIFT's **gateway module interop plugin** in order to establish that connection.
- I went about following the steps in [Developer Guide](https://gifttutoring.org/projects/gift/wiki/Developer_Guide_2023-1#Creating-a-new-Gateway-Module-Interop-Plugin) and tried to implement the steps as mentioned in there.
- My first blocker was to try & create a control-config file, which they asked me to copy from this particular file in their codebase. Turns out this particular file is nowhere to be found in their specified location. But they said, this is not a mandatory step & can be skipped.
- Next, I started by creating a new PluginInterface file, which essentially asks you to copy paste some stuff, in a way that it explains each sub part that we're using next.
- In the first step, it asked us to import some stuff, literally the first line was nowhere to be found:

     *import generated.course.MyPluginInputs;*

- I could see the generated.course.*, but there was nothing named MyPluginInputs. (Based on this naming convention, I presume this is something that I must create, Idk, it's not mentioned.)
- Moving on, some of the methods they asked me to build, weren't satisfying the parent interface that I was implementing. So these parent interfaces used certain classes in its abstract code decalaration. These guys asked me to write such methods that would essentially override the parent methods, but use arguments that were the **grand-parent** (for loss of a better term) arguments. Java didn't like that & I had to modify stuff.
- In another such place, I had to add try catch blocks because apparently, their code wasn't handling exceptions properly.
- The last method I was implementing & got errors in was the ``handleGIFTMessage()`` method. I commented this method for now, but will try & fix this too.
- As for the tutor module startup, I found [this](https://gifttutoring.org/projects/gift/wiki/Developer_Guide_2023-1#Launching-the-Development-Mode-server). I'll try this tmrw.



## Dec 15
### Installation on Linux
1) Extract the release_2023-1 folder from GIFT_2023 zip file.
2) Download the GIFT_Linux_Dependencies.zip file from the GIFT Website's Downlods page. From this extract the openjdk-11 for Linux & place that file in the GIFT/external folder.
3) Next just run the installGIFT followed by the launchGIFT shell scripts with su permissions.






## Summary (Mar 17, 2024)
### Installation (Windows)
1) There are 2 versions of GIFT: - The GIFT downloadable code (from the public website) & the GIFT SVN version.
2) Lets first try to run the public version of GIFT:
- First run the installGIFT.bat script
- This well open a terminal and run some checks, unzip the jdk(provided by GIFT), then close this terminal
- Then a GIFT installation window will open up. This will ask you to install some virtual human character server, media semantics character server as well as another version of Python. Note that you don't have to do any of this. There'll be a checkbox to indicate that you have it installed - just tick it. You can then hit the next button & move on with the installation. (These extra installations are needed for some very specific use cases (TAs), which you can ignore for now).
- Once, the installation is done, the install window will close up.
- In the same folder as the installGIFT.bat, you'll now see a launchGIFT.bat( this wasn't there earlier).
- You run this launchGIFT.bat, and some checks will run again which will run certain checks, and then directly start GIFT up.
3) Next, in order to run the SVN version of GIFT:
- First you will notice that the GIFT/external folder is empty. In the SVN repository, you should find the third parties directory somewhere in the SVN repo.
- You need to get all the contents of this folder, and place them in GIFT/external.
- Once this is done, then you go on to run the installGIFT.bat, and everything will happen exactly like the GIFT's downloadable version.
- Next you will run the launchGIFT.bat file present in this same directory as installGIFT.bat, which will start up GIFT for you.


### Installation (Linux)
1) The GIFT downloadable folder is still the same as the one used in Windows. We will be using shell (.sh) scripts now though.
2) First, in the GIFT's downloads folder, there'll be a GIFT_Linux_Dependencies.zip file, download this. Then after unzipping this, you'll see 3 files in here.
3) Get the openjdk-11 for Linux file (it's a tar.gz file, don't un-tar it) and place this in the GIFT/external directory.
4) Next, just run the GIFT/scripts/dev-tools/Linux/installGIFT.sh shell script. ( You might want to give executable permissions to this file first by - chmod +x ./installGIFT.sh)
5) The installation steps is same as in Windows. It runs some checks for you and get you the launchGIFT.sh
6) Next run the GIFT/launchGIFT.sh shell script. ( Again give executable permissions to this file first by - chmod +x ./launchGIFT.sh)
7) Note that you have to run the **GIFT/launchGIFT.sh** and not the GIFT/scripts/dev-tools/Linux/launchGIFT.sh. 
They expect us to run the former.
