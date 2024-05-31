# Discussion with GIFT - Devs

## Nov 11 :

 - Generally, the bulk of the architecture has remained the same over the years, though that diagram is missing a few specific modules worth knowing about. I’ve attached a newer one that covers all of the core modules from a high-level, if that suits your needs:
 [Updated Architecture Diagram](https://slack-files.com/TBY1XTCCR-F065QGWQWPK-03cb25bc15)
 P.S: I don't think this diagram is any more useful than the previous ones we've seen.
 
 - **Is there an example you can point me to of what specifically you are looking for? Since you mentioned dependencies specifically, are you interested in Maven-like or Gradle-like configurations?** Part of the challenge with GIFT is that, as a generalizable framework, nearly everything is configurable to some degree, so if you are focused on changing something specific. In terms of the core functions of GIFT, I can point you to the following configurations that are commonly touched:

	a.`GIFT/configs/common.properties`

	- Above destination contains miscellaneous launch properties that are frequently changed by most users and affect multiple GIFT modules. These properties are less oriented toward developers and more so toward general research needs.

	b. `GIFT/scripts/util/launchProcess.xml`

	- Above destination defines the Java command lines that are used to run each of GIFT’s Java processes, as well as their Java system variables and runtime classpaths.

	c. `GIFT/build.xml`

	-  Above destination is an Ant configuration that invokes other build.xml files in GIFT/src to control how GIFT’s source code is built to construct the JARs and WARs in GIFT/bin.

	d. `GIFT/config/gateway/configurations/default.interopconfig.xml`

	- Above destination controls the launch arguments that are used to connect plugins in the Gateway module to external training software. The schema used here is defined by `GIFT/config/gateway/interopConfig.xsd.` If you had a training application with a network endpoint that GIFT needed to reach to receive game state data from the training application, these are the configurations you would modify to do that.

	e. `GIFT/config/domain`
 
	 - Controls what authoring constructs can be added to the courses that GIFT can execute. Of particular note are:
		 1. `GIFT/config/domain/course/course.xsd` - which defines the course objects that can be added to a course from a high level. This also defines the schema for “interop inputs”, which are arguments that can be passed to a Gateway plugin when a course wants to load a specific scenario. For example, the VBS training application has interop inputs to control the name of the scenario that is automatically loaded into VBS by GIFT.
		2.  `GIFT/config/domain/dkf/dkf.xsd` - which defines that elements can be added to Domain Knowledge Files (DKFs). This also defines “condition inputs”, which are arguments that can be passed to “condition classes” in the Domain module to determine which conditions within a training environment should trigger an assessment event. For example, EliminateHostilesCondition in the domain module has a set of condition inputs that let authors decide which units in a training scenario should be eliminated.

	 f. The remaining configurations are less frequently modified by most users. Generally you’d only need to touch them if you wanted to change what’s stored in GIFT’s databases, modify its pedagogical decision-making logic, or change how its server Java applications behave.

-  There are 2 main reasons for what you see with the .bat files.

	a. Many of the groups that we’ve worked with have limited control over what they can install to their computers, so we can’t assume that our users can just install things like Java or freely change their environment variables. To deal with this, we package a distributable JDK with GIFT and use the .bat files as an entry point to reach the bulk of GIFT’s Java-based logic using the JDK executables. If a specific use case calls for it, we can bypass these .bat scripts to invoke the Java commands directly.
	b. Until the last year or so, the GIFT software has always been required to be run on Windows, largely due to the fact that most of the training applications that GIFT has supported also only run on Windows. We’ve recently added support for running in Linux distributions for server-based deployments of GIFT, but the main focus is still largely on Windows machines. That said, we’ve been steadily reducing GIFT’s dependency on these .bat files as the Linux support has been fleshed out. Many .bats have been converted to Ant tasks, and the ones that haven’t generally have shell script equivalents.

-	The dependency on Windows doesn’t go as deep as you might think. The only major dependencies on Windows libraries are in the Gateway and Sensor modules, since *many of the training applications and sensors that GIFT supports rely heavily on Windows libraries and specific DLLs*. Specifically, we have sensor and gateway plugin classes that choose which dependencies they need to use, and those are loaded at runtime using Java reflection only as they are needed. Basically, if you run a course that needs PowerPoint, that relies on some DLLs in the Jacob library that will be loaded when the course initializes the PowerPoint plugin, but if a course does not use PowerPoint or any other plugins that require such DLLs, no windows-specific resources will be loaded. If you run the PowerPoint course on a non-Windows platform, the course itself simply shows an error when it would try to show the PowerPoint content and then ends the course gracefully. I think where the lines get a bit blurred is in two specific areas

	a. The java commands that we use to launch these processes include any library dependencies that  _might_  be needed. For example, the *Gateway module’s java command line includes the DLL dependencies to support Jacob for PowerPoint even if PowerPoint will not ever be used*.
	
	b. There are **2 main execution modes** for GIFT:

	- The first, which most users are familiar with is launchGIFT.bat which launches all of GIFT’s modules** and **ActiveMQ broker** under a single Java process. This muddies the waters a bit in terms of dependencies, since *we use thread classloaders to establish classpath separation for the various modules running in one process, but that doesn’t allow for the same level of separation for runtime library dependencies.* The UMS module doesn’t require any DLLs, but if it is run by launchGIFT.bat, it will still have access to those libraries because the Gateway module loads them.
	- The second is what we refer to as **power user mode**, which is used by launching the **control panel** from GIFT/scripts. Power user mode lets you run each of the modules and applications as separate processes, allowing finer control over classpaths, library dependencies, and Java system settings such as RAM allocation. We generally use power user mode for server deployments and use cases where finer control over the Java processes is needed. Going back to my previous example, the UMS module in power user mode would not have any dependency on Windows runtime libraries, since the Gateway module would only load them to its own Java process.
