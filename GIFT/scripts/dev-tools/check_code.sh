#!/bin/bash
#
# This document is useful for checking source code for common coding issues
# examples: 
#              - using * in an import
#              - threads with no names
#              - using "collections.size() == 0" instead of "collections.isEmpty()"
#              - missing java file header comment block
#              - using '== true' or '== false' for booleans
#

src_dir="$(pwd)/../../src/"

# Check that this script was ran from the correct location
if [ ! -d "$src_dir" ];then

	echo "ERROR: Unable to find ${src_dir}, did you execute the script from within the dev-tools directory?"
	exit
fi

cd "$src_dir"

echo "Starting to check code for common issues..."

echo -e "\n-------------------------------------------"

echo -e "Checking for * imports\n"
find . -name "*.java" | xargs grep -n import |grep "*;"

echo -e "\n-------------------------------------------"

echo -e "Checking for Threads with no names\n"
find . -name "*.java" | xargs grep -n " Thread()" | grep -v "gat/client" | grep -v "dashboard/client" | grep -v "ert/client" | grep -v "tutor/client" | grep -v "wrap/client"
find . -name "*.java" | xargs grep -n  " Timer()" | grep -v "gat/client" | grep -v "dashboard/client" | grep -v "ert/client" | grep -v "tutor/client" | grep -v "wrap/client" | grep -v "widgets/bootstrap"

echo -e "\n-------------------------------------------"

#TODO: use reg expression for spaces and new lines
echo -e "Checking for size() misuse\n"
find . -name "*.java" | xargs grep -n ".size() == 0" | grep -v "gat/client" 
find . -name "*.java" | xargs grep -n ".size() > 0" | grep -v "KinectSensorDataModel.java"
find . -name "*.java" | xargs grep -n ".size() <= 0"

echo -e "\n-------------------------------------------"

echo -e "Checking for missing file header comment\n"
# There are some exceptions {Edk.java, EdkErrorCode.java, EmoState.java"}
find . -name "*.java" | xargs grep -L "LICENSE.txt file"|grep -v "Edk.java"|grep -v "EdkErrorCode.java"|grep -v "EmoState.java"|grep -v "impl/lrs"

echo -e "\n-------------------------------------------"

echo -e "Checking for '== true' and '== false'\n"
find . -name "*.java" | xargs grep -n "== true"
find . -name "*.java" | xargs grep -n "==true"
find . -name "*.java" | xargs grep -n "== false" | grep -v "ValidateMediaSemanticsHandler.java"
find . -name "*.java" | xargs grep -n "==false"  

echo -e "\n-------------------------------------------"

echo -e "Checking for String Buffer/Builder misuse of '+'\n"
 
find . -name "*.java" | xargs grep -n "append(" . |grep "+"|grep -v "++" 

echo -e "\nFinished checking code"