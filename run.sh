#!/bin/bash

# CADP File Converter Execution Script
# Usage: ./run.sh [options]
#
# Examples:
# 1. Protect (Encrypt):
#    ./run.sh -m protect -i input.csv -o output.csv -c 1 -c 3=mypolicy -s
#
# 2. Reveal (Decrypt):
#    ./run.sh -m reveal -i input.csv -o output.csv -c 1 -c 3=mypolicy -s
#
# Options:
#   -m, --mode <mode>       protect | reveal
#   -i, --input <file>      Input file path
#   -o, --output <file>     Output file path
#   -c, --column <index>    Column index (1-based), optional =policy
#   -s, --skip-header       Skip first line
#   -t, --threads <n>       Number of threads
#

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
