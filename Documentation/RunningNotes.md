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
