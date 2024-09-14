#!/bin/bash

# Directory where the scripts are located
# By default, it uses the current directory
SCRIPT_DIR="."

# The permissions to apply
# 755 allows read and execute for everyone, and also write for the owner
PERMISSIONS="755"

echo "Applying chmod $PERMISSIONS to all shell scripts in $SCRIPT_DIR"

# Find all files with .sh extension and apply chmod
find "$SCRIPT_DIR" -type f -name "*.sh" -print0 | while IFS= read -r -d '' file; do
    echo "Changing permissions for: $file"
    chmod $PERMISSIONS "$file"
done

echo "Permission change completed."