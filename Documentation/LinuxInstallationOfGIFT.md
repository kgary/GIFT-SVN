# Installation (Linux)
1) The GIFT downloadable folder(from the [public website](https://gifttutoring.org/projects/gift/files)) is still the same as the one used in Windows. We will be using shell (.sh) scripts to start GIFT though.
2) First, in the GIFT's downloads [URL](https://gifttutoring.org/projects/gift/files), there'll be a GIFT_Linux_Dependencies.zip file, download this. Then after unzipping this, you'll see 3 files in here.
3) Get the openjdk-11 for Linux file (it's a tar.gz file, don't un-tar it) and place this in the GIFT/external directory.
4) Next, just run the GIFT/scripts/dev-tools/Linux/installGIFT.sh shell script. ( You might want to give executable permissions to this file first by - chmod +x GIFT/scripts/dev-tools/Linux/installGIFT.sh)
5) The installation steps is same as in Windows. It runs some checks for you and get you the launchGIFT.sh
6) Next run the GIFT/launchGIFT.sh shell script. ( Again give executable permissions to this file first by - chmod +x ./launchGIFT.sh)

7) Note that you have to run the **GIFT/launchGIFT.sh** and not the GIFT/scripts/dev-tools/Linux/launchGIFT.sh. 
They expect us to run the former.

8) If you're using the GIFT SVN Version, then you just need to get the  **external** directory from [here](https://drive.google.com/file/d/1vftcN0LvvODZungnYQjvUjL3BCnKbdI2/view), which is the same as above, and then follow steps (2) to (7).