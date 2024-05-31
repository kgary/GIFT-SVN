comment "Causes VBS to release the mouse for use on the tutor";


//set the flag indicating that the tutor should be used
//this variable will be monitored by the GIFTVBSPlugin.dll
useTutorFlag = true;

//allow the GIFTVBSPlugin to detect the change in value,
//then reset the variable for use again later
sleep 0.1;
useTutorFlag = false;