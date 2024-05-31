# Defines all the ports used in/with GIFT

1) GIFT Admin Server: 8080, HTTP
2) Tutor Module Server: 8090, HTTP
3) ActiveMQ Web Console: 8161, HTTP
4) ActiveMQ message broker: 61617, TCP 
4) Derby Database: 1527, TCP (On a different process)
5) Simple Example Training Application Interface: 10565(TA xml rpc server), 10564(GIFT xml rpc server)	,TCP
6) JMX connectivity to the ActiveMQ broker: 7020, TCP
7) Derby DB used by GIFT: 1527, TCP
8) Domain Content Server: 8885, TCP
9) TC3 training application: 11002, TCP
10) SCATT training application: 27000, TCP
11) Single Process Launcher debugging port: 50015, TCP
12) Unknown: 63866, TCP
13) Unknown processes listening on activemq's 61617 port: (almost all ports from 63879 to 64029, there are quite a few ports missing in here though), TCP - Bidirectional communication
14) Unknown processes listening on Derby DB's 1527 port: all ports from 64101 to 64109, TCP - Bidirectional communication
15) JMX Port for GIFT to listen on : 7015, TCP
16) JMX port for ActiveMQ broker to listen on: 7020, TCP
17) Domain Conten Server: 8885, TCP
18) Unknown: 51829, UDP


## Summary (Mar 17, 2024)
1) It can be clearly seen that certain ports are unidentified currently.
2) Currently, we're not in the process of identifying the ports as other tasks take higher priority.
3)This also doesn't have all ports used by all TAs interop plugins.
4) Hence, this Portmap is declared incomplete.
