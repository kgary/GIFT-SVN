# GIFT Communication with Unity - Comprehensive Guide

## Overview

GIFT (Generalized Intelligent Framework for Tutoring) communicates with Unity applications through a sophisticated communication architecture involving multiple layers and components. This document provides a comprehensive overview of the communication process and introduces recent enhancements to improve interaction between GIFT and Unity applications.

## Tutor Module Communication

GIFT interacts with embedded Unity applications via the Tutor Module. This module facilitates communication by sending messages from the Domain module to the embedded training applications, which are then relayed to the Tutor client. These messages control both the Tutor client's widget displays and the embedded Unity application.

## Message Transmission

Messages intended for the embedded training application are transmitted to the Tutor client and then sent to the embedded application using the `window.postMessage` JavaScript API. This targets the iframe hosting the embedded Unity application, ensuring secure and targeted message delivery.

## Unity Game Engine Processing

Upon receiving a message, the `index.html` page within the iframe forwards it to the Unity game engine via a Unity-provided API. The GIFT-Unity SDK deserializes the message and invokes the appropriate handlers defined by the Unity developer.

## Messaging Components

### Input to Unity

- **SIMAN Messages and Feedback Messages**: These JSON strings manage the lifecycle of a training application (Load, Start, Stop, Pause, Resume) and convey instructional information to the learner.

### Output from Unity

- **Response Messages**: Including `SimanResponse`, `StopFreeze`, and `SimpleExampleState`, these JSON strings inform GIFT about the learner's state or application status or trigger further actions based on learner interaction.

## Enhanced Communication Framework

![Diagram](https://i.ibb.co/CBY5q3j/Untitled.png)

### AbstractGiftConnector Framework

Serves as the core interface for communication between Unity and GIFT, simplifying message handling, state management, and event processing. It implements a state machine model for managing the lifecycle of training scenarios.

### Platform-Specific Connectors

- **UnityDesktopGiftConnector**: For desktop applications, establishing a TCP/IP connection for incoming messages from GIFT.
- **GiftConnection for WebGL**: Manages message reception from the HTML host, essential for web-based deployments.

### Message Handling

Facilitates the serialization/deserialization of messages to/from JSON, employing event-driven programming for modular application design.

### DemoAppEventHandler

Illustrates the application of the connector by handling GIFT messages and updating GIFT on simulation states.

### GiftConnectorFactory

Utilizes the Factory pattern to manage connector instantiation, ensuring a streamlined acquisition process based on the deployment platform.

## Message Flow Illustration

### From GIFT to Unity

1. GIFT sends a JSON string message.
2. The message is deserialized into a C# object within Unity.

### From Unity to GIFT

1. Unity generates and serializes a message into a JSON string.
2. This message is sent to GIFT for further processing.

## FAQ

**What happens to uncaught exceptions?**

Uncaught exceptions within the Unity application are handled by the GIFT Unity C# API, which displays errors to the learner and can prematurely end the course upon acknowledgment.

## Official Documentation Reference

For more detailed information, please visit the [GIFT Unity Embedded Application Developer Guide 2023-1](https://gifttutoring.org/projects/gift/wiki/GIFT_Unity_Embedded_Application_Developer_Guide_2023-1).
