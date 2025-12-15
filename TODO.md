# Project Roadmap & Tasks

## Current Status
- [x] Initial Project Setup (Maven, Structure)
- [x] Configuration Management (Dotenv, CLI Args)
- [x] CSV Parsing and Writing (Commons CSV)
- [x] Basic "Protect" and "Reveal" logic (Stubbed)
- [x] Multithreading Support

## Immediate Next Steps (To-Do)
- [ ] **Integration**: Implement real Thales CADP Java Library calls in `CadpService.java`.
- [ ] **Error Handling**: Enhance exception handling for API connection failures and malformed CSV rows.
- [ ] **Unit Tests**: Add JUnit tests for `Converter.java` and `Config.java`.
- [ ] **Logging**: Replace `System.out` with SLF4J/Log4j for proper logging.

## Future Enhancements
- [ ] Support for JSON and Fixed-Width file formats.
- [ ] Docker containerization for easy deployment.
- [ ] REST API Wrapper around the converter for real-time processing.
