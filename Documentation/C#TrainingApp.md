# C# Training App

- For the C# app, turns out its source code was in fact provided with GIFT itself. (.cs files)
- I’m currently trying to try and understand how this runs so that I could recreate it in Java.
- The entry point of the application is Program.cs → Form1.cs

Each button in the SimpleExampleTA application is used to show case a specific feature in the GIFT assessment logic.  This is merely meant to help a GIFT software developer understand the logic behind the core GIFT modules (Gateway, Domain, Learner, Pedagogical).

- Pressing "Button 1" will cause a performance assessment, followed by an update in learner state, an instructional strategy request and finally feedback presented to the learner.
- Pressing "Button 2" will cause a performance assessment, followed by an update in learner state.
- Pressing "Button 3" will cause a performance assessment that indicates the training applications 'scenario' is completed which results in the next course object in the course to be shown.

Feb 19

- I tried to run the training application along with GIFT. But the default training application had its buttons disabled for some reason and weren’t enabling.
- I then updated the source code to enable the buttons by default, compiled the code. Then clicked one of them, it throws an error, that I’m currently looking into fixing.
- Next it was mentioned that for every training application, a GIFT course must be running that references the appropriate Gateway interop plugin of which utilizes XML-RPC connection(s) configured to the same network parameters.
- I found the corresponding GIFT course, started it(all while the training app(.exe file) was running). The course threw an error and exited. The error said that it could not detect the training app running. But the GIFT config file(which references the training app) had the required parameters to detect the training app. Still looking into this issue.

Feb 22

- The previous error that I got points to the absence of a config variable. On analyzing further, it seems that the "USING_GIFT_XML_RPC_SERVER_TEST=true" line was commented out. Right above it, there was a small line asking to uncomment this line while developing/testing.
- I did that. Next after running it, I get the error which says: 'Unable to connect to the remote server'. Here it points to the XML RPC server, which was supposed to be started on localhost:10564 by GIFT itself. 
- It is slightly unclear as to how am I supposed to start this XML-RPC server.
- New findings: So this training app creates an XML RPC client which then communicates with the XML RPC server created by GIFT. This app also creates an XML RPC server, which is used by the GIFT XML RPC client to connect to. So this establishes a 2 way communication between GIFT & the training app.
- On running the GIFT course (on the local GIFT Web page), it throws an error, which basically says that the GIFT XML-RPC client wasn't able to reach the TA XML-RPC server.
- On compiling the C# TA source-code, I get the app window. On clicking the button over there(after enabling the buttons), it throws the error mentioned above(on the first line of this day's updates). This says that the TA XML-RPC client wasn't able to find the GIFT XML-RPC server.
- Essentially on both sides, the servers are either not starting or not responding.


Feb 29

- So turns out in order to run any TA, you need to start the course. Note that the TA should not be running while you do that.
- Because if you start the TA on your own (by runing the .exe or running the code), then it would fail as it turns out the course will go ahead and start the TA for you.
- Now, we can see communication between GIFT and the TA. Next steps would be to try and understand the communication & build our own app to do so.


Mar 09
- Trying to understand the communication between GIFT & the Simple Example TA & rebuilding our own TA.
-  In [here](https://www.gifttutoring.org/projects/gift/wiki/Developer_Guide_2022-1#Creating-a-new-Control-Configuration-java-file), it asks us to copy the content from VBSControlConfig.java file, but I don't find this file anywhere. Skipping this section. Moving on to creating a new plugin interface.
- All plugin interface classes must extend the **AbstractInteropInterface.java** class and ofcourse implement the abstract methods.
- Followed all the steps till [here](https://www.gifttutoring.org/projects/gift/wiki/Developer_Guide_2022-1#Configure-new-Interop-course-inputs)
-  Beyond this point, it asks us to do the following things:
    - Create a game state common class
    - Create a new MessageTypeEnum
    - Create a codec class for the messages to encode/decode Java objects into JSON. (In the latest update, JSON has been replaced by Protobuf, so the file references provided in this step and the steps mentioned are of no use any longer. The documentation isn't updated here.)
    - Register the codec class for message type.
- It goes on to say that for all the 4 steps above, we can choose to skip them and use the corresponding files which were written for the SimpleExampleTraingApp.
- Thus, since the steps for protobuf encoding/decoding aren't mentioned, I chose to use the default available files.
- Next, it asks us to compile the changes made by running the build.bat ( Note to self: Every time any piece of code is changed inside **src/mil/arl/gift** , the code needs to be recompiled.)
- However, the build failed. On debugging, I found out that the errors came from the MyPluginInterface.java. I had to make some additional changes to fix it.
- The extra changes were: 
    - Since, the MyPluginInterface extends the AbstractInteropInterface, ofcourse I had to implement all the mentioned functions. The documentation didn't mention anything about the implementation about these missing functions.
    - A certain argument being passed wasn't typecasted, I did that.
    - The construction of the XMLRPCClient & XMLRPCServer had to be error-handled (put in try catch).
- After implementing all the above steps, only then the build passed. 

Mar 10
- Today we start from [here](https://www.gifttutoring.org/projects/gift/wiki/Developer_Guide_2022-1#Configure-Course-to-use-Interop-Plugin).
- We have to do this step because, in order for GIFT to know when to use the Gateway module Interop-Plugin( and more importantly the TA), we need to create a GIFT course which references this plugin class.
- It asks us to run the launchCAT.bat script, but this file doesn't exist anywhere in the mentioned location.
- So, what I did is run the launchControlPanel.bat. Then go to the Authoring Tools tab. Then go to the Desktop App tab. Then click on the "CAT" button.
- This opens up the Course Authoring tool (CAT).
- Followed the steps in the documentation on how to author a course and it generated a **MyPluginTest.course.xml** file.
- Need to create a folder with the name as your course name under **Domain/workspace/{username}**. In this folder add your generated .course.xml file. Also add the simplest.dkf.xml file that you referenced in there. Important- Make sure the name of your folder exactly matches the name of your course as declared in your .course.xml file including the presence of any spaces, otherwise it'll throw an error.
- The documentation doesn't mention this, but every transition requires a name, inside the below tag:
`<trasitionName>Transition 1</transitionName>`
If this isn't done, then an error is thrown, saying : _The course object cannot be null._
