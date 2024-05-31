# Topics
Topics are used by GIFT to implement a pub-sub model . A message published to a topic is delivered to all subscribers. Both Publishers and subscribers are various modules in GIFT.

There are 2 types of topics available.
1) Discovery topic for module X: Used to obtain the address and status of available X modules.
2) Base topic for module X: Used by module X to communicate to other subscribers.

The following topics exist at any given time:

1) UMS Discovery:
Pub: UMS
Sub: Domain, Monitor, Tutor

2) LMS Discovery:
Pub: LMS
Sub: Tutor, Monitor, Domain, UMS (for logging)

3) Pedagological Discovery:
Pub: Ped
Sub: Monitor, Domain, UMS (for logging)

4) Tutor Discovery:
Pub: Tutor
Sub: Monitor, Domain, UMS (for logging)

5) Learner Discovery: 
Pub: Learner
Sub: Monitor, Domain, UMS (for logging)

6) Sensor Discovery: 
Pub: Sensor
Sub: Monitor, Domain, UMS (for logging)

7) Domain Discovery:
Pub: Domain
Sub: Tutor, Monitor, UMS (for logging)

8) Gateway Discovery:
Pub: Gateway
Sub: Domain, Monitor, UMS (for logging)

9) Tutor:
Pub: Tutor
Sub: Monitor, Domain
Use: Embedded apps(e.g. Unity Webgl TA) send game state messages to the tutor topic. An instance of this topic is created for each domain session( basically each course's session(as in when u run a course) is termed as a domain session) that contains an embedded training application. Each instance(of this topic) is created by the Tutor Module when an "Initialize Domain Session Request" is received and is destroyed by the Domain Module when a domain session is closed.


10) Gateway:
Pub: Gateway
Sub: Domain, Monitor, UMS (for logging)
Use: The Gateway topic is used to send GIFT SIMAN messages from interop plugins (e.g., DIS, VBS plugin).

11) Domain:
Pub: Domain
Sub: Unidentified
Use: The topic used to send out simulation messages as part of a log playback service within the Domain module.(Got this from a comment in the codebase, not sure what this means though.)


# Queues
Queues are used to send information from a single source to a single destination. Each module has 2 queues:
1) A queue with the “Inbox” suffix: (e.g., Sensor_Queue:192.168.1.113:Inbox). The Inbox queue is used as the destination to send messages to a particular module. 

2) A queue without the "Inbox" suffix: The modules themselves actually read messages from this queue.

The above data is taken from [here](https://www.gifttutoring.org/projects/gift/wiki/Interface_Control_Document_2019-1#ActiveMQ-Topics-and-Queues).
