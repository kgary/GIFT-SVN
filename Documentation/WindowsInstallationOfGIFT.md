# Installation (Windows)
1) There are 2 versions of GIFT: - The GIFT downloadable code (from the [public website](https://gifttutoring.org/projects/gift/files)) & the GIFT SVN version.

2) Lets first try to run the **public version** of GIFT:
- First run the installGIFT.bat script
- This well open a terminal and run some checks, unzip the jdk(provided by GIFT), then close this terminal
- Then a GIFT installation window will open up. This will ask you to install some virtual human character server, media semantics character server as well as another version of Python. You can skip all of this. There'll be a checkbox to indicate that you have it installed - just tick it. You can then hit the next button & move on with the installation. (These extra installations are needed for some very specific use cases (TAs), which you can ignore for now).
- Once, the installation is done, the install window will close up.
- In the same folder as the installGIFT.bat, you'll now see a launchGIFT.bat( this was generated after the installGIFT.bat run successfully).
- You run this launchGIFT.bat, and some checks will be run and then directly start GIFT up.


3) Next, we will try to run the SVN version of GIFT:
- First you will notice that the GIFT/external folder is empty. You can get this **external** directory from [here](https://drive.google.com/file/d/1vftcN0LvvODZungnYQjvUjL3BCnKbdI2/view)
- You need to download this zip, unzip it and place it as GIFT/external.(This folder should now contain many folders like activemq, ant etc.)
- Once this is done, then you go on to run the installGIFT.bat, and everything will happen exactly like the GIFT's public version as described in (2).
- Next you will run the launchGIFT.bat file present in this same directory as installGIFT.bat, which will start up GIFT for you.
