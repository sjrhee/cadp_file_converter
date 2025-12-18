#!/bin/bash

# Docker Test Script for CADP File Converter
# This script tests the docker image using .env and sampleData/employee_10.csv

IMAGE_NAME="cadp-file-converter"
ENV_FILE=".env"
INPUT_FILE="sampleData/employee_10.csv"
OUTPUT_FILE="sampleData/employee_10_docker_protected.csv"
DECRYPT_FILE="sampleData/employee_10_docker_revealed.csv"

# Check if docker image exists
if [[ "$(docker images -q $IMAGE_NAME 2> /dev/null)" == "" ]]; then
    echo "Error: Docker image '$IMAGE_NAME' not found. Please build it first:"
    echo "docker build -t $IMAGE_NAME ."
    exit 1
fi

# Check if .env exists
if [ ! -f "$ENV_FILE" ]; then
    echo "Error: .env file not found."
    exit 1
fi

# Check if input file exists
if [ ! -f "$INPUT_FILE" ]; then
    echo "Error: Input file '$INPUT_FILE' not found."
    exit 1
fi

echo "--- Starting Docker Encryption (Protect) Test ---"
docker run --rm \
    -v "$(pwd):/data" \
    -v "$(pwd)/$ENV_FILE:/app/.env" \
    $IMAGE_NAME \
    -m protect \
    -i "/data/$INPUT_FILE" \
    -o "/data/$OUTPUT_FILE" \
    -c 3 -c 8 -c 11 -s -t 4

if [ $? -eq 0 ]; then
    echo "Encryption SUCCESS: $OUTPUT_FILE"
else
    echo "Encryption FAILED"
    exit 1
fi

echo ""
echo "--- Starting Docker Decryption (Reveal) Test ---"
docker run --rm \
    -v "$(pwd):/data" \
    -v "$(pwd)/$ENV_FILE:/app/.env" \
    $IMAGE_NAME \
    -m reveal \
    -i "/data/$OUTPUT_FILE" \
    -o "/data/$DECRYPT_FILE" \
    -c 3 -c 8 -c 11 -s -t 4

if [ $? -eq 0 ]; then
    echo "Decryption SUCCESS: $DECRYPT_FILE"
    echo ""
    echo "Comparing original and decrypted files (skipping headers)..."
    diff <(tail -n +2 "$INPUT_FILE") <(tail -n +2 "$DECRYPT_FILE")
    if [ $? -eq 0 ]; then
        echo "Verification SUCCESS: Original and Decrypted data match!"
    else
        echo "Verification FAILED: Data mismatch found."
    fi
else
    echo "Decryption FAILED"
    exit 1
fi

echo ""
echo "Test Completed."
