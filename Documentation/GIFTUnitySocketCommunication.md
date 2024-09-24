# Gift communication with unity TA through sockets

Before we understand the underlying raw socket communication being used, let us look into "what" data is being exchanged here.

1) Control Messages - Sent from GIFT to Unity to control the various states of the TA. Can be SIMAN messages or other ad-hoc messages.
2) Data Messages - Sent from Unity to GIFT like positional data or speech data.

Below is the exact nature of socket communication implemented. The java class used in GIFT is called **AsyncSocketHandler**:

## Overview

`AsyncSocketHandler` is a class designed to facilitate async bi-directional communication over a socket connection. It is used for scenarios where data needs to be exchanged efficiently between two endpoints, such as between GIFT and Unity in this context. The class uses separate threads to handle sending and receiving messages, ensuring that the main application remains responsive.


## Components

### Listener Thread (Reading from Socket)

- **Blocking**: The listener thread performs blocking read operations using `reader.readLine()`. It waits for data to be available or the operation to time out.
- **Timeout Handling**: Blocks for up to 5 seconds while waiting for data. If no data is received, it catches a `SocketTimeoutException`, yields briefly, and attempts to read again.
- **Non-Interfering**: Although the read operation is blocking, it runs in its own thread, allowing the main thread to perform other tasks simultaneously.

### Main Thread (Sending Messages)

- **Blocking**: The main thread performs blocking write operations using the `sendMessage` method.
- **Simultaneous Read and Write**: Simultaneous read and write operations are supported, allowing the main thread to send messages even if the listener thread is blocked.

## How It Works

### Establishing Connection

1. **Connect**: The main thread calls the `connect` method to establish the socket connection and then also spawns the listener thread.
2. **Listener Thread**: This separate thread is started to handle incoming messages. It reads from the socket's input stream in a loop.

### Sending Messages

1. **Main Thread**: The main thread calls the `sendMessage` method to send data.    
2. **Write Operation**: The method writes data to the socket's **output stream** and flushes it immediately.

### Receiving Messages

1. **Listener Thread**: The listener thread attempts to read data on the socket's input stream using `reader.readLine()`.
2. **Blocking**: The thread blocks for up to 5 seconds, waiting for data.
3. **Timeout Handling**: If a timeout occurs, the thread catches the `SocketTimeoutException`, yields, and re-attempts reading.


## Overall Design:

1. Each thread performs blocking operations in its tasks.
2. The main thread and the listener thread perform actions on the output and input streams, respectively.
3. At the socket level, the overall design achieves asynchronous communication because the blocking operations are handled by separate threads, allowing them to run concurrently.
