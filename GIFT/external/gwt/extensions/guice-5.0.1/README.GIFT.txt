Jan 2022
#5158 - in the process of upgrading to OpenJDK 11, Guice was updated from 3.0 to 5.0.1.  This was mainly to fix illegal reflective access warnings found in 
the GAS console due to reflection calls in Guice.


PRE Jan 2022 -

This folder is a product of work done in the authoring_tool_4.0
branch to incorporate operations from GWT 2.6.1 for use in the 
Gift Authoring Tool (GAT).

The files included in this folder were selected from the
GIFT/gwt_libs.zip folder in the authoring_tool_4.0 branch
based on necessary classpath relationships and dependencies
found within the source code and build files.

The following is a list of all files from the gwt_libs.zip that 
were determined unnecessary for running GIFT and subsequently 
removed from this folder. These files were removed based on the
following criteria:
	- 	No build.xml files within GIFT contained references to the file
	- 	The Eclipse classpath did not contain a reference to the file
	-	No files referenced by the build.xml files or the Eclipse classpath
		relied upon the file (checked by researching Maven dependencies on
		the web)
	- 	After removing the file from the external folder, GIFT was still 
		able to build without errors or exceptions
	- 	After removing the file from the external folder, GIFT could still
		successfully run through 3 courses without errors or exceptions
	- 	After removing the file from the external folder, the GAT could
		still be run using the Gift Admin Server, Ant's GWT development mode,
		and Eclipse's GWT development mode
	- 	After removing the file from the external folder, the Survey 
		Authoring Service could still be used to create questions, surveys,
		and survey contexts and backup the UMS database
	- 	After removing the file from the external folder, the ERT could still
		be used to generate event reports

Files Removed:
	-	guice-assistedinject-3.0.jar
	-	guice-grapher-3.0.jar
	-	guice-jmx-3.0.jar
	-	guice-jndi-3.0.jar
	-	guice-multibindings-3.0.jar
	-	guice-persist-3.0.jar
	-	guice-spring-3.0.jar
	-	guice-struts2-plugin-3.0.jar
	-	guice-throwingproviders-3.0.jar

