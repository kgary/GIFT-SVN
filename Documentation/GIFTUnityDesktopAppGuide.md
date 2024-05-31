# GIFT Unity Desktop Application Connection

## Overview

The Unity Desktop GIFT Connector is an essential class designed to facilitate communication between Unity applications and the Generalized Intelligent Framework for Tutoring (GIFT) over a TCP/IP network. This specialized connector enables Unity applications running on desktop platforms to interact with GIFT, supporting the creation of interactive and adaptive learning experiences. This guide provides insights into initiating and managing the connection between GIFT and Unity-based applications.

## Key Components

- **TCP Port**: The connector listens on TCP port 5000, ready for incoming connections from GIFT.
- **Message Queue**: Utilizes a thread-safe `ConcurrentQueue<string>` to store incoming messages for sequential processing.
- **Network Thread**: Operates on a dedicated thread (`consumerNetworkThread`) to manage network communication, ensuring the main Unity thread remains responsive.
- **Message Processing**: Efficiently processes messages from the queue, allowing Unity applications to respond to commands from GIFT.

## How It Works

![](https://lucid.app/publicSegments/view/917fc303-36be-492b-8983-e5e44564b41d/image.png)

### Initialization

The `UnityDesktopGiftConnector` initializes a network thread that listens for incoming TCP connections from GIFT on the preconfigured port (default is 5000).

### Accepting Connections

The network thread continuously listens for new connections, accepting TCP clients and reading messages from the stream to establish the communication channel.

### Handling Messages

Messages received are enqueued into `messageInbox`. A coroutine (`MessageDequeueCoroutine`) processes these messages asynchronously, ensuring smooth communication.

### Sending Messages

Messages to GIFT are serialized into JSON strings and transmitted through the TCP stream using `outputWriter`, facilitating bidirectional communication.

### Disposal

Upon completion, the connector disposes of network resources to ensure there are no loose ends.

## Integration with GIFT

### GIFT as Client

- **Role**: GIFT initiates the connection, acting as the client, establishing the communication pathway to the Unity application.
- **Behavior**: The `UnityInterface` within GIFT sets up the connection, detailing the network address and port, and manages the message exchange.
- **Management**: Controls the Unity application by sending specific commands (e.g., start/pause scenarios) and receives updates or data in return.

### Unity Application as Server

- **Role**: Acts as the receiver of the connection, waiting for GIFT's initiation.
- **Behavior**: The `UnityDesktopGiftConnector` listens for GIFT's incoming connections, accepting them to establish communication.
- **Dual Role**: Though primarily serving as a server, it also performs client-like roles by sending responses back to GIFT, thus participating actively in the dialogue.

## Practical Usage

### Setting Up

1. Ensure your Unity project targets a desktop platform.
2. Include `UnityDesktopGiftConnector.cs` in your project.
3. Reference necessary namespaces for networking.

### Initializing the Connector

```csharp
UnityDesktopGiftConnector giftConnector = new UnityDesktopGiftConnector();
StartCoroutine(giftConnector.MessageDequeueCoroutine());
```

## Interacting with GIFT

- **Receiving Messages**: Implement logic within your Unity application to process incoming messages from GIFT. This could involve UI updates, state changes, or the triggering of specific in-game events based on the instructions received from GIFT.

- **Sending Messages**: When you need to send data or notifications back to GIFT, use the `SendMessageToGift("Your message here")` method. Ensure that the messages you send are formatted according to the schema expected by GIFT for seamless integration and compatibility.

## Cleanup

When your application is closing or you need to terminate the connection with GIFT, ensure to properly dispose of the `giftConnector` to release network resources and clean up properly:

```csharp
giftConnector.Dispose();
```

## Architecture Diagram
This is the complete [architecture diagram](https://drive.google.com/file/d/1XHfqQLM1EDdGmWpwbm30DUmGg3QCB5A2/view?usp=sharing) showing communication between GIFT & the Unity Desktop TA.
