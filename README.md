# CADP 파일 변환기 (CADP File Converter)

Thales CipherTrust Application Data Protection (CADP)를 사용하여 데이터 파일(CSV)을 암호화 및 복호화하는 고성능 커맨드라인 도구입니다.

## 필수 조건 (Prerequisites)

- Java 17 이상
- Maven 3.6 이상
- Thales CADP 로컬/네트워크 설정 (실제 키 관리 연동을 위해 필요)

## 빌드 (Build)

```bash
mvn clean package
```

이 명령을 실행하면 `target/` 디렉토리에 실행 가능한 JAR 파일(`cadp-file-converter-1.0-SNAPSHOT.jar`)과 `lib/` 폴더(의존성 라이브러리)가 생성됩니다.

**주의**: 실행 시 `lib/` 폴더가 JAR 파일과 동일한 디렉토리(또는 상위/지정 경로)에 존재해야 합니다.

## 에러 처리 및 로깅 (Error Handling & Logging)

- **자동 중단 (Circuit Breaker)**: 처리 중 연속으로 **10회**의 에러(API 실패 등)가 발생하면, 대량의 로그 발생을 방지하기 위해 작업이 자동으로 중단됩니다.
- **실행 시간 측정**: 작업 시작 시간, 종료 시간, 그리고 총 소요 시간(초)이 콘솔에 출력됩니다.
- **Row 단위 에러**: 단일 행 처리 실패 시 해당 행은 건너뛰고 오류 메시지를 출력하며, 전체 작업은 계속 진행됩니다(연속 실패 한도 내에서).

## 환경 변수 (Environment Variables)

루트 디렉토리에 `.env` 파일을 생성하거나 다음 환경 변수들을 직접 설정하십시오:

| 변수명 | 설명 | 기본값 |
|----------|-------------|---------|
| `CADP_API_HOST` | CADP/NAE 서버의 IP 주소 | `192.168.0.10` |
| `CADP_API_PORT` | CADP/NAE 서버의 포트 | `32082` |
| `CADP_REGISTRATION_TOKEN` | CADP 등록 토큰 (Registration Token) | (필수) |
| `CADP_USER_NAME` | CADP 작업을 수행할 사용자명 | (필수) |
| `CADP_PROTECTION_POLICY_NAME` | 기본 보호 정책 (키 이름) | `dev-users-policy` |

## 사용법 (Usage)

### 기본 명령어 구조

```bash
java -jar target/cadp-file-converter-*.jar [OPTIONS]
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

```bash
java -jar target/cadp-file-converter-1.0-SNAPSHOT.jar -m protect -i input_data.csv -o encrypted_data.csv -c 1=credit-card-policy -c 3 -s
```

**복호화 (Reveal):**
위에서 암호화한 파일을 동일한 설정으로 복호화합니다.

```bash
java -jar target/cadp-file-converter-1.0-SNAPSHOT.jar -m reveal -i encrypted_data.csv -o decrypted_data.csv -c 1=credit-card-policy -c 3 -s
```

## 문제 해결 (Troubleshooting)

연결 문제가 발생하는 경우, `CADP_API_HOST` 설정과 방화벽 규칙이 올바른지 확인하십시오.
