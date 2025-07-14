# Commit Chronicle

AI를 활용한 Git 커밋 분석 및 요약 라이브러리입니다. 커밋 히스토리를 분석하여 Pull Request 초안, 변경 로그, 커밋 요약을 자동으로 생성합니다.

## 🚀 주요 기능

- **AI 기반 커밋 분석**: OpenAI, Claude, Gemini, Perplexity, DeepSeek 지원
- **PR 초안 자동 생성**: 커밋 히스토리 기반 PR 템플릿 생성
- **다국어 지원**: 한국어, 영어, 중국어, 일본어
- **GitHub 템플릿 감지**: 기존 PR 템플릿 자동 적용
- **브랜치 검증**: main/master 브랜치 경고 및 의미있는 커밋 필터링
- **CLI 도구**: 명령줄에서 바로 사용 가능

## 📦 설치 방법

### Gradle

```kotlin
repositories {
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("com.github.hj4645:commit-chronicle:v0.1.0")
}
```

### Maven

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.hj4645</groupId>
        <artifactId>commit-chronicle</artifactId>
        <version>v0.1.0</version>
    </dependency>
</dependencies>
```

## 🛠️ 사용 방법

### CLI 사용법

#### 1. 초기 설정

```bash
java -jar commitchronicle-0.1.0.jar
```

첫 실행 시 대화형 설정이 시작됩니다:
- 언어 선택 (한국어, English, 中文, 日本語)
- AI 모델 선택 (OpenAI, Claude, Gemini, Perplexity, DeepSeek)
- API 키 입력

#### 2. 커밋 요약 생성

```bash
# 기본 설정으로 요약 (최근 7일, 최대 50개 커밋)
java -jar commitchronicle-0.1.0.jar summarize

# 옵션 지정
java -jar commitchronicle-0.1.0.jar summarize -d 14 -l 100
java -jar commitchronicle-0.1.0.jar summarize --path /path/to/repo
```

#### 3. PR 초안 생성

```bash
# 현재 브랜치의 PR 초안 생성
java -jar commitchronicle-0.1.0.jar pr

# 옵션 지정
java -jar commitchronicle-0.1.0.jar pr -d 7 -l 20
```

#### 4. 설정 관리

```bash
# 현재 설정 보기
java -jar commitchronicle-0.1.0.jar settings --show

# 설정 변경
java -jar commitchronicle-0.1.0.jar settings

# 설정 초기화
java -jar commitchronicle-0.1.0.jar settings --reset
```

### 라이브러리 사용법

```kotlin
import com.commitchronicle.git.GitAnalyzerFactory
import com.commitchronicle.ai.factory.AISummarizerFactory
import com.commitchronicle.ai.providers.openai.config.OpenAIConfig
import com.commitchronicle.ai.AIProviderType
import com.commitchronicle.language.Locale

// Git 분석기 생성
val gitAnalyzer = GitAnalyzerFactory.create("/path/to/repo")

// AI 설정
val aiConfig = OpenAIConfig(
    apiKey = "your-api-key",
    locale = Locale.KOREAN
)
val aiSummarizer = AISummarizerFactory.create(aiConfig, AIProviderType.OPENAI)

// 커밋 분석
val commits = gitAnalyzer.getCommits(7) // 최근 7일
val summary = aiSummarizer.summarize(commits)
val prDraft = aiSummarizer.generatePRDraft(commits)

println("요약: $summary")
println("PR 초안: $prDraft")
```

## 🔧 설정

설정은 `~/.commit-chronicle/config.json`에 저장됩니다:

```json
{
  "apiKey": "your-api-key",
  "providerType": "openai",
  "locale": "ko",
  "defaultDays": 7,
  "defaultLimit": 50
}
```

## 🤖 지원 AI 모델

| 제공업체 | 모델 |
|---------|------|
| OpenAI | gpt-4o, gpt-4o-mini, gpt-4-turbo |
| Claude | claude-3-5-sonnet-20241022, claude-3-5-haiku-20241022 |
| Gemini | gemini-2.0-flash-exp, gemini-1.5-flash, gemini-1.5-pro |
| Perplexity | llama-3.1-sonar-large-128k-online, llama-3.1-sonar-small-128k-online |
| DeepSeek | deepseek-chat, deepseek-coder |

## 📁 프로젝트 구조

```
commit-chronicle/
├── core/
│   ├── api/           # 인터페이스 및 모델
│   └── impl/          # 구현체
├── cli/               # CLI 도구
└── build.gradle.kts   # 빌드 설정
```

## 🌐 다국어 지원

- **한국어** (ko)
- **English** (en)
- **中文** (zh)
- **日本語** (ja)

## 🔒 보안

- API 키는 사용자 홈 디렉토리에 안전하게 저장
- JAR 파일에 민감한 정보 포함되지 않음
- 사용자별 독립적인 설정 관리

## 📄 라이센스

MIT License

## 🤝 기여하기

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📞 지원

- Issues: [GitHub Issues](https://github.com/hj4645/commit-chronicle/issues)
- Documentation: [Wiki](https://github.com/hj4645/commit-chronicle/wiki)