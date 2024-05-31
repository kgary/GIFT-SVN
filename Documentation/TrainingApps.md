## Architecture of various training applications
## Mar 18 :

1) So as we know, each TA has its own interop plugin through which it communicates with GIFT. These TAs start up their own servers, for the interop plugin to call methods declared in the TA’s source code.

2) We’ve identified that all interop plugins and their corresponding ports as well as network addresses are defined in the default.interopConfig.xml file. So, in practice each interop plugin(per TA) can listen on the ports configured to the corresponding TA waiting for incoming connections. So, if you want multiple instances of a TA to communicate with the process. There are 2 methods:
a) **Multi threaded approach**:  In a multi-threaded server, each listening port runs in its own thread, allowing the main server process to simultaneously listen to incoming connections(from various TA instances) on different ports.
b) **Single Port approach**: In this approach, all TA instances(having a unique identity), communicate with the server on the same port and we distinguish the TA instance based on the message that it sends(filter by some unique id per TA).


3) We’ve looked into other Interop plugins as well and how they’re doing it. Here are some examples:

- VREngage: - GIFT communicates with the VR-Engage platform using the VREngageInterface.java, which employs TCP socket communication and Protocol Buffers (protobuf) for data exchange.
   
    The encoded protobuf message is sent over a TCP socket to the VR-Engage system.  
   
    Upon receiving a message, the ProtobufSocketHandler(GIFT internal class) reads and decodes the protobuf message from the input stream. This involves parsing the incoming data into a VrEngageMessage object, which represents the response from VR-Engage.
   
- VBS: - GIFT communicates with the VBS platform using the VBSPluginInterface.java. This file constructs certain plain strings (commands) which also employ TCP socket communication.
   
    The constructed commands are sent over a TCP socket to a plugin (which is a DLL) within VBS that is designed to interface with external applications like GIFT.
   
    For commands that require a response, the plugin collects the necessary information, formats it as needed, and sends it back to GIFT via the same socket connection. GIFT then processes this information as per its requirements.
   
    The plugin mentioned above is specifically designed for GIFT.
   
- Sudoku TA: GIFT communicates with Sudoku TA through the SudokuTAPLuginInterface.java. This communication exactly resembles that in the SimpleExampleTA ‘s interop plugin. The outer class has client methods to call remote RPC methods. There’s an inner class which has server methods for the Sudoku TA to call.  Entirely similar to the SimpleExamplePLuginInterface.java
- TC3: GIFT communicates with TC3 using the TC3PluginInterface.java. This file also employs TCP socket communication for data exchange.
   
    The interface uses a java.net.ServerSocket to listen for incoming connections(TC3) on a specific port defined in the TC3 plugin configuration. This interface generates a string (command), which is then sent over a socket to TC3. Similarly, data can be received from TC3 through this socket.
   
- Unity App: GIFT communicates with a Unity Desktop App using the UnityInterface.java. Seems like we can use any Unity app with this Interface. This communication is facilitated via a TCP socket connection, allowing bidirectional data exchange between GIFT and the Unity app.
   
    It utilizes an AsyncSocketHandler (GIFT’s internal class) to manage socket connections, sending messages to, and receiving messages from the Unity app.
   
    While the VBS and TC3 plugins primarily use plain string commands for communication, UnityInterface.java makes explicit use of JSON objects for encoding messages.
   
    Sending Messages to Unity: When GIFT needs to send a message to the Unity app, UnityInterface encodes the message into JSON format and sends it through the established TCP socket connection using socketHandler.sendMessage(jsonString).
   
    Receiving Messages from Unity: Incoming messages from the Unity app, depending on their type, are either logged as an error or forwarded to GIFT for further processing.
   
    When the UnityInterface.java sends a message, this message is encoded into a format expected by the Unity app. The SDK within the Unity app receives this message, decodes it, and handles the request accordingly. If a response is required, the SDK will encode this data into the correct format and send it back through the established socket connection.

3) We've also noticed that there are only a few common methods between all the interop plugins. One is the handleGiftMessage(). They all interpret the SIMAN messages received from GIFT and based on this, a command is sent to the corresponding TA. The other is the configure() method, used to initialize connections to TA.
And some other minor methods like cleanup().

4) Lastly - as to how GIFT communicates with the InteropPlugins - it seems like these are just in-memory calls happening. We’ve come to this conclusion because of the way the handleGIFTMessages() function is designed, there’s no serialization/deserialization happening. Just directly getting SIMAN messages to perform any sort of actions. Whereas in all other communication-related methods, there’s some sort of data serialization/deserialization happening.


## Summary (Mar 24, 2024)
1) There are multiple ways in which various TAs communicate with their corresponding interop plugins on GIFT. They are:
a) XML -RPC
b) TCP/IP sockets: VR Engage, VBS, Sudoku TA, TC3, Any unity app, 

2) Also there are multiple identified ways for multiple TA instances to communicate with 1 single interop plugin. They are:
- Multi threaded approach
- Single Port approach
More details are mentioned in the detailed description above in (2)