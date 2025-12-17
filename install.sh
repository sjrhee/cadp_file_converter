#!/bin/bash

# CADP File Converter Installation Script
# This script installs the application to /opt/cadp-file-converter
# and creates a command 'cadp-file-converter' in /usr/local/bin.

APP_NAME="cadp-file-converter"
INSTALL_DIR="/opt/$APP_NAME"
BIN_PATH="/usr/local/bin/$APP_NAME"
JAR_NAME="cadp-file-converter-1.0-SNAPSHOT.jar"

# Check for root privileges
if [ "$(id -u)" -ne 0 ]; then 
    echo "Error: Please run as root (use: sudo ./install.sh)"
    exit 1
fi

# Ensure build.sh exists
if [ ! -f "build.sh" ]; then
    echo "Error: build.sh not found."
    exit 1
fi

echo "Step 1: Building project..."
./build.sh
if [ $? -ne 0 ]; then
    echo "Build failed. Aborting installation."
    exit 1
fi

echo "Step 2: Installing files to $INSTALL_DIR..."
# Create directory
mkdir -p "$INSTALL_DIR/lib"

# Copy JAR
if [ -f "target/$JAR_NAME" ]; then
    cp "target/$JAR_NAME" "$INSTALL_DIR/"
else
    echo "Error: Target JAR not found."
    exit 1
fi

# Copy Libraries
if [ -d "target/lib" ]; then
    cp -r target/lib/* "$INSTALL_DIR/lib/"
else
    echo "Error: Library folder target/lib not found."
    exit 1
fi

# Handle Log4j (Redirect logs to /tmp for debugging)
# The CADP library insists on writing to lib/CADPLogs.txt.
# We redirect this to a writable temporary file.
LOG_FILE="/tmp/$APP_NAME.log"
touch "$LOG_FILE"
chmod 666 "$LOG_FILE" # Allow any user to write to the log
ln -sf "$LOG_FILE" "$INSTALL_DIR/lib/CADPLogs.txt"

# Copy .env if exists (as default system config)
if [ -f ".env" ]; then
    echo "Copying local .env to $INSTALL_DIR/.env"
    cp ".env" "$INSTALL_DIR/"
    chmod 644 "$INSTALL_DIR/.env"
fi

echo "Step 3: Creating launcher command at $BIN_PATH..."
cat > "$BIN_PATH" <<EOF
#!/bin/bash
# Launcher for CADP File Converter
# Delegates execution to the installed JAR

# Ensure Java is installed
if ! command -v java &> /dev/null; then
    echo "Error: Java is not installed or not in PATH"
    exit 1
fi

# Execute
# Note: We attempt to load .env from the installation directory if it exists,
# allowing the tool to work from any CWD without needing a local .env copy.
if [ -f "$INSTALL_DIR/.env" ]; then
    set -a
    source "$INSTALL_DIR/.env"
    set +a
fi

exec java -jar "$INSTALL_DIR/$JAR_NAME" "\$@"
EOF

# Make executable
chmod +x "$BIN_PATH"

echo "-------------------------------------------------------"
echo "Installation Complete!"
echo "You can now run the tool from anywhere using:"
echo "  $APP_NAME [options]"
echo ""
echo "Example:"
echo "  $APP_NAME -m protect -i input.csv -o output.csv -c 1 -s"
echo "-------------------------------------------------------"
