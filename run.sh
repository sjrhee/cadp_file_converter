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
# ./run.sh -m protect -i sampleData/employee.csv -o sampleData/employee_encrypted.csv \
#   -c 3=dev-policy-02 -c 8=dev-policy-02 -c 11=dev-policy-01 -s -t 8
#
# Determine the directory of the script
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
JAR_FILE="${SCRIPT_DIR}/cadp-file-converter-1.0-SNAPSHOT.jar"
JAVA_BIN="${SCRIPT_DIR}/jre/bin/java"

# Fallback to system java if bundled JRE not found
if [ ! -f "$JAVA_BIN" ]; then
    JAVA_BIN="java"
fi

# Check if JAR exists
if [ ! -f "$JAR_FILE" ]; then
    # For development environment (before packaging)
    JAR_FILE="${SCRIPT_DIR}/target/cadp-file-converter-1.0-SNAPSHOT.jar"
    if [ ! -f "$JAR_FILE" ]; then
        echo "Error: Application JAR not found."
        echo "Please run './build.sh' (or 'mvn clean package') first to build the project."
        exit 1
    fi
fi

# Execute the application with provided arguments
"$JAVA_BIN" -jar "$JAR_FILE" "$@"
exit $?
