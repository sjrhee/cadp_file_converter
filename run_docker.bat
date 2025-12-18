@echo off
SETLOCAL EnableDelayedExpansion

:: Windows Batch Script for CADP File Converter Docker execution
:: Usage: run_docker.bat [OPTIONS]

SET IMAGE_NAME=cadp-file-converter
SET ENV_FILE=.env
SET CURRENT_DIR=%~dp0
:: Remove trailing backslash if present
IF "%CURRENT_DIR:~-1%"=="\" SET CURRENT_DIR=%CURRENT_DIR:~0,-1%

:: Check if docker is installed
docker --version >nul 2>&1
IF %ERRORLEVEL% NEQ 0 (
    echo Error: Docker is not installed or not in PATH.
    exit /b 1
)

:: Check if image exists
docker images -q %IMAGE_NAME% >nul 2>&1
IF %ERRORLEVEL% NEQ 0 (
    echo Error: Docker image '%IMAGE_NAME%' not found.
    echo Please build it first using: docker build -t %IMAGE_NAME% .
    exit /b 1
)

:: Check if .env exists
IF NOT EXIST "%CURRENT_DIR%\%ENV_FILE%" (
    echo Error: .env file not found in %CURRENT_DIR%.
    exit /b 1
)

:: Run the container
:: Map current directory to /data and .env to /app/.env
:: Use %* to pass all arguments to the container
docker run --rm ^
    -v "%CURRENT_DIR%:/data" ^
    -v "%CURRENT_DIR%\%ENV_FILE%:/app/.env" ^
    %IMAGE_NAME% %*

exit /b %ERRORLEVEL%
