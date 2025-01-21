# Steps to write new data message parser in GIFT side

1. Firstly in your interop plugin file, you should have an event handler(e.g. UnityInterface.java -> handleRawUnityMessage() ) to receive this data message from unity.

2. Once this data is received, we use the below line to call the main decoder/parser.
```final Object message = EmbeddedAppMessageEncoder.decodeForGift(line);```

3. The above line calls the GIFT\src\mil\arl\gift\net\embedded\message\codec\EmbeddedAppMessageEncoder.java\decodeForGift() function, which first gets the value of the message `type` field and then calls the corresponding codec based on its type. decodes(`codec.decode`) the payload and then calls the embeddedPayloadToGiftPayload() function(in the same file) to return the parsed object.

4. The codec.decode() function to find out which decoder to user, you first need to populate the map of codecs with your new codec. We will create the new codec in an upcoming step, but for now, just go ahead and populate the codec map. You can add something like:
```messageTypeToCodec.put(EncodedMessageType.CompetencyMessageBatch,COMPETENCY_BATCH_JSON_CODEC );```
In here, the first parameter is the enum that you will have to add next in the same file under the 'EncodedMessageType' enum.
The 2nd parameter is the main codec class that you will have to declare around L115 in the same file by just creating an object of the class you'll be creating next.

5. Now create your file in `GIFT\src\mil\arl\gift\net\embedded\message`. This would be a simple java class describing the received message's atributes as variables in your class. This is NOT the file referenced at the end of (4.)

6. Now you will also have to create the decode/encode function for your new class by creatig another file at `GIFT\src\mil\arl\gift\net\embedded\message\codec\json`.
Now this IS the file referenced at the end of (4.). And this file will also reference your main class file create in (5.).

7. Once all steps are done perfectly and there are no compilation errors, build GIFT(gateway & common module) and then launch GIFT. It should receive incoming data and parse it properly.
