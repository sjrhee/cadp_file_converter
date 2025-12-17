#!/bin/bash

# CADP File Converter Execution Script
# Usage: ./cadp-converter.sh [options]

JAR_FILE="target/cadp-file-converter-1.0-SNAPSHOT.jar"

# Check if JAR exists
if [ ! -f "$JAR_FILE" ]; then
    echo "Error: Application JAR not found."
    echo "Please run './build.sh' (or 'mvn clean package') first to build the project."
    exit 1
fi

# Execute the application with provided arguments
java -jar "$JAR_FILE" "$@"
exit $?
