# CADP File Converter Windows Build Script

Write-Host "Starting build process..."

# Constants
$MavenVersion = "3.9.6"
$MavenUrl = "https://archive.apache.org/dist/maven/maven-3/$MavenVersion/binaries/apache-maven-$MavenVersion-bin.zip"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$ToolsDir = Join-Path $ScriptDir "tools"
$MavenInstallDir = Join-Path $ToolsDir "apache-maven-$MavenVersion"
$MavenBin = Join-Path $MavenInstallDir "bin"

# Helper function to check for command
function Test-Command ($Command) {
    return [bool](Get-Command $Command -ErrorAction SilentlyContinue)
}

# Check if Maven is globally installed
if (Test-Command "mvn") {
    Write-Host "System Maven found."
    $MavenCmd = "mvn"
} else {
    # Check for local Maven
    if (-not (Test-Path "$MavenBin\mvn.cmd")) {
        Write-Warning "Maven not found in PATH. Attempting to download portable Maven..."
        
        if (-not (Test-Path $ToolsDir)) {
            New-Item -ItemType Directory -Path $ToolsDir | Out-Null
        }

        $ZipPath = Join-Path $ToolsDir "maven.zip"
        Write-Host "Downloading Maven $MavenVersion..."
        Invoke-WebRequest -Uri $MavenUrl -OutFile $ZipPath

        Write-Host "Extracting Maven..."
        Expand-Archive -Path $ZipPath -DestinationPath $ToolsDir -Force
        Remove-Item $ZipPath
        
        if (-not (Test-Path "$MavenBin\mvn.cmd")) {
            Write-Error "Failed to install Maven. Please install manually."
            exit 1
        }
        Write-Host "Maven installed to $MavenInstallDir"
    } else {
        Write-Host "Using local Maven from $MavenInstallDir"
    }
    
    # Add to PATH for this session
    $env:PATH = "$MavenBin;$env:PATH"
    $MavenCmd = "mvn"
}

# --- JRE Setup ---
# Use OpenJDK 21 (LTS) to match user's environment
$JreVersion = "21.0.5+11"
$JreUrl = "https://aka.ms/download-jdk/microsoft-jdk-21.0.5-windows-x64.zip"
$JreDir = Join-Path $ScriptDir "jre"

if (-not (Test-Path $JreDir)) {
    Write-Host "JRE not found in $JreDir. Downloading OpenJDK 21..."
    
    $JreZipPath = Join-Path $ToolsDir "openjdk.zip"
    if (-not (Test-Path $ToolsDir)) { New-Item -ItemType Directory -Path $ToolsDir | Out-Null }
    
    try {
        Invoke-WebRequest -Uri $JreUrl -OutFile $JreZipPath
        Write-Host "Extracting JRE..."
        Expand-Archive -Path $JreZipPath -DestinationPath $ToolsDir -Force
        
        # Move extracted folder to windows/jre
        # Microsoft Build creates a folder like 'jdk-21.0.5+11'. We need to find it.
        $ExtractedJre = Get-ChildItem -Path $ToolsDir -Directory | Where-Object { $_.Name -like "jdk-*" } | Select-Object -First 1
        if ($ExtractedJre) {
            Move-Item -Path $ExtractedJre.FullName -Destination $JreDir
            Write-Host "JRE setup complete at $JreDir"
        } else {
            Write-Error "Failed to locate extracted JDK folder."
        }
        
    } catch {
        Write-Warning "Failed to download or setup JRE. Package will be created without JRE."
        Write-Warning $_.Exception.Message
    } finally {
        if (Test-Path $JreZipPath) { Remove-Item $JreZipPath -Force }
    }
} else {
    Write-Host "Using existing JRE at $JreDir"
}

# Run Maven clean package
Write-Host "Running: $MavenCmd clean package"
& $MavenCmd clean package

if ($LASTEXITCODE -eq 0) {
    Write-Host "Build successful! Artifacts are in target/" -ForegroundColor Green

    # --- ZIP Packaging ---
    Write-Host "Creating Windows distribution package..."
    
    $ProjectRoot = $ScriptDir | Split-Path -Parent
    $TargetDir = Join-Path $ProjectRoot "target"
    $DistDir = Join-Path $TargetDir "cadp-file-converter-windows"
    $ZipFile = Join-Path $TargetDir "cadp-file-converter-windows.zip"

    # Clean previous dist
    if (Test-Path $DistDir) { Remove-Item $DistDir -Recurse -Force }
    if (Test-Path $ZipFile) { Remove-Item $ZipFile -Force }

    # Create directory structure
    New-Item -ItemType Directory -Path $DistDir | Out-Null

    # 1. Copy JAR
    $JarFile = Get-ChildItem -Path $TargetDir -Filter "*.jar" | Where-Object { $_.Name -notmatch "^original-" } | Select-Object -First 1
    if ($JarFile) {
        Copy-Item $JarFile.FullName -Destination $DistDir
        Write-Host "Included: $($JarFile.Name)"
    } else {
        Write-Warning "No JAR file found to package."
    }

    # 1.1 Copy Dependencies (lib/)
    $LibDir = Join-Path $TargetDir "lib"
    if (Test-Path $LibDir) {
        $LibDest = Join-Path $DistDir "lib"
        Copy-Item -Path $LibDir -Destination $LibDest -Recurse
        Write-Host "Included: lib/"
    }

    # 2. Copy Windows Scripts
    $Scripts = @("run.ps1", "README.md")
    foreach ($Script in $Scripts) {
        $Src = Join-Path $ScriptDir $Script
        if (Test-Path $Src) {
            Copy-Item $Src -Destination $DistDir
            Write-Host "Included: $Script"
        }
    }

    # 3. Copy JRE if exists
    $JreSrc = Join-Path $ScriptDir "jre"
    if (Test-Path $JreSrc) {
        $JreDest = Join-Path $DistDir "jre"
        Write-Host "Bundling JRE..."
        Copy-Item -Path $JreSrc -Destination $JreDest -Recurse
    }

    # 4. Create ZIP
    Compress-Archive -Path "$DistDir\*" -DestinationPath $ZipFile
    Write-Host "Package created: $ZipFile" -ForegroundColor Cyan

    # Cleanup dist dir
    Remove-Item $DistDir -Recurse -Force
} else {
    Write-Error "Build failed with exit code $LASTEXITCODE"
    exit $LASTEXITCODE
}
