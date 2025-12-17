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

이 명령을 실행하면 `target/` 디렉토리에 실행 가능한 jar 파일이 생성됩니다 (예: `cadp-file-converter-1.0-SNAPSHOT-jar-with-dependencies.jar`).

## 환경 변수 (Environment Variables)

루트 디렉토리에 `.env` 파일을 생성하거나 다음 환경 변수들을 직접 설정하십시오:

| 변수명 | 설명 | 기본값 |
|----------|-------------|---------|
| `CADP_API_HOST` | CADP/NAE 서버의 IP 주소 | `192.168.0.10` |
| `CADP_API_PORT` | CADP/NAE 서버의 포트 | `32082` |
| `CADP_API_TLS` | 연결 시 TLS 사용 여부 (`true`/`false`) | `false` |
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
java -jar target/cadp-file-converter-1.0.jar \
  -m protect \
  -i input_data.csv \
  -o encrypted_data.csv \
  -c 1=credit-card-policy -c 3 \
  -s
```

**복호화 (Reveal):**
위에서 암호화한 파일을 동일한 설정으로 복호화합니다.

```bash
java -jar target/cadp-file-converter-1.0.jar \
  -m reveal \
  -i encrypted_data.csv \
  -o decrypted_data.csv \
  -c 1=credit-card-policy -c 3 \
  -s
```

## 기능 및 로드맵 (Features & Roadmap)

- [x] 유연한 파일 처리를 위한 CLI
- [x] 환경 변수 설정 지원 (.env)
- [x] 멀티 스레드 처리 지원
- [x] 실제 Thales CADP Java 라이브러리 연동 (`CadpClient` 통합 완료)
- [ ] 배포를 위한 헬퍼 스크립트
- [ ] Docker 지원

## 문제 해결 (Troubleshooting)

연결 문제가 발생하는 경우, `CADP_API_HOST` 설정과 방화벽 규칙이 올바른지 확인하십시오.
