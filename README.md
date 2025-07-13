# CommitChronicle

CommitChronicle는 Git 커밋 히스토리와 AI를 활용해 Pull Request 초안 및 버전별 변경 사항 요약, 커밋 내용 등을 자동 생성하는 오픈소스 라이브러리입니다.

## 주요 기능

- 특정 기간/커밋 범위 기반 변경 사항 분석
- AI를 활용한 자연어 기반 요약 생성
- PR 초안 자동 생성
- 변경 로그 자동 생성
- 사용자 정의 템플릿 지원
- **GitHub 템플릿 자동 감지 및 적용**
- IntelliJ IDEA 플러그인 지원

## 프로젝트 구조

```
commit-chronicle/
├── core/                      # 핵심 라이브러리
│   ├── api/                   # 인터페이스 및 도메인 모델
│   │   └── src/main/kotlin/com/commitchronicle/
│   │       ├── ai/           # AI 요약 인터페이스
│   │       ├── git/          # Git 분석 인터페이스
│   │       ├── model/        # 데이터 클래스
│   │       └── template/     # 템플릿 엔진 및 GitHub 템플릿 감지 인터페이스
│   └── impl/                 # 구현체
│       └── src/main/kotlin/com/commitchronicle/
│           ├── ai/           # OpenAI 구현체
│           ├── git/          # JGit 구현체
│           └── template/     # Markdown 템플릿 및 GitHub 템플릿 감지 구현체
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

# PR 초안 생성 (GitHub 템플릿 자동 감지)
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

### GitHub 템플릿 자동 감지 기능

CommitChronicle는 프로젝트의 GitHub 템플릿을 자동으로 감지하고 적용합니다.

#### 지원하는 GitHub 템플릿 경로

**PR 템플릿:**
- `.github/pull_request_template.md`
- `.github/PULL_REQUEST_TEMPLATE.md`
- `.github/PULL_REQUEST_TEMPLATE/pull_request_template.md`
- `docs/pull_request_template.md`
- `docs/PULL_REQUEST_TEMPLATE.md`
- `pull_request_template.md`
- `PULL_REQUEST_TEMPLATE.md`

**Issue 템플릿:**
- `.github/ISSUE_TEMPLATE/` (디렉토리 내 모든 .md, .yml, .yaml 파일)
- `.github/issue_template.md`
- `.github/ISSUE_TEMPLATE.md`
- `docs/issue_template.md`
- `docs/ISSUE_TEMPLATE.md`
- `issue_template.md`
- `ISSUE_TEMPLATE.md`

#### 템플릿 변수 지원

GitHub 템플릿 내에서 다음 변수들을 사용할 수 있습니다:

```markdown
<!-- PR 템플릿 예시 -->
# {{title}}

## 변경 사항
{{commits.summary}}

## 커밋 목록
{{commits.list}}

## 변경된 파일
{{commits.files}}

## 참여자
{{commits.authors}}

## 체크리스트
- [ ] 테스트 완료
- [ ] 문서 업데이트
- [ ] Breaking changes 확인
```

#### 자동 체크박스 처리

템플릿의 체크박스는 커밋 내용을 분석하여 자동으로 체크됩니다:
- 테스트 관련 커밋이 있으면 "test" 관련 체크박스 자동 체크
- 문서 관련 커밋이 있으면 "documentation" 관련 체크박스 자동 체크
- 버그 수정 커밋이 있으면 "fix" 관련 체크박스 자동 체크
- 새 기능 커밋이 있으면 "feature" 관련 체크박스 자동 체크

### IntelliJ 플러그인 설치 및 사용

1. IntelliJ IDEA에서 플러그인 빌드:
   ```bash
   ./gradlew :ide-plugin-intellij:buildPlugin
   ```

2. 빌드된 플러그인(ide-plugin-intellij/build/distributions/CommitChronicle-*.zip) 설치
3. `Tools > CommitChronicle` 메뉴에서 원하는 기능 실행
4. OpenAI API 키 입력 후 실행
5. PR 생성 시 GitHub 템플릿이 자동으로 감지되어 적용됩니다

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