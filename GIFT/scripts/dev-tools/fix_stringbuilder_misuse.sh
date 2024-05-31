#!/bin/bash
#
# This document is useful for checking source code for String Buffer/Builder
# misuses of '+' and attempting to fix them. This is not guaranteed to fix
# every instance it finds. It's advised to run the check_code.sh script
# to find instances that were missed or not fixed. Please ensure the project
# builds successfully after running this script.
#
# Cases that this script will not fix include:
#   - Lines containing a forward slash character
#   - String Buffer/Builders that span multiple lines
#

src_dir="$(pwd)/../../src/"

# Check that this script was ran from the correct location
if [ ! -d "$src_dir" ];then

	echo "ERROR: Unable to find ${src_dir}, did you execute the script from within the dev-tools directory?"
	exit
fi

cd "$src_dir"

echo -e "Checking for String Buffer/Builder misuse of '+'\n"

# Preserves whitespaces
IFS='%'

#TODO: Print line number for file
# Search all java files for lines with "append(.*+" and fix it if it's a misuse
find . -name "*.java" | while read file; do
  egrep "append\(.*\+" $file | while read -r line; do
		string=$line
    # Flag if a line was fixed
    fix=0
		# Used to keep track of matching parenthesis
		parenthesis=0
		# Used to keep track of the index of matched +
		plusIndex=0

    # Iterate through each character except if it's the last character
    # Since we don't want to end the line with ").append("
		for ((i=0; i<${#string}-1; i++)); do
			char=${string:$i:1}
			if [ "$char" == "(" ]; then
					((parenthesis++))
			elif  [ "$char" == ")" ]; then
					((parenthesis--))
			fi
			if [ "$char" == "+" ]; then
				((plusIndex++))
			fi
      
      # TODO: Find instances of lines ending with '+' or '"' and inform
      # user that line was not fixed.

			# Only replace + if it's within the first enclosing parenthesis
			if [ $parenthesis -eq "1" ] && [ "$char" == "+" ]; then
        fix=1
				string=$(sed "s|\s*+\s*|\).append\(|$plusIndex" <<< $string)
				# Decrement plusIndex since we've just replaced one instance
				((plusIndex--))
				# Decrement parenthesis since we've just closed the parenthesis for "append("
				((parenthesis--))
			fi
		done

		# Replace the instance with the new line in the file
    if [ $fix -ge "1" ]; then
  		echo "$file: $line"
  		sed -i "/$line/ s/.*/$string/" $file
    fi
	done
done

echo -e "\nFinished checking code"
