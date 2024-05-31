#!/bin/bash

parent_path=$( cd "$(dirname "${BASH_SOURCE[0]}")" ; pwd -P )

cd "$parent_path"

AbsBaseDir=$(cd "$parent_path/../.."; pwd)
jdkDir="${AbsBaseDir}/external/jdk-11"
JAVA_HOME="${jdkDir}"

#Check if the JDK has been extracted
if ! [ -d "${jdkDir}" ] ; then

    if ! [ -f "${jdkDirArchive}" ] ; then
    
        echo "ERROR: Unable to find ${jdkDirArchive}."
	echo "Do you have the GIFT third parties in the ${AbsBaseDir}/external folder?"
	
	exit 1
    fi
    
    cd ${AbsBaseDir}/external
    tar -xvzf ${jdkDirArchive}
    cd "$parent_path"
else
    echo "Found JDK at ${JAVA_HOME}"
fi

cd ../../

export JAVA_HOME

"${AbsBaseDir}/external/ant/bin/ant" -file "${AbsBaseDir}/scripts/dev-tools/export.xml" -Dbase.path=$1 -Doutput.path=$2
