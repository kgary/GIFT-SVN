This directory contains the Remote Gateway module application used for a GIFT server instance.

For more information on GIFT Server mode refer to the "GIFT Server Mode" section in the GIFT Developer Guide documentation.

In order to run your own server to host the Gateway module remotely, you will need to do the following:
configuration:
1) update the IP address references (see section below) for your IP address
2) build GIFT using the build.xml files (make sure the gateway jar is built in bin folder of this directory)
3) (automatically done if using build.xml files) manually sign the GIFT jars (run GIFT/scripts/dev-tools/signGIFTJars.bat)
hosting:
4) start ActiveMQ
5) start all GIFT modules except the GW module (if you are just testing the GW module remotely then you only have to run the domain module which has a jetty instance that host the Domain folder)
running:
6) Launch a course that requires a GW instance
7) Navigate inside the generated directory that is created after the GIFT build and extract the loadGatewayDependencies<token>.zip.
        example: loadGatewayDependenciesd2a4fcb0-fcfa-4736-92eb-4d01d3970d8c.zip
8) Launch startGIFTGateway.bat inside to start GW remotely
------------------------------------------
- IP Address references
Currently the computer hosting the Domain content is referenced in the following locations (below).  Therefore if the address changes, 
these locations need to be updated.  Then build GIFT and resign the GIFT jars.

1. GIFT/config/build.properties - change the server address to be used by the Gateway build when creating the gift_gateway_manifest.jar (it's a value placed in the Manifest file of that jar).
	example: gateway.jws.server.address=http://10.1.21.68:8885
