#!/bin/bash

# CADP File Converter Build Script

echo "Building CADP File Converter..."
mvn clean package -DskipTests

if [ $? -eq 0 ]; then
    echo "Build successful."
    echo "You can now run the application using ./cadp-converter.sh"
else
    echo "Build failed."
    exit 1
fi
