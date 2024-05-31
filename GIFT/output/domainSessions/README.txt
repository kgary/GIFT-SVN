The domainSessions folder contains domain session folders.  Each domain session folder
contains any files that were created as the result of running that domain session (an instance of a course).
Files include a domain session message log, sensor files, GIFT monitor bookmarks, bookmark audio files, etc.

The logIndex.json file is used as a cache checking mechanism for After Action Review applications like Game Master.

Domain session message log files:
There are two time stamps at the beginning of each line
in the domain session log files. The first time stamp is
the elapsed time from the start of the domain session to
the Message creation time. The second time stamp is the 
elapsed time from the start of the domain session to the
disk write operation of the log message. The second time 
stamp is meant to be used as insight to how much latency
error can be expected from the time stamp on the messages
themselves