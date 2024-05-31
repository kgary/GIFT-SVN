# Communication between GIFT & C# TA

1) The interop plugin on intialization, first starts its own XML RPC server(listening on :10564).
2) Next, the interop plugin uses **ProcessBuilder.start()** to launch the C# Training Application(.exe).
3) Next, as the C# TA process is started, on the main thread you have the TA's GUI. The TA then starts its own XML RPC server on another thread(listening on :10565).
4) The Gateway module receives a message from activemq(which _seems_ to be sent from the Domain module). This message, based on the **message type** is sent to the corresponding interop plugin.
When the corresponding interop plugin's **handleGIFTMessage(Message message, StringBuilder errorMsg)** method receives the message, it is sent to the TA's server through an XML RPC call. 
Usually, the first message received is the  **load scenario request** . (_Atleast for the C# TA's interop plugin, the load scenario request is the first message that's sent to the interop plugin._)

5) The C# TA server then updates this message on the TA's GUI.
6) Now say, a user clicks a button on the TA GUI, then an XML RPC request is sent to the interop plugin's server.
7) The interop plugin then sends this message to "another" module in GIFT.(It is unclear for now, which module can receive this data.)


[Architecture diagram for the above](https://drive.google.com/file/d/1iza_XDS0NWfFrF_Zv4KMXMiu8gNOpQYt/view?usp=sharing)