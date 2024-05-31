Steps to add GIFT plugin to VR-Engage install:
1. Run <GIFT install location>\installGIFT.bat
2. Keep clicking "Next" until the "Training Applications" page appears
3. Click "VR-Engage" to open the VR-Engage installer interface
4. Select the location of your VR-Engage install folder using the "Browse" button
5. Click "Next" until the "Finish" button becomes enabled
6. Click "Finish"
7. Open <VR-Engage install>\appData\scripts\playerStation.lua and look for the "backendPlugins" setting. Add the following line before that setting's closing } brace:
,"VrfGiftPlugin"

Steps to add GIFT plugin to a standalone VR-Forces install:
1. Place VrfGiftPluginDIS.dll in <VR-Forces install>\plugins64\vrForces\release
2. Place VrfGiftPlugin.xml in <VR-Forces install>\appData\plugins
3. Place gift_script_executor.lua and gift_script_executor.xml in <VR-Forces install>/simulationModelSets/VR-Engage/scripts

Steps to build:
1. Ensure that all *.proto files in this branch's GIFT\config\gateway\externalApplications\VR-Engage folder are explicitly referenced in this branch's 
Training.Apps\VR-Engage\VTMAKPlugin\VTMAKPlugin\compile-cpp.bat script
2. Run the Training.Apps\VR-Engage\VTMAKPlugin\VTMAKPlugin\compile-cpp.bat script.
3. Open the Training.Apps\VR-Engage\VTMAKPlugin\VTMAKPlugin.sln solution
4. Add all generated files to the project
    1. Right click the VTMAKPlugin project
    2. Click "Add" > "Existing Item..."
    3. Select all *.pb.cc and *.pb.h files
    4. Click "Add"
5. Ensure references to VR-Forces and VR-Link files are up to date
    1. Right click teh VTMAKPlugin project
    2. Click "Properties"
    3. Navigate to "Configuration Properties" > "C/C++" > "General" > "Additional Include Directories"
    4. Ensure that the <VR-Forces Install Dir>\include value matches your install location
    5. Ensure that the <VR-Link Install Dir>\include value matches your install location (NOTE: the value "$(MAK_VRLDIR)\include" should always be valid and is preferred)