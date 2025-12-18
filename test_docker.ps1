# Docker Test Script for CADP File Converter (Windows PowerShell)
# This script tests the docker image using .env and sampleData\employee_10.csv

$IMAGE_NAME = "cadp-file-converter"
$ENV_FILE = ".env"
$INPUT_FILE = "sampleData\employee_10.csv"
$OUTPUT_FILE = "sampleData\employee_10_docker_protected.csv"
$DECRYPT_FILE = "sampleData\employee_10_docker_revealed.csv"

# Current directory for volume mounting
$PWD_WIN = Get-Location

# Check if docker image exists
$imageExists = docker images -q $IMAGE_NAME
if (-not $imageExists) {
    Write-Error "Error: Docker image '$IMAGE_NAME' not found. Please build it first:`ndocker build -t $IMAGE_NAME ."
    exit 1
}

# Check if .env exists
if (-not (Test-Path $ENV_FILE)) {
    Write-Error "Error: .env file not found."
    exit 1
}

# Check if input file exists
if (-not (Test-Path $INPUT_FILE)) {
    Write-Error "Error: Input file '$INPUT_FILE' not found."
    exit 1
}

Write-Host "--- Starting Docker Encryption (Protect) Test ---" -ForegroundColor Cyan
docker run --rm `
    -v "${PWD_WIN}:/data" `
    -v "${PWD_WIN}\${ENV_FILE}:/app/.env" `
    $IMAGE_NAME `
    -m protect `
    -i "/data/$($INPUT_FILE.Replace('\', '/'))" `
    -o "/data/$($OUTPUT_FILE.Replace('\', '/'))" `
    -c 3 -c 8 -c 11 -s -t 4

if ($LASTEXITCODE -eq 0) {
    Write-Host "Encryption SUCCESS: $OUTPUT_FILE" -ForegroundColor Green
} else {
    Write-Error "Encryption FAILED"
    exit 1
}

Write-Host ""
Write-Host "--- Starting Docker Decryption (Reveal) Test ---" -ForegroundColor Cyan
docker run --rm `
    -v "${PWD_WIN}:/data" `
    -v "${PWD_WIN}\${ENV_FILE}:/app/.env" `
    $IMAGE_NAME `
    -m reveal `
    -i "/data/$($OUTPUT_FILE.Replace('\', '/'))" `
    -o "/data/$($DECRYPT_FILE.Replace('\', '/'))" `
    -c 3 -c 8 -c 11 -s -t 4

if ($LASTEXITCODE -eq 0) {
    Write-Host "Decryption SUCCESS: $DECRYPT_FILE" -ForegroundColor Green
    Write-Host ""
    Write-Host "Comparing original and decrypted files (ignoring headers)..." -ForegroundColor Cyan
    
    # Compare files skipping the first line (header)
    $originalContent = Get-Content $INPUT_FILE | Select-Object -Skip 1
    $decryptedContent = Get-Content $DECRYPT_FILE | Select-Object -Skip 1
    
    $diff = Compare-Object $originalContent $decryptedContent
    if ($null -eq $diff) {
        Write-Host "Verification SUCCESS: Original and Decrypted data match!" -ForegroundColor Green
    } else {
        Write-Error "Verification FAILED: Data mismatch found."
        $diff | Format-Table
    }
} else {
    Write-Error "Decryption FAILED"
    exit 1
}

Write-Host ""
Write-Host "Test Completed." -ForegroundColor Green
