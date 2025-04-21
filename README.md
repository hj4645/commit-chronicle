# CommitChronicle

CommitChronicle는 Git 커밋 히스토리와 AI를 활용해 Pull Request 초안 및 버전별 변경 사항 요약, 커밋 내용 등을 자동 생성하는 오픈소스 라이브러리입니다.

## 주요 기능

- 특정 기간/커밋 범위 기반 변경 사항 분석
- AI를 활용한 자연어 기반 요약 생성
- PR 초안 자동 생성
- 변경 로그 자동 생성
- 사용자 정의 템플릿 지원
- IntelliJ IDEA 플러그인 지원

## 프로젝트 구조

```
commit-chronicle/
├── core/                      # 핵심 라이브러리 (공통 인터페이스 및 기본 구현)
│   ├── src/main/kotlin/
│   │   ├── git/              # Git 분석 모듈 (JGit 활용)
│   │   ├── ai/               # AI 요약 모듈 (OpenAI API 활용)
│   │   ├── template/         # 템플릿 엔진
│   │   └── model/            # 데이터 클래스
│   └── build.gradle.kts
├── cli/                       # CLI 모듈 (core 의존)
├── ide-plugin-intellij/       # IntelliJ 플러그인 (core 의존)
└── settings.gradle.kts
```

## 시작하기

### 요구 사항

- JDK 8 이상
- Kotlin 1.4.20 이상
- OpenAI API 키

### 모듈별 호환성 안내

- **core 모듈**: Java 8 이상, Kotlin 1.4.20 이상 지원
- **cli 모듈**: Java 8 이상, Kotlin 1.4.20 이상 지원
- **ide-plugin-intellij 모듈**: Java 11 이상 지원

### CLI 설치 및 사용

#### 프로젝트 빌드

먼저 프로젝트를 빌드합니다:

```bash
./gradlew build
```

#### Gradle로 직접 실행

```bash
# 기본 요약 기능 실행
./gradlew :cli:run --args="--path /path/to/repo --key YOUR_API_KEY"
```

#### CLI 명령어 상세 옵션

```bash
# 커밋 요약 생성
./gradlew :cli:run --args="--path /path/to/repo --key YOUR_API_KEY [-d DAYS] [-l LIMIT]"

# PR 초안 생성
./gradlew :cli:run --args="pr --path /path/to/repo --key YOUR_API_KEY [-t TITLE] [--template TEMPLATE_PATH]"

# 변경 로그 생성
./gradlew :cli:run --args="changelog --path /path/to/repo --key YOUR_API_KEY [--group] [--template TEMPLATE_PATH]"
```

#### 배포 버전 실행

```bash
# 배포된 JAR 파일 생성
./gradlew :cli:shadowJar

# 생성된 JAR 실행
java -jar cli/build/libs/commitchronicle-cli-0.1.0-all.jar --path /path/to/repo --key YOUR_API_KEY
```

### IntelliJ 플러그인 설치 및 사용

1. IntelliJ IDEA에서 플러그인 빌드:
   ```bash
   ./gradlew :ide-plugin-intellij:buildPlugin
   ```

2. 빌드된 플러그인(ide-plugin-intellij/build/distributions/CommitChronicle-*.zip) 설치
3. `Tools > CommitChronicle` 메뉴에서 원하는 기능 실행
4. OpenAI API 키 입력 후 실행

## 테스트 및 모듈 검증

### 모듈별 테스트 실행

```bash
# core 모듈 테스트
./gradlew :core:test

# cli 모듈 테스트
./gradlew :cli:test
```

### 성능 테스트

대량의 커밋을 분석할 때의 성능을 테스트합니다:

```bash
./gradlew :cli:run --args="--path /path/to/large/repo --key YOUR_API_KEY --limit 1000"
```

## 프로젝트 확장 가이드

### 새로운 AI 요약 엔진 추가

1. `AISummarizer` 인터페이스 구현:

```kotlin
// core/src/main/kotlin/com/commitchronicle/ai/CustomAISummarizer.kt
package com.commitchronicle.ai

import com.commitchronicle.model.Commit

class CustomAISummarizer(private val apiKey: String) : AISummarizer {
    override suspend fun summarize(commits: List<Commit>): String {
        // 커스텀 구현
    }
    
    override suspend fun generatePRDraft(commits: List<Commit>, title: String?): String {
        // 커스텀 구현
    }
    
    override suspend fun generateChangelog(commits: List<Commit>, groupByType: Boolean): String {
        // 커스텀 구현
    }
}
```

2. `AISummarizerFactory`에 새 구현체 등록:

```kotlin
// AISummarizerFactory.kt 업데이트
fun create(apiKey: String, engine: String = "openai"): AISummarizer {
    return when (engine) {
        "custom" -> CustomAISummarizer(apiKey)
        else -> OpenAISummarizer(apiKey)
    }
}
```

### 새로운 템플릿 엔진 추가

`TemplateEngine` 인터페이스를 구현하여 새로운 템플릿 엔진을 추가할 수 있습니다:

```kotlin
// core/src/main/kotlin/com/commitchronicle/template/CustomTemplateEngine.kt
package com.commitchronicle.template

class CustomTemplateEngine : TemplateEngine {
    override fun render(templatePath: String, data: Map<String, Any>): String {
        // 커스텀 템플릿 구현
    }
    
    override fun renderString(templateContent: String, data: Map<String, Any>): String {
        // 커스텀 구현
    }
}
```

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