# Connecting GIFT to the RIDE App

The `RIDEPluginInterface` in GIFT facilitates the integration and communication between these two systems.

## Architecture

The `RIDEPluginInterface` acts as a bridge between GIFT and RIDE, enabling bi-directional communication. It consists of two main components:

1. **TCP/IP Socket Connection**: This component establishes a direct socket connection between GIFT and RIDE, facilitating the exchange of raw data.
2. **gRPC Channel**: This component sets up a communication channel using the gRPC protocol, which is built on top of Protocol Buffers (Protobuf). It provides a more structured and efficient way of exchanging data between the two systems.

## GIFT and RIDE setup
![](https://github.com/kgary/GIFT/assets/527039/6fba3105-d298-4e7b-b538-425569e9ea4f)


## Protocols and Data Exchange

### TCP/IP Socket Connection

The TCP/IP socket connection is used for low-level data exchange between GIFT and RIDE. GIFT sends and receives raw byte data through this connection. The `RIDEPluginInterface` listens for incoming data from RIDE and processes it accordingly.

The incoming data from RIDE is expected to follow a specific format:

1. A 4-byte "message start" signature (`MESSAGE_START_BYTE_SIGNATURE`) indicates the beginning of a new message.
2. A 2-byte value specifying the event type (e.g., entity update, weapon fire, etc.).
3. A 4-byte value indicating the size of the upcoming Protobuf message.
4. The actual Protobuf message data.

The `RIDEPluginInterface` continuously reads and processes the incoming byte stream, searching for the "message start" signature and extracting the Protobuf messages.

### gRPC and Protocol Buffers

In addition to the low-level TCP/IP connection, the `RIDEPluginInterface` establishes a gRPC channel for more structured communication with RIDE. gRPC is a high-performance, open-source RPC framework that uses Protocol Buffers (Protobuf) for data serialization and transmission.

The `RIDEPluginInterface` obtains three gRPC stubs (clients) for interacting with RIDE's entity, scenario, and equipment services. These stubs allow GIFT to send requests and receive responses from RIDE in a structured manner, using Protobuf messages.

For example, GIFT can send requests to RIDE to:

- Display feedback messages in the RIDE UI
- Cause weapon malfunctions or repairs for specific entities
- Set the time of day in the RIDE scenario

RIDE, in turn, can send entity updates, weapon fire events, and other simulation data to GIFT through the gRPC channel.

## Data Flow and Integration

The communication between GIFT and RIDE is bi-directional:

1. **GIFT to RIDE**:
  - GIFT sends scenario adaptations (e.g., setting fog level, time of day) and other commands to RIDE through the gRPC channel.
  - GIFT can also display feedback messages in the RIDE UI using the gRPC channel.

2. **RIDE to GIFT**:
  - RIDE sends entity updates, weapon fire events, and other simulation data to GIFT through the TCP/IP socket connection as raw byte streams.
  - The `RIDEPluginInterface` processes these byte streams, extracts the Protobuf messages, and translates them into GIFT-specific messages (e.g., `ENTITY_STATE`, `WEAPON_FIRE`, `DETONATION`).
  - These GIFT messages are then sent to the appropriate GIFT modules for further processing and integration into the tutoring system.

By applying both the low-level TCP/IP socket connection and the structured gRPC communication, the `RIDEPluginInterface` facilitates  integration between GIFT and RIDE, enabling the exchange of simulation data, commands, and feedback in a bidirectional manner.

## Connection Establishment

1. **Socket Connection**: The `RIDEPluginInterface` initiates a socket connection with RIDE for continuous real-time data reception.
2. **gRPC Channel**: The `RIDEPluginInterface` sets up a gRPC channel with RIDE for structured command execution and data exchange using Protocol Buffers.

## Data Flow

### Sockets - Continuous Data Reception

1. **Data Transmission**: RIDE sends continuous data streams to the `RIDEPluginInterface` via the socket connection.
2. **Data Queueing and Processing**: Upon receipt, the `RIDEPluginInterface` queues and processes the incoming data to interpret the messages.
3. **Data Sending**: The processed data, such as entity states (a custom message type), are forwarded to the GatewayModule in GIFT for further handling or interaction with other GIFT modules.

### gRPC - Command Execution

1. **Command Sending**: Specific commands, like "disable weapon," are sent from the `RIDEPluginInterface` to RIDE using the gRPC channel.
2. **Acknowledgment/Response**: RIDE executes the command and sends back acknowledgments or responses via the gRPC channel.

### Event Response via gRPC

For certain operations like fetching entity details, the `RIDEPluginInterface` makes a request via the gRPC channel, to which RIDE responds with the required data.

## Data Formats

### Socket Communication

1. **Data Received by the `RIDEPluginInterface` from RIDE via Socket**:
  - **Format**: Binary data
  - **Processing**: The `RIDEPluginInterface` receives byte streams that include headers indicating the type of message and its length.

2. **Data Sent to the GatewayModule from the `RIDEPluginInterface` after Processing**:
  - **Conversion**: The binary data is parsed and potentially converted into a message enum called "EntityState."

3. **Data Sent from the `RIDEPluginInterface` to RIDE via Socket**:
  - There's no code indicating that data is being sent to RIDE using sockets.

### gRPC Communication

1. **Data Received by the `RIDEPluginInterface` from RIDE**:
  - **Format**: Protocol Buffers
  - **Processing**: Responses from gRPC calls are received as Protobuf messages, which are then used to trigger further actions.

2. **Post-processing Data Received by the `RIDEPluginInterface`**:
  - The received data does not seem to be sent to the GatewayModule. Instead, it updates the WorldStateManager, which holds simulation data received by any interop plugin (not just RIDE-specific). This data may need to be tracked for later reference or use.

3. **Data Sent from the `RIDEPluginInterface` to RIDE via gRPC**:
  - **Format**: Protocol Buffers (Protobuf)
  - **Sending Commands/Requests**: Defined service methods are called using Protobuf-defined requests, and Protobuf-defined responses are expected.

## Configuration

### GIFT Configuration

To configure GIFT to communicate with RIDE, follow these steps:

1. Open the `GIFT/config/gateway/configurations/default/interopConfig.xml` file.
2. In the `<RIDE>` tag, find the value called `RIDEHostIpAddress`.
3. Replace `RIDEHostIpAddress` with the IP address of the computer running the RIDE host. Note that the RIDE host can be run on the same computer as GIFT, but the computer's IP address must be used. A "localhost" IP address does not work.
4. Leave the default values for the `networkPort` and `grpcNetworkPort` as they are.

### RIDE Host/Client Configuration

When running a RIDE host, no configuration is necessary. Choose your scenario and click the "Start Host" button.

When running a RIDE client, after choosing your scenario, you will see two text boxes under the word "Client":

1. In the IP address text box, enter the IP address of the computer running the RIDE host. Similarly to the GIFT configuration, a RIDE client can run on the same computer as a RIDE host.
2. In the port number text box, leave the default value.

## Scripting

The RIDE course object can use the "Script" activity type in the Course Creator. It can currently be used only to disable or repair an entity's primary weapon.

The script command uses the following format:
```java
simulation disableWeaponControl isWeaponDisabled
```
Replace `simulationId` with the integer simulation ID of the entity to disable or repair. Replace `isWeaponDisabled` with true if the weapon should be disabled, or false if the weapon should be repaired.
For example:
```java
10 disableWeaponControl true
```
This command will disable the primary weapon of the entity with simulation ID 10.


## Sample Messages received
1) Message receivedby the RIDEPluginInterface on the raw Socket channel:
Below is an entity-updates list msg
```
updates {
  entity {
    entity {
      engineID: 2
      simID: 1
      name: "Opfor_01"
    }
    position {
      x: -2476988.0
      y: -4477303.5
      z: 3798626.5
    }
    rotation {
      x: -2.0761178
      y: 0.9289717
      z: 3.1415925
    }
  }
  health {
    current: 100
    max: 100
  }
}
updates {
  entity {
    entity {
      engineID: 3
      simID: 2
      name: "Opfor_02"
    }
    position {
      x: -2476978.2
      y: -4477309.0
      z: 3798626.5
    }
    rotation {
      x: -2.0761156
      y: 0.92897165
      z: 3.1415925
    }
  }
  health {
    current: 100
    max: 100
  }
}
updates {
  entity {
    entity {
      engineID: 4
      simID: 3
      name: "Opfor_03"
    }
    position {
      x: -2476959.8
      y: -4477319.5
      z: 3798626.5
    }
    rotation {
      x: -2.0761116
      y: 0.92897177
      z: 3.1415925
    }
  }
  health {
    current: 100
    max: 100
  }
}
updates {
  entity {
    entity {
      engineID: 5
      simID: 4
      name: "Opfor_04"
    }
    position {
      x: -2476947.2
      y: -4477331.5
      z: 3798620.2
    }
    rotation {
      x: -2.0761082
      y: 0.92897296
      z: 3.1415925
    }
  }
  health {
    current: 100
    max: 100
  }
}
updates {
  entity {
    entity {
      engineID: 1248
      simID: 5
      name: "Team_LFT"
    }
    position {
      x: -2477003.2
      y: -4477371.5
      z: 3798537.2
    }
    rotation {
      x: -1.3162986
      y: 0.7701898
      z: -2.5571566
    }
  }
  health {
    current: 100
    max: 100
  }
}
updates {
  entity {
    entity {
      engineID: 1497
      simID: 104
      name: "jvaida"
    }
    position {
      x: -2476986.5
      y: -4477407.0
      z: 3798506.2
    }
    rotation {
      x: -3.0077507
      y: 0.673629
      z: 2.4433157
    }
  }
  health {
    current: 100
    max: 100
  }
}
```