# CADP File Converter Windows Run Script

$ScriptPath = Split-Path -Parent $MyInvocation.MyCommand.Path
# Determine JAR location
# 1. Distribution mode: JAR is in the same directory as the script
$DistJar = Get-ChildItem -Path $ScriptPath -Filter "*.jar" -ErrorAction SilentlyContinue | Select-Object -First 1

if ($DistJar) {
    $JarFile = $DistJar
} else {
    # 2. Development mode: JAR is in ../target/
    $ProjectRoot = Split-Path -Parent $ScriptPath
    $TargetDir = Join-Path $ProjectRoot "target"
    
    $JarFile = Get-ChildItem -Path $TargetDir -Filter "*.jar" -ErrorAction SilentlyContinue | 
               Where-Object { $_.Name -notmatch "^original-" } | 
               Select-Object -First 1
}

if (-not $JarFile) {
    Write-Error "No executable JAR found in $ScriptPath or ../target."
    Write-Error "If running from source, please build first: .\windows\build_package.ps1"
    exit 1
}

Write-Host "Starting CADP File Converter..."
Write-Host "JAR File: $($JarFile.Name)"

# Locate Java
$LocalJre = Join-Path $ScriptPath "jre\bin\java.exe"
if (Test-Path $LocalJre) {
    $JavaCmd = $LocalJre
    Write-Host "Using Bundled JRE"
} else {
    $JavaCmd = "java"
    Write-Host "Using System Java"
}

# Execute
try {
    & $JavaCmd -jar $($JarFile.FullName) $args
} catch {
    Write-Error "Failed to execute JAR. Ensure Java is installed and in your PATH."
    exit 1
}
