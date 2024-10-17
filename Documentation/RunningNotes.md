# Running Notes

## GIFT - Unity Communication

Currently, we are utilizing **a raw socket channel** for sending **control messages** from GIFT to Unity. This socket channel allows GIFT to send SIMAN control messages over to the unity environment and then receive acks for the same.

For receiving **data messages** sent from Unity to GIFT, we are using **Kafka**. Although we have a fully functional socket channel that could handle data communication, we have switched to Kafka for managing the data channel to better suit our messaging system's architecture and scalability.

---

## GIFT - Unity VR Integration Scenario

The expected setup for SteelARTT integration includes the following:

1. **Three Trainees with VR Headsets**: There will be three separate Unity instances, one for each trainee, each operating independently.
2. **One Unity Server**: These Unity instances will be connected to a single Unity server that manages their operations.
3. **One GIFT Server**: All trainees will be connected to a single GIFT server, with each trainee's Unity instance communicating with an instance of GIFT via the `<IP>:<port>` of the running GIFT instance on the host machine.

### Network Configuration:
- **Same Network Requirement**: All machines (Unity instances, Unity server, and GIFT server) must be on the same network. Otherwise, they will not be able to communicate with one another due to network access restrictions.

This setup ensures smooth communication between GIFT and Unity for each trainee in the VR environment while leveraging both socket and Kafka channels for control and data messages respectively.


### Oct 16 2024

**GIFT Side interop plugin design pattern:**

Currently for all the files in the [Steelartt interop plugin](https://github.com/kgary/GIFT-SVN/tree/better-build/GIFT/src/edu/asu/selab/steelartt/gateway/interop/Steelartt), we're using the below design patttern.

1) Firstly, we're using GIFT's provided abstract class [AbstractInteropInterface.java](https://github.com/kgary/GIFT-SVN/blob/better-build/GIFT/src/mil/arl/gift/gateway/interop/AbstractInteropInterface.java).

2) Next we've created [SteelArttInteropTemplate.java](https://github.com/kgary/GIFT-SVN/blob/better-build/GIFT/src/edu/asu/selab/steelartt/gateway/interop/Steelartt/SteelArttInteropTemplate.java), this has the basic functions overriden from AbstractInteropInterface, along with default implementations of the Raw Socket used for the control message channel(as that is not being changed for the forseeable future). This also has some  "template methods", that will me overridden by 2 other classes below.

3) Next, we've created [SteelArttSocket.java](https://github.com/kgary/GIFT-SVN/blob/better-build/GIFT/src/edu/asu/selab/steelartt/gateway/interop/Steelartt/SteelArttSocket.java), this has all the methods for the 2nd Raw socket channel(involving data messages). This also has the dataSocketHandler member variable initialized and used in various methods(some unique(local) and some overridden from SteelArttInteropTemplate).

4) Lastly, we've created [SteelArttKafka.java](https://github.com/kgary/GIFT-SVN/blob/better-build/GIFT/src/edu/asu/selab/steelartt/gateway/interop/Steelartt/SteelArttKafka.java), this has all the methods for the Kafka channel(involving data messages). This has the KafkaConsumer member variable and all other unique(local) methods for starting Kafka, stopping Kafka & consuming messages.

The class diagram for the above is here ![GIFT Interop Plugin Class Diagram](GIFT_Steelartt_Class_Diagram.png "Class Diagram")


**Unity Side interop plugin design pattern**

The class diagram for this is here ![Unity Interop Plugin Class Diagram](Unity_Steelartt_Class_Diagram.png "Class Diagram")
