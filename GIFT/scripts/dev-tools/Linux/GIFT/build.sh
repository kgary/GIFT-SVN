#!/bin/bash

buildTarget=$1

# Navigate to the appropriate working directory if this script was invoked by another script
parent_path=$( cd "$(dirname "${BASH_SOURCE[0]}")" ; pwd -P )
cd "$parent_path"

AbsBaseDir=$(pwd)

# Check to see if any spaces are in the path and, if so, throw an error.
# While GIFT's own scripts can handle spaces, some of its third party dependencies cannot.
if [[ "${AbsBaseDir}" = *" "* ]]; then
	echo
	echo "ERROR: Please remove all spaces in the GIFT path of ${AbsBaseDir}, otherwise the various GIFT scripts will not work"
	echo
	exit 1
fi

echo Executing build...

# Check for GWT library
if ! [ -d "${AbsBaseDir}/external/gwt" ] ; then
	echo Unable to find the GWT library.  Did you install/extract the GIFT third parties into the external folder?
	exit 1
fi
GWT_HOME="${AbsBaseDir}/external/gwt"

# Check for ActiveMQ library
if ! [ -d "${AbsBaseDir}/external/activemq" ] ; then
	echo Unable to find the ActiveMQ library.  Did you install/extract the GIFT third parties into the external folder?
	exit 1
fi
ACTIVEMQ_HOME="${AbsBaseDir}/external/activemq"

# Check for Ant
if ! [ -e "${AbsBaseDir}/external/ant/bin/ant" ] ; then
	echo Unable to find the Ant library.  Did you install/extract the GIFT third parties into the external folder?
	exit 1
fi

externalLibsDir="${AbsBaseDir}/external"
export JAVA_HOME="${externalLibsDir}/jdk-11"
jdkDirArchive="${externalLibsDir}/openjdk-11-linux.x64.GIFT.tar.gz"

# Check if the archive containing the JDK exists

#Check if the JDK has been extracted
if ! [ -d "${JAVA_HOME} ]" ; then
    
    if ! [ -f "${jdkDirArchive}" ] ; then
	
	echo ERROR: Unable to find "${jdkDirArchive}"
	echo Do you have the GIFT third parties in the "${externalLibsDir}" folder?
	
	exit 1
    fi

    cd "${externalLibsDir}"
    tar -xvzf "${jdkDirArchive}"
else
    echo "Found JDK at ${JAVA_HOME}"
fi

echo using "${JAVA_HOME}"

# TODO: How to check if path to GIFT is too long in Ubuntu? Is that even a thing?

cd "${AbsBaseDir}"

export PATH="${AbsBaseDir}/external/protobuf/bin:$PATH"

RELEASE(){
    echo Building release version
    
    #"./external/ant/bin/ant" $1 > build.out.txt
    "./external/ant/bin/ant" release
    
    #Store the error code returned from calling the Ant configuration file
    ERROR_CODE=$?
    
    #Check if the application launch was successful. If not, pause the console and capture 
    #user input to continue. 
    if [ ${ERROR_CODE} != 0 ] ; then 
    	# There was an error
	
	# Execute a pause if the exitOnError argument was NOT used
	if [ "$1" != "exitOnError" ] ; then
        	read -p "Press enter to continue..."
        fi
    fi
}

SPECIAL(){
    echo Using build file argument of ${buildTarget}
    
    #"./external/ant/bin/ant" ${buildTarget} > build.out.txt
    "./external/ant/bin/ant" ${buildTarget}
    
    #Store the error code returned from calling the Ant configuration file
    ERROR_CODE=$?
    
    #Check if the application launch was successful. If not, pause the console and capture 
    #user input to continue. 
    if [ ${ERROR_CODE} != 0 ] ; then 
    	# There was an error
	
	# Execute a pause if the exitOnError argument was NOT used
	if [ "${buildTarget}" != "exitOnError" ] ; then
        	read -p "Press enter to continue..."
        fi
    fi
}

if [ -z "${buildTarget}" ] ; then
	# no build argument, therefore build release version which includes javadocs
	RELEASE
else
	if [ "${buildTarget}" = "exitOnError" ] ; then
		# the argument relates to what to do at the end of this script, therefore build release version which includes javadocs
		RELEASE
	else
		SPECIAL
	fi

fi
