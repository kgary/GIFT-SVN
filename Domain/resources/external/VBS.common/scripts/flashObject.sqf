// This script will cause an object to disappear and re-appear.
// Arguments:
//	1. URN Marking of object in the VBS scenario [Type: String, no spaces]
//  2. The number of times the object should disappear and then re-appear (minimum is 1) [Type: integer, greater than 0]
//  3. How long to wait in seconds between loops [Type: decimal, greater than 0]
//
// Author: Michael Hoffman, Dignitas Technologies, April 2019, GIFT contract
//
_entityToFlash = _this select 0;
_numOfLoops = _this select 1;
_sleepTime = _this select 2;
_originalPosition = getPos _entityToFlash;

// validate inputs
if (_numOfLoops < 1) then 
{
	_numOfLoops = 1;
};

if (_sleepTime < 0) then 
{
	_sleepTime = 0.25;
};


for "_i" from 0 to (_numOfLoops - 1) step 1 do
{
	_entityToFlash setPos[0,0]; 
	sleep _sleepTime;
	_entityToFlash setPos _originalPosition;
	sleep _sleepTime;	
};
