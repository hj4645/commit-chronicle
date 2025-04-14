# CommitChronicle

CommitChronicle는 Git 커밋 히스토리와 AI를 활용해 Pull Request 초안 및 버전별 변경 사항 요약, 커밋 내용 등을 자동 생성하는 오픈소스 라이브러리입니다.

## 주요 기능

- 특정 기간/커밋 범위 기반 변경 사항 분석
- AI를 활용한 자연어 기반 요약 생성
- PR 초안 자동 생성
- 변경 로그 자동 생성
- 사용자 정의 템플릿 지원
- IntelliJ IDEA 플러그인 지원
- 낮은 버전에서는 기본 AI 요약 기능 사용, 높은 버전에서는 MCP 지원

## 프로젝트 구조

```
commit-chronicle/
├── core/                      # 핵심 라이브러리 (공통 인터페이스 및 기본 구현)
│   ├── src/main/kotlin/
│   │   ├── git/              # Git 분석 모듈
│   │   ├── ai/               # AI 요약 모듈 (버전별 구현 포함)
│   │   ├── template/         # 템플릿 엔진
│   │   └── model/            # 데이터 클래스
│   └── build.gradle.kts
├── mcp-extension/             # MCP 기반 고급 기능 (JDK 17+)
│   ├── src/main/kotlin/
│   │   └── ai/               # MCP 통합 모듈
│   └── build.gradle.kts
├── cli/                       # CLI 모듈 (core 의존)
├── ide-plugin-intellij/       # IntelliJ 플러그인 (core 의존)
└── settings.gradle.kts
```

## 시작하기

### 요구 사항

- JDK 8 이상 (MCP 기능의 경우 JDK 17 이상)
- OpenAI API 키

### CLI 설치 및 사용

#### Gradle로 직접 실행

```bash
./gradlew :cli:run --args="--path /path/to/repo --key YOUR_API_KEY"
```

#### CLI 명령어

```bash
# 커밋 요약 생성
java -jar commitchronicle-cli.jar --path /path/to/repo --key YOUR_API_KEY

# PR 초안 생성
java -jar commitchronicle-cli.jar pr --path /path/to/repo --key YOUR_API_KEY --title "PR 제목"

# 변경 로그 생성
java -jar commitchronicle-cli.jar changelog --path /path/to/repo --key YOUR_API_KEY
```

### IntelliJ 플러그인 설치 및 사용

1. IntelliJ IDEA에서 플러그인 설치
2. `Tools > CommitChronicle` 메뉴에서 원하는 기능 실행
3. OpenAI API 키 입력 후 실행

## 개발 참여

### 프로젝트 개발 환경 설정

```bash
git clone https://github.com/hj4645/commit-chronicle.git
cd commit-chronicle
./gradlew build
```

### 테스트 실행

```bash
./gradlew test
```

## 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다. 자세한 내용은 [LICENSE](LICENSE) 파일을 참조하세요.