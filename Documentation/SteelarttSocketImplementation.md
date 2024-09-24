
# GIFT-Unity Socket Communication Overview

This document provides an overview of the communication channels used between the GIFT platform and the steelartt unity application. There are two main socket channels involved in this communication:

1. **Channel for sending control messages from GIFT to Unity**
2. **Channel for sending data messages from Unity to GIFT**

## Channel 1: Sending Control Messages from GIFT to Unity

This channel is used to send control messages from the GIFT platform to the Unity application. The messages are in JSON format and contain various instructions for the Unity application to execute. This format is designed by GIFT and has not been modified by us (Steelartt-SE).

### Control Message Format

The control messages follow the structure shown below:

```json
{
  "payload": {
    "Siman_Type": "Load",
    "LoadArgs": {},
    "RouteType": "Interop",
    "CourseFolder": "runtime\\\\jvaida\\\\2024-06-28_00-37-35\\\\Steelartt",
    "FileSize": 0
  },
  "type": "Siman"
}
```

- **payload**: A JSON string containing the details of the control message.
  - **Siman_Type**: The type of simulation action to perform (e.g., "Load").
  - **LoadArgs**: Arguments for loading, usually an empty object if not needed.
  - **RouteType**: Specifies the routing type, in this case, "Interop".
  - **CourseFolder**: The folder path for the course or scenario to be loaded.
  - **FileSize**: Size of the file to be loaded, often set to 0.

- **type**: The type of the message, here it is "Siman".

### Acknowledgment for Control Messages

Upon receiving a control message, for now the unity app doesn't do anything with that message. It just sends back an acknowledgment back to GIFT. This message format has beein implemented by the GIFT developers and not the Steelartt-SE team. Although we can modify it, but we haven't done so. The format for the same is given below:

```json
{
  "Type": "SimanResponse",
  "Payload": "Load"
}
```

- **Type**: Specifies the type of the response, which is "SimanResponse".
- **Payload**: Indicates the action acknowledged by Unity, in this case, "Load".

## Channel 2: Sending Data Messages from Unity to GIFT

This channel is used for sending data messages from the Unity application back to the GIFT platform. These messages typically contain positional and rotational data for various objects in the Unity environment.

### Data Message Format

The data messages follow the structure shown below:

```json
{
  "type": "PositionalMessageBatch",
  "payload": {
    "Timestamp": "2024-06-28T03:00:06.4781066Z",
    "DataSize": 5,
    "Messages": [
      {
        "position": {
          "x": 0.0,
          "y": 0.0,
          "z": 0.0
        },
        "rotation": {
          "x": 0.0,
          "y": 0.0,
          "z": 0.0,
          "w": 1.0
        },
        "name": "Avatars",
        "parentIndex": -1
      },
      {
        "position": {
          "x": 0.0,
          "y": 0.0,
          "z": 0.0
        },
        "rotation": {
          "x": 0.0,
          "y": 0.0,
          "z": 0.0,
          "w": 1.0
        },
        "name": "AvatarOffset-9af70101-7503-740d-b7d8-679d000001c1",
        "parentIndex": 0
      }
      .......
    ]
  }
}  
      

```

- **type**: The type of the message, here it is "PositionalMessageBatch".
- **payload**: A JSON string containing the positional data.
  - **Timestamp**: The timestamp of when the data was recorded.
  - **DataSize**: The number of positional data objects in the message.
  - **Messages**: An array of JSON strings, each representing the state of an object.
    - **position**: The position of the object in 3D space (x, y, z).
    - **rotation**: The rotation of the object in quaternion (x, y, z, w).
    - **name**: The name of the object.
    - **parentIndex**: The index of the parent object, -1 if it has no parent.



## Note
There is an important observation to note here. While the data size for each batch of positional messages is variant, the data message when sent by Unity is broken down into multiple packets. However, it seems like it is being assembled back on the other end because GIFT logs just one request.