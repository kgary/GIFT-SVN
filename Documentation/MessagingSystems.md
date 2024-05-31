# Messaging Systems 

## Tutor Module Messaging System

The tutor modules use a complex messaging system within a software framework, designed for tutoring modules that interact with a server and potentially other modules or services. This system appears to employ a variety of message types and leverages an underlying messaging architecture that facilitates communication between different components of the system. 
The system uses an enumerated type MessageTypeEnum to define various message types that dictate the kind of action or response expected upon receipt. 
These messages encapsulate requests, commands, and notifications related to the management of tutoring sessions, user interactions, and content delivery within the tutoring system.

## Tutor Module Messaging Architecture
The underlying architecture applies a publish-subscribe (pub/sub) messaging pattern facilitated through a message queue or topic-based system. This is evidenced by:

- **SubjectUtil.TUTOR_QUEUE**: References to specific queues or topics that messages are published to or subscribed from, indicating a segmented messaging system where different queues or topics are dedicated to specific types of messages or modules.
- **Message Handling**: The handleMessage method demonstrates a dispatcher pattern, where incoming messages are inspected, and based on their type, different handlers are invoked to process the message.
- **Asynchronous Callbacks**: The use of callbacks (AsyncResponseCallback, MessageCollectionCallback) suggests that the system is designed for asynchronous communication, where responses to requests might not be immediate, but are handled through callback mechanisms once the response is available.
- **Domain Sessions**: The concept of domain sessions, managed through messages, suggests a stateful interaction pattern within the tutoring sessions. State changes are communicated through specific message types like START_DOMAIN_SESSION, CLOSE_DOMAIN_SESSION_REQUEST, etc.

The messaging architecture seems to be built on a combination of message queueing, pub/sub patterns, and asynchronous processing. This architecture supports modularity, scalability, and decoupled interactions between different components of the system. The exact implementation details of the message queue or pub/sub system (e.g., MQTT, AMQP, Kafka) are not specified in the provided code.

## Tutor Module with Unity Embedded Training Application Messaging

- **GiftConnection GameObject**: An invisible GameObject in Unity that facilitates receiving messages from GIFT. It represents the communication link between the Unity application and GIFT.

- **GIFT Unity SDK**: A set of prebuilt C# classes, Unity scripts, and Unity prefabs that simplify the process of adding GIFT functionality to a Unity application. The SDK handles the serialization and deserialization of messages, making it easier to send and receive messages.

- **Message Handling**: The Unity application uses handlers to process incoming messages from GIFT (e.g., SIMAN and Feedback messages) and to send appropriate responses or state updates back to GIFT.

- **Window.postMessage JavaScript API**: Used to send messages to the embedded Unity application from the Tutor client running in a web browser. This allows for communication between the GIFT Tutor Module (server-side) and the Unity application (client-side).

- **Unity’s API**: Within the Unity application, messages received from the Tutor client via JavaScript are passed into the Unity engine using Unity's API, where they are processed by the GIFT Unity SDK and corresponding event handlers.

- **Pub/Sub Messaging Pattern**: The architecture suggests a publish-subscribe pattern where GIFT publishes messages (commands, requests, feedback) that are subscribed to and handled by the Unity application. Conversely, the Unity application can publish state updates or responses that GIFT subscribes to and processes accordingly.

The format of the messages exchanged between Unity applications and GIFT, particularly as described in the context of using the GIFT Unity SDK, is JSON.

A typical message exchanged between a Unity application and GIFT consists of two main components: type and payload.

- **type**: A string that specifies the type of the message. This helps the receiving side (either GIFT or the Unity application) to understand how to process the message. For example, the type might indicate whether the message is a SIMAN command (for controlling the lifecycle of the application), a feedback message, or a state update from the Unity application.

- **payload**: Contains the actual data of the message. The structure of the payload can vary depending on the type of message. It is a JSON object that can include various fields and nested objects.

Sending and receiving messages:
- **From Unity to GIFT**: When the Unity application needs to send a message to GIFT, it constructs a JSON message with the appropriate type and payload, serializes it, and sends it through the established communication channel. For example, to indicate that a button was pressed within the Unity application, it might send a message with a custom state update.

- **From GIFT to Unity**: GIFT sends JSON messages to the Unity application to control its behavior or to deliver instructional content. The Unity application listens for incoming messages, deserializes them from JSON, and then processes them according to their type and payload.


Example message:
  ```json
{
  "type": "SIMAN",
  "payload": {
    "Siman_Type": "Start",
    "RouteType": "Embedded",
    "FileSize": 0
  }
}
```


## Tutor Module Message Types
Below are given all of the message types that are used inside the Tutor Module.

### `KILL_MODULE`
Terminates a module's operation, typically for shutdown or restart purposes.

&nbsp;

### `START_DOMAIN_SESSION`
Initiates a session within a specific domain, setting the stage for user interaction.

&nbsp;

### `INITIALIZE_DOMAIN_SESSION_REQUEST`
Prepares the necessary resources and settings for a new domain session to begin.

&nbsp;

### `CLOSE_DOMAIN_SESSION_REQUEST`
Ends an active domain session, possibly involving saving progress and releasing resources.

&nbsp;

### `DISPLAY_CONTENT_TUTOR_REQUEST`
Requests the display of specific educational content to the learner.

&nbsp;

### `DISPLAY_FEEDBACK_TUTOR_REQUEST`
Asks the tutor system to present tailored feedback based on the learner's progress or performance.

&nbsp;

### `DISPLAY_SURVEY_TUTOR_REQUEST`
Displays a survey to collect learner feedback on the educational material or experience.

&nbsp;

### `DISPLAY_TEAM_SESSIONS`
Shows information about team sessions, including meetings or collaborative activities.

&nbsp;

### `DISPLAY_LESSON_MATERIAL_TUTOR_REQUEST`
Requests the presentation of specific lesson materials, ranging from text to interactive simulations.

&nbsp;

### `DISPLAY_MIDLESSON_MEDIA_TUTOR_REQUEST`
Requests the display of media, like videos or animations, to supplement the lesson material.

&nbsp;

### `DISPLAY_AAR_TUTOR_REQUEST`
Requests an After Action Review panel for debriefing and reflection after learning activities.

&nbsp;

### `LESSON_STARTED`
Signals the beginning of a lesson, activating necessary resources and notifications.

&nbsp;

### `LESSON_COMPLETED`
Indicates the end of a lesson, involving tasks like recording completion time and assessing performance.

&nbsp;

### `DISPLAY_LEARNER_ACTIONS_TUTOR_REQUEST`
Shows the actions taken by a learner during a lesson, including submissions and page visits.

&nbsp;

### `ACTIVE_USER_SESSIONS_REQUEST`
Seeks information on currently active user sessions for monitoring or support purposes.

&nbsp;

### `DISPLAY_CHAT_WINDOW_REQUEST`
Opens a chat window for real-time communication among learners or between learners and tutors.

&nbsp;

### `DISPLAY_CHAT_WINDOW_UPDATE_REQUEST`
Updates an existing chat window with new messages or changes in chat participants.

&nbsp;

### `DISPLAY_COURSE_INIT_INSTRUCTIONS_REQUEST`
Displays initial instructions or setup steps at the start of a course or learning module.

&nbsp;

### `SUBJECT_CREATED`
Notifies the creation of a new subject or course topic, updating available materials or catalogs.

&nbsp;

### `TRAINING_APP_SURVEY_RESPONSE`
Handles the collection and processing of responses submitted through a training app survey.

&nbsp;

### `TRAINING_APP_SURVEY_SUBMIT`
Indicates the completion and submission of a survey within a training application.

&nbsp;

### `LOAD_PROGRESS`
Loads a user's progress in a course or module, allowing for seamless continuation of learning activities.

&nbsp;

### `INIT_EMBEDDED_CONNECTIONS`
Starts up embedded connections within the system for integrations with other applications or services.

&nbsp;

### `SIMAN`
Relates to simulation management commands or data within the system.

&nbsp;

### `DISPLAY_FEEDBACK_EMBEDDED_REQUEST`
Requests the display of feedback directly integrated into the learning platform or content.

&nbsp;

### `VIBRATE_DEVICE_REQUEST`
Triggers a device to vibrate, typically for notifications or feedback in mobile learning apps.

&nbsp;

### `COURSE_STATE`
Communicates the current status of a course, such as whether it is active, paused, or completed.

&nbsp;

### `KNOWLEDGE_SESSION_UPDATED_REQUEST`
Requests updates to a knowledge session, possibly refreshing content or session parameters.

&nbsp;

### `KNOWLEDGE_SESSION_CREATED`
Signifies the creation of a new knowledge session, initiating learning activities and resource allocation.

&nbsp;&nbsp;

## Unity Training App Message Types:

### `Siman`
Manages the lifecycle of a training application with commands for starting, pausing, resuming, or stopping sessions.

&nbsp;

### `Feedback`
Provides feedback within the training application, addressing user actions, inputs, or decisions.

&nbsp;

### `StopFreeze`
Commands the simulation to stop or pause, essential for instructional purposes or immediate halting.

&nbsp;

### `SimpleExampleState`
Transmits a basic state within the application for illustrative purposes without complex structures.

&nbsp;

### `GenericJSONState`
Sends a state or data in a flexible JSON format, accommodating various data types and structures.

&nbsp;

### `SimanResponse`
Delivers a response from the Simulation Manager, detailing the outcome or data related to executed commands.


## C# Training App Message Types

### `SIMAN`
Handles simulation management commands such as loading scenarios, pausing, restarting, resuming, starting, and stopping simulations, enabling GIFT to synchronize with the Training Application.

**Origination**: Domain Module     
**Path**: Domain Module ->JMS -> Gateway Module -> In memory call to TA's interop plugin -> TA.   
**Destination**: Training Application. 

For all the below message types **(Load, Pause, Restart, Resume, Stop)**, the origination & destination is the same.

#### `LOAD`
Initiates the loading of a specific scenario within the Training Application, essential for beginning a new or specific instructional session.

 

#### `PAUSE`
Signals the Training Application to pause its current activity, allowing for instructional intervention or reflection without ending the session.

 

#### `RESTART`
Instructs the Training Application to restart its current scenario or activity, resetting the environment for another attempt or review.

 

#### `RESUME`
Commands the Training Application to resume its activity from a paused state, continuing the instructional session without restarting.

 

#### `START`
Triggers the beginning of the Training Application's activity or scenario, marking the start of an instructional or training session.

 

#### `STOP`
Directs the Training Application to stop its current activity or scenario, effectively ending the session or preparing for a new command.

 

### `DISPLAY_FEEDBACK_GATEWAY_REQUEST`
Facilitates the sending of instructional feedback to the Training Application, allowing for real-time or post-action review and guidance.

**Origination**: Tutor Module.   
**Path**: Tutor Module ->JMS -> Gateway Module -> In memory call to TA's interop plugin -> TA.   
**Destination**: Training Application
