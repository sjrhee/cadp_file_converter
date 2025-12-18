# CADP 파일 변환기 (CADP File Converter)

Thales CipherTrust Application Data Protection (CADP)를 사용하여 데이터 파일(CSV)을 암호화 및 복호화하는 고성능 커맨드라인 도구입니다.

## 필수 조건 (Prerequisites)

### 빌드 및 개발 시
- **Java 11 이상** (pom.xml 및 현재 빌드 환경 기준)
- Maven 3.6 이상
- Thales CADP 로컬/네트워크 설정

### 단독 실행 패키지 사용 시
- **자바(Java) 설치 불필요** (JRE 11이 패키지에 포함되어 있습니다)
- 리눅스(Linux) x64 환경
- Thales CADP 연동 설정 (.env 파일)

## 설치 및 빌드 (Installation & Build)

이 프로젝트는 `install.sh` 스크립트를 통해 자동으로 빌드되고 시스템(`/opt/cadp-file-converter`)에 설치됩니다.

```bash
sudo ./install.sh
```

- **설치 경로**: `/opt/cadp-file-converter`
- **실행 명령어**: `/usr/local/bin/cadp-file-converter` 가 생성되어 어디서든 실행 가능합니다.

## 단독 실행 패키지 제작 (Standalone Packaging)

다른 서버로 전달하여 즉시 실행할 수 있도록 JRE(Java Runtime Environment)와 설정 파일을 포함한 패키지를 제작할 수 있습니다.

### 패키지 빌드

```bash
mvn clean package -DskipTests
```

- **결과물**: `target/cadp-file-converter-1.0-SNAPSHOT-bin.tar.gz`
- **포함 내용**:
    - `jre/`: Java 실행 환경 (별도 자바 설치 필요 없음)
    - `.env`: 현재 설정 파일 (전달 시 포함됨)
    - `run.sh`: 원클릭 실행 스크립트
    - `lib/` 및 핵심 JAR 파일

### 다른 서버에서 실행

1. 생성된 `tar.gz` 파일을 대상 서버로 복사합니다.
2. 압축을 해제하고 `run.sh`를 실행합니다.

```bash
tar -xzvf cadp-file-converter-1.0-SNAPSHOT-bin.tar.gz
cd cadp-file-converter-1.0-SNAPSHOT
./run.sh [OPTIONS]
```

## 도커 이미지 사용 (Docker Image Usage)

도커를 사용하면 자바 설치나 복잡한 설정 없이 어디서든 즉시 실행할 수 있습니다.

### 이미지 빌드

```bash
docker build -t cadp-file-converter .
```

### GitHub Container Registry (GHCR)에서 바로 실행

이미지를 직접 빌드하지 않고, GitHub 저장소에서 바로 다운로드하여 실행할 수 있습니다.

```bash
# 최신 이미지 다운로드
docker pull ghcr.io/sjrhee/cadp-file-converter:latest
```
`.env` 파일과 데이터 파일(`input.csv` 등)이 있는 현재 디렉토리를 각각 마운트하여 실행합니다. 
컨테이너 내부에서는 `/data` 경로를 통해 파일에 접근할 수 있습니다.

```bash
docker run --rm -v $(pwd):/data -v $(pwd)/.env:/app/.env ghcr.io/sjrhee/cadp-file-converter:latest -i /data/input.csv -o /data/output.csv [OPTIONS]
```

- **옵션 예시**: `-m protect -i data.csv -o output.csv -c 1 -s`
- **입출력 파일 연결**: 로컬 디렉터리의 파일을 처리하려면 볼륨 마운트(`-v`)가 필요합니다.
  ```bash
  docker run --rm -v $(pwd):/data -v $(pwd)/.env:/app/.env cadp-file-converter -i /data/input.csv -o /data/output.csv [다른 옵션들]
  ```
### 윈도우 PowerShell 실행 예제 (Windows PowerShell Example)

윈도우 환경에서 실행 시 경로 지정 방식이 다릅니다:

```powershell
docker run --rm `
  -v "${PWD}/data:/data" `
  -v "${PWD}/env.txt:/app/.env" `
  ghcr.io/sjrhee/cadp-file-converter:latest `
  -m protect `
  -i /data/employee.csv `
  -o /data/employee_enc.csv `
  -s -c 11 -t 4
```
docker 실행 폴더에 env.txt 파일이 있어야 합니다.

## 에러 처리 및 로깅 (Error Handling & Logging)

- **로그 파일**: 권한 문제 방지를 위해 에러 로그는 **/tmp/cadp-file-converter.log** 에 저장됩니다. 디버깅 시 이 파일을 확인하십시오.
- **자동 중단 (Circuit Breaker)**: 처리 중 연속으로 **10회**의 에러(API 실패 등)가 발생하면, 대량의 로그 발생을 방지하기 위해 작업이 자동으로 중단됩니다.
- **실행 시간 측정**: 작업 시작 시간, 종료 시간, 그리고 총 소요 시간(초)이 콘솔에 출력됩니다.
- **Row 단위 에러**: 단일 행 처리 실패 시 해당 행은 건너뛰고 오류 메시지를 출력하며, 전체 작업은 계속 진행됩니다(연속 실패 한도 내에서).

## 환경 변수 (Environment Variables)

루트 디렉토리에 `.env` 파일을 생성하거나 다음 환경 변수들을 직접 설정하십시오.
편의를 위해 `cadp-file-converter --init` 명령어를 실행하면 샘플 `.env` 파일이 생성됩니다.

| 변수명 | 설명 | 예시 |
|----------|-------------|---------|
| `CADP_KMS_HOST` | CADP/NAE 서버의 IP 주소 | `192.168.0.10` |
| `CADP_KMS_PORT` | CADP/NAE 서버의 포트 | `443` |
| `CADP_REGISTRATION_TOKEN` | CADP 등록 토큰 (Registration Token) | `j96WogOWbdLPkuc....` |
| `CADP_USER_NAME` | CADP 작업을 수행할 사용자명 | `dev-user` |
| `CADP_PROTECTION_POLICY_NAME` | 기본 보호 정책 (키 이름) | `dev-users-policy` |

## 사용법 (Usage)

설치가 완료되면 `cadp-file-converter` 명령어를 어디서든 사용할 수 있습니다.

### 기본 명령어 구조

```bash
cadp-file-converter [OPTIONS]
```

### 옵션 (Options)

- `-m, --mode <mode>` : 동작 모드. `protect` (암호화) 또는 `reveal` (복호화).
- `-i, --input <file>` : 입력 CSV 파일 경로.
- `-c, --column <index>` : 처리할 컬럼 인덱스 (1부터 시작). 반복 사용 가능.
    - 사용법: `-c 3` (3번째 컬럼에 기본 정책 사용), `-c 3=policy01` (3번째 컬럼에 `policy01` 정책 사용).
- `-o, --output <file>` : 출력 CSV 파일 경로.
- `-s, --skip-header` : CSV의 첫 번째 줄(헤더)을 건너뜁니다.
- `-d, --delimiter <char>` : CSV 구분자 (기본값: `,`).
- `-t, --threads <number>` : 병렬 작업 스레드 수 (기본값: `1`).

### 예제 (Examples)

**암호화 (Protect):**
`data.csv` 파일의 1번째 컬럼(특정 정책 사용)과 3번째 컬럼(기본 정책 사용)을 암호화합니다.
`skip-header`(-s) 옵션을 사용하여 헤더 처리를 방지하십시오 (암호화 실패 방지).

```bash
cadp-file-converter -m protect -i input_data.csv -o encrypted_data.csv -c 1=credit-card-policy -c 3 -s
```

**복호화 (Reveal):**
위에서 암호화한 파일을 동일한 설정으로 복호화합니다.

```bash
cadp-file-converter -m reveal -i encrypted_data.csv -o decrypted_data.csv -c 1=credit-card-policy -c 3 -s
```

## 문제 해결 (Troubleshooting)

연결 문제가 발생하는 경우, `CADP_KMS_HOST` 설정과 방화벽 규칙이 올바른지 확인하십시오.
