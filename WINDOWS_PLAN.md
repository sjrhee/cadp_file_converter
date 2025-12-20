# Windows Porting Plan

This document tracks the tasks required to support Windows for the CADP File Converter.

## Status
- [x] Linux: Rename `build.sh` to `build_package.sh` and update refs
- [x] Linux: Fix target permission issues
- [x] Linux: Bundle System JRE and verify

## Windows Tasks
- [ ] **Create `windows/` directory**: dedicated folder for independent environment
- [ ] **Create `windows/build_package.ps1`**: Maven wrapper for Windows build
- [ ] **Create `windows/run.ps1`**: Execution script handling JRE selection
- [ ] **Create `windows/install.ps1`**: (Optional) Installation helper
- [ ] **Create `windows/README.md`**: Windows-specific setup and usage instructions
- [ ] **Verify**: Test build and execution on a Windows machine
