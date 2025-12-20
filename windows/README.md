# CADP File Converter for Windows

## Prerequisites
- **Java Development Kit (JDK) 11** or higher.
- **Maven** (optional, wrapper script uses system maven).
- **PowerShell**.

## Structure
- `build_package.ps1`: Builds the project using Maven.
- `run.ps1`: Runs the built JAR file.
- `install.ps1`: (Optional) Setup script.

## Getting Started

1. **Build the project**:
   Open PowerShell and navigate to the `windows` directory or run from root:
   ```powershell
   .\windows\build_package.ps1
   ```
   This will create the JAR file in the `target/` directory.

2. **Run the application**:
   ```powershell
   .\windows\run.ps1
   ```

## Troubleshooting
- If build fails, ensure Maven is in your PATH (`mvn -version`).
- If run fails, ensure Java is in your PATH (`java -version`).
