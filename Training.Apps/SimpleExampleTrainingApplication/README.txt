
This C# project was created to provide an example and simple training application that can communicate with the 
GIFT Gateway module interop plugin called Example Plugin Interface (GIFT/src/mil/arl/gift/gateway/interops/example/ExampleInteropPlugin.java).

-------------
Quickstart: |
-------------
Double click on the "RunApplication.bat" file to launch the program.

--------------------
About the Program: |
--------------------
The project/solution was developed using Microsoft Visual Studio C# 2010 Express.

It uses a third party library called XML-RPC.net version 2.5.0 (http://xml-rpc.net/ as of 01/23/14).

The reason behind choosing a C# project was to provide an example of communicating with GIFT in another programming 
languange (besides the already provided Java and C++ examples).

The program uses XML-RPC as the communication protocol to interact with GIFT.  This program will start a XML-RPC server and a client
using the properties in the 'application.properties' file.  Make sure the property values match the settings for the 
Example Plugin Interface instance in GIFT/config/gateway/interopConfig.xml.

The dialog presented contains 2 buttons and 2 list boxes.  When you click one of the 2 buttons, the program's XML-RPC
client will call the GIFT XML-RPC server's method and pass a string to it.  The GIFT Example Plugin Interface will
receive the request with the argument and then send a GIFT message via the Gateway module (Requires that you are running a GIFT course
that is currently in a Training Application course transition which references the Example Plugin Interface as the interop plugin to use).

The list boxes will show out going message information, as well as incoming message information.  

Check the source code for more information.