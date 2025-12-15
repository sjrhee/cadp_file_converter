# CADP File Converter

A high-performance command-line tool for encrypting and decrypting data files (CSV) using Thales CipherTrust Application Data Protection (CADP).

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Thales CADP Local/Network Configuration (for actual Key Management integration)

## Build

```bash
mvn clean package
```

This will create an executable jar in the `target/` directory (e.g., `cadp-file-converter-1.0-SNAPSHOT-jar-with-dependencies.jar`).

## Environment Variables

Create a `.env` file in the root directory or set these environment variables directly:

| Variable | Description | Default |
|----------|-------------|---------|
| `CADP_API_HOST` | The IP address of the CADP/NAE Server | `192.168.0.10` |
| `CADP_API_PORT` | The port of the CADP/NAE Server | `32082` |
| `CADP_API_TLS` | Enable TLS for the connection (`true`/`false`) | `false` |
| `CADP_REGISTRATION_TOKEN` | Registration Token for CADP | (Required) |
| `CADP_USER_NAME` | Username for CADP operations | (Required) |

## Usage

### Basic Command Structure

```bash
java -jar target/cadp-file-converter-*.jar [OPTIONS]
```

### Options

- `-m, --mode <mode>` : Operation mode. `protect` (encrypt) or `reveal` (decrypt).
- `-i, --input <file>` : Input CSV file path.
- `-o, --output <file>` : Output CSV file path.
- `-c, --config <file>` : Path to configuration file (optional).
- `--skip-header` : Skip the first row (header) of the CSV.
- `--delimiter <char>` : CSV delimiter (default: `,`).
- `--batch <size>` : Batch size for processing (default: `100`).
- `--threads <number>` : Number of parallel worker threads (default: `1`).

### Examples

**Encryption (Protect):**
Encrypt columns 1 and 3 (0-based index) of `data.csv`.

```bash
java -jar target/cadp-file-converter-1.0.jar \
  --mode protect \
  --input input_data.csv \
  --output encrypted_data.csv \
  --columns 1:credit-card-policy,3:ssn-policy \
  --skip-header
```

**Decryption (Reveal):**
Decrypt the same file.

```bash
java -jar target/cadp-file-converter-1.0.jar \
  --mode reveal \
  --input encrypted_data.csv \
  --output decrypted_data.csv \
  --columns 1:credit-card-policy,3:ssn-policy \
  --skip-header
```

## Features & roadmap

- [x] CLI for flexible file processing
- [x] Environment variable configuration
- [x] Multi-threaded processing
- [ ] Integration with real Thales CADP Java Library (currently using `CadpService` stub)
- [ ] Helper scripts for specialized deployment
- [ ] Docker support

## Troubleshooting

If you encounter connection issues, verify `CADP_API_HOST` and ensure the proper firewall rules are in place.
