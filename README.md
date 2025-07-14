# 🚀 CommitChronicle

[![JitPack](https://jitpack.io/v/hj4645/commit-chronicle.svg)](https://jitpack.io/#hj4645/commit-chronicle)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=flat&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![GitHub Issues](https://img.shields.io/github/issues/hj4645/commit-chronicle.svg)](https://github.com/hj4645/commit-chronicle/issues)
[![GitHub Stars](https://img.shields.io/github/stars/hj4645/commit-chronicle.svg)](https://github.com/hj4645/commit-chronicle/stargazers)
[![CI](https://github.com/hj4645/commit-chronicle/workflows/CI/badge.svg)](https://github.com/hj4645/commit-chronicle/actions)

**AI 기반 Git 커밋 분석 및 PR 생성 도구**

CommitChronicle은 Git 커밋 히스토리와 다양한 AI 제공업체(OpenAI, Claude, Gemini, DeepSeek, Perplexity)를 활용해 Pull Request 초안, 변경 로그, 커밋 요약을 자동 생성하는 강력한 도구입니다.

---
 
## 📖 언어별 README | Multi-language Documentation

- **[한국어 (Korean)](README.md)** - 현재 문서
- **[English](README_EN.md)** - English Documentation  
- **[中文 (Chinese)](README_CN.md)** - 中文文档
- **[日本語 (Japanese)](README_JA.md)** - 日本語ドキュメント

---

## ✨ 주요 기능 | Key Features

🤖 **다중 AI 지원** - OpenAI, Claude, Gemini, DeepSeek, Perplexity 등 5개 AI 제공업체 지원  
📝 **자동 PR 생성** - GitHub 템플릿 자동 감지 및 다국어 PR 초안 생성  
📊 **스마트 변경 로그** - 커밋 타입별 자동 그룹화 및 변경 로그 생성  
🎯 **지능적 커밋 요약** - AI 기반 커밋 내용 분석 및 요약  
🔧 **템플릿 시스템** - 사용자 정의 템플릿 및 GitHub 템플릿 자동 감지  
🌐 **다국어 지원** - 한국어/영어 인터페이스 및 출력 지원  
⚡ **CLI & 플러그인** - 명령줄 도구 및 IntelliJ IDEA 플러그인 제공  
🔒 **브랜치 보호** - Git Hook을 통한 자동 브랜치 정리  
☁️ **클라우드 배포** - JitPack을 통한 간편한 의존성 관리

## 🏗️ 프로젝트 구조

```
commit-chronicle/
├── core/                          # 핵심 라이브러리
│   ├── api/                       # 인터페이스 및 도메인 모델
│   │   └── src/main/kotlin/com/commitchronicle/
│   │       ├── ai/               # AI 요약 인터페이스
│   │       ├── git/              # Git 분석 인터페이스
│   │       ├── language/         # 다국어 지원
│   │       └── template/         # 템플릿 엔진 인터페이스
│   └── impl/                     # 구현체
│       └── src/main/kotlin/com/commitchronicle/
│           ├── ai/               # AI 제공업체별 구현체
│           │   ├── providers/    # OpenAI, Claude, Gemini, DeepSeek, Perplexity
│           │   └── factory/      # AI 팩토리
│           ├── git/              # JGit 구현체
│           ├── config/           # 사용자 설정
│           └── template/         # 템플릿 엔진 구현체
├── cli/                          # CLI 모듈
├── ide-plugin-intellij/          # IntelliJ 플러그인
└── .github/                      # GitHub Actions & 템플릿
    ├── workflows/               # CI/CD 파이프라인
    └── templates/               # PR/Issue 템플릿
```

## 🚀 빠른 시작

### 📋 요구 사항

- **Java**: 8~24 지원 (Java 8 최소 요구사항)
- **Kotlin**: 1.4.20 이상
- **AI API 키**: OpenAI, Claude, Gemini, DeepSeek, Perplexity 중 하나

### 📦 설치 방법

#### 1. 프로젝트 빌드
```bash
git clone https://github.com/hj4645/commit-chronicle.git
cd commit-chronicle
./gradlew build
```

#### 2. CLI 실행 파일 생성
```bash
./gradlew :cli:shadowJar
```

#### 3. JAR 파일 실행
```bash
java -jar cli/build/libs/commitchronicle-cli-*-all.jar --help
```

## 🖥️ CLI 사용법

### 기본 명령어 구조
```bash
java -jar commitchronicle-cli.jar [COMMAND] [OPTIONS]
```

### 🔧 전역 옵션

| 옵션 | 설명 | 기본값 | 예시 |
|------|------|--------|------|
| `--path` | Git 저장소 경로 | 현재 디렉토리 | `--path /path/to/repo` |
| `--locale` | 언어 설정 | 시스템 설정 | `--locale ko` 또는 `--locale en` |
| `--help` | 도움말 표시 | - | `--help` |

### 🤖 AI 제공업체 설정

#### 지원하는 AI 제공업체
CLI 실행 시 대화형으로 AI 제공업체를 선택할 수 있습니다:

```
AI 제공업체를 선택하세요:
1) OpenAI (ChatGPT)
2) Claude (Anthropic) 
3) Gemini (Google)
4) DeepSeek
5) Perplexity

선택 (1-5): 1
```

각 제공업체별 API 키가 필요합니다:
- **OpenAI**: https://platform.openai.com/api-keys
- **Claude**: https://console.anthropic.com/
- **Gemini**: https://makersuite.google.com/app/apikey
- **DeepSeek**: https://platform.deepseek.com/
- **Perplexity**: https://www.perplexity.ai/settings/api

### 📝 커밋 요약 생성

```bash
# 기본 요약 (최근 7일)
java -jar commitchronicle-cli.jar

# 특정 기간 요약
java -jar commitchronicle-cli.jar --days 30

# 커밋 개수 제한
java -jar commitchronicle-cli.jar --limit 50

# 특정 브랜치 분석
java -jar commitchronicle-cli.jar --branch develop

# 영어로 요약 생성
java -jar commitchronicle-cli.jar --locale en
```

### 🎯 PR 초안 생성

```bash
# GitHub 템플릿 자동 감지하여 PR 생성
java -jar commitchronicle-cli.jar pr --title "새로운 기능 추가"

# 사용자 정의 템플릿 사용
java -jar commitchronicle-cli.jar pr --template my_template.md

# 특정 브랜치 간 PR
java -jar commitchronicle-cli.jar pr --base main --head feature/new-feature

# 영어 PR 생성
java -jar commitchronicle-cli.jar pr --locale en
```

### 📊 변경 로그 생성

```bash
# 기본 변경 로그
java -jar commitchronicle-cli.jar changelog

# 타입별 그룹화
java -jar commitchronicle-cli.jar changelog --group

# 사용자 정의 템플릿으로 변경 로그
java -jar commitchronicle-cli.jar changelog --template changelog_template.md

# 특정 태그 간 변경 로그
java -jar commitchronicle-cli.jar changelog --from v1.0.0 --to v2.0.0
```

### 🌍 다국어 사용법

#### 언어 설정
```bash
# 한국어 (기본값)
java -jar commitchronicle-cli.jar --locale ko

# 영어
java -jar commitchronicle-cli.jar --locale en
```

#### 언어별 출력 예시

**한국어 출력:**
```
✅ 분석 완료: 15개의 커밋을 분석했습니다
📝 주요 변경사항:
- 새로운 AI 제공업체 추가 (Claude, Gemini)
- 다국어 지원 구현
- GitHub 템플릿 자동 감지 기능
```

**영어 출력:**
```
✅ Analysis complete: Analyzed 15 commits
📝 Key changes:
- Added new AI providers (Claude, Gemini)  
- Implemented multi-language support
- Added GitHub template auto-detection
```

## 🔧 GitHub 템플릿 자동 감지

### 지원하는 템플릿 경로

**PR 템플릿 (우선순위 순):**
```
.github/pull_request_template_[locale].md    # 다국어 지원
.github/pull_request_template.md
.github/PULL_REQUEST_TEMPLATE.md
.github/PULL_REQUEST_TEMPLATE/pull_request_template.md
docs/pull_request_template.md
docs/PULL_REQUEST_TEMPLATE.md
pull_request_template.md
PULL_REQUEST_TEMPLATE.md
```

**다국어 템플릿 예시:**
- `.github/pull_request_template_ko.md` (한국어)
- `.github/pull_request_template_en.md` (영어)

### 템플릿 변수 시스템

GitHub 템플릿에서 사용 가능한 변수들:

```markdown
# {{title}}

## 📝 변경 사항
{{commits.summary}}

## 📋 커밋 목록  
{{commits.list}}

## 📁 변경된 파일
{{commits.files}}

## 👥 참여자
{{commits.authors}}

## ✅ 체크리스트
- [ ] 테스트 완료
- [ ] 문서 업데이트  
- [ ] Breaking changes 확인
- [ ] 코드 리뷰 완료
```

### 자동 체크박스 처리

커밋 내용을 분석하여 체크박스를 자동으로 체크합니다:

| 키워드 | 자동 체크되는 항목 |
|--------|-------------------|
| `test`, `spec`, `junit` | 테스트 관련 체크박스 |
| `doc`, `readme`, `문서` | 문서 관련 체크박스 |
| `fix`, `bug`, `수정` | 버그 수정 체크박스 |
| `feat`, `feature`, `기능` | 새 기능 체크박스 |
| `refactor`, `리팩토링` | 리팩토링 체크박스 |

## 🔌 IntelliJ IDEA 플러그인

### 설치 방법

1. **플러그인 빌드:**
   ```bash
   ./gradlew :ide-plugin-intellij:buildPlugin
   ```

2. **플러그인 설치:**
   - `ide-plugin-intellij/build/distributions/CommitChronicle-*.zip` 파일을 IntelliJ에서 설치

3. **사용법:**
   - `Tools > CommitChronicle` 메뉴 접근
   - AI 제공업체 및 API 키 설정
   - 원하는 기능 실행

### 플러그인 기능

- **커밋 요약**: 현재 프로젝트의 커밋 히스토리 요약
- **PR 생성**: GitHub 템플릿 자동 감지하여 PR 초안 생성
- **변경 로그**: 릴리스용 변경 로그 자동 생성
- **설정 관리**: AI 제공업체 및 언어 설정

## 🛠️ 고급 기능

### Git Hook 설정

자동 브랜치 정리를 위한 Git Hook:

```bash
# .git/hooks/post-merge 파일 생성
cat > .git/hooks/post-merge << 'EOF'
#!/bin/bash

# PR 머지 후 원격 브랜치 자동 삭제
current_branch=$(git rev-parse --abbrev-ref HEAD)

if [[ "$current_branch" == "main" || "$current_branch" == "develop" ]]; then
    # 머지된 브랜치들을 찾아서 원격에서 삭제
    git remote prune origin
    
    # 로컬에서 이미 머지된 브랜치들 정리
    git branch --merged | grep -v "main\|develop\|master" | xargs -n 1 git branch -d 2>/dev/null || true
fi
EOF

chmod +x .git/hooks/post-merge
```

### 사용자 정의 템플릿

#### 템플릿 파일 생성
```markdown
<!-- custom_pr_template.md -->
# 🚀 {{title}}

## 📋 요약
{{commits.summary}}

## 🔄 변경사항
{{#commits.changes}}
- **{{type}}**: {{message}}
{{/commits.changes}}

## 🧪 테스트
- [ ] 단위 테스트 통과
- [ ] 통합 테스트 통과

## 📖 문서
- [ ] README 업데이트
- [ ] API 문서 업데이트
```

#### 템플릿 사용
```bash
java -jar commitchronicle-cli.jar pr --template custom_pr_template.md
```

## 🏗️ 개발자 가이드

### 새로운 AI 제공업체 추가

1. **AI 설정 클래스 생성:**
```kotlin
// core/impl/src/main/kotlin/com/commitchronicle/ai/providers/newai/config/NewAIConfig.kt
data class NewAIConfig(
    val apiKey: String,
    val model: String = "default-model",
    val maxTokens: Int = 4096
)
```

2. **AI 요약기 구현:**
```kotlin
// core/impl/src/main/kotlin/com/commitchronicle/ai/providers/newai/NewAISummarizer.kt
class NewAISummarizer(private val config: NewAIConfig) : BaseSummarizer() {
    override suspend fun callAI(prompt: String): String {
        // AI API 호출 구현
    }
}
```

3. **팩토리에 등록:**
```kotlin
// AISummarizerFactory.kt 업데이트
"newai" -> NewAISummarizer(NewAIConfig(apiKey))
```

### 새로운 언어 추가

1. **메시지 파일 생성:**
```properties
# core/impl/src/main/resources/messages_fr.properties (프랑스어 예시)
summary.title=Résumé des commits
pr.title=Projet de Pull Request
changelog.title=Journal des modifications
```

2. **로케일 코드 추가:**
```kotlin
// core/api/src/main/kotlin/com/commitchronicle/language/Locale.kt
enum class Locale(val code: String, val displayName: String) {
    // ... 기존 코드 ...
    FRENCH("fr", "Français")
}
```

## 🧪 테스트

### 전체 테스트 실행
```bash
./gradlew test
```

### 모듈별 테스트
```bash
# Core 모듈 테스트
./gradlew :core:api:test :core:impl:test

# CLI 모듈 테스트  
./gradlew :cli:test

# 플러그인 테스트
./gradlew :ide-plugin-intellij:test
```

### 성능 테스트
```bash
# 대량 커밋 분석 테스트
java -jar commitchronicle-cli.jar --limit 1000 --days 365
```

## 📈 성능 최적화

### 권장 설정

| 저장소 크기 | 권장 limit | 권장 days | 예상 처리 시간 |
|-------------|------------|-----------|----------------|
| 소형 (<100 커밋) | 50 | 30 | 10-30초 |
| 중형 (100-1000 커밋) | 100 | 14 | 30-60초 |
| 대형 (1000+ 커밋) | 200 | 7 | 1-3분 |

### 메모리 사용량 최적화
```bash
# JVM 메모리 설정
java -Xmx2g -Xms512m -jar commitchronicle-cli.jar
```

## 🤝 기여하기

### 개발 환경 설정
```bash
git clone https://github.com/hj4645/commit-chronicle.git
cd commit-chronicle

# 개발용 빌드
./gradlew build

# 코드 스타일 검사
./gradlew ktlintCheck

# 코드 스타일 자동 수정
./gradlew ktlintFormat
```

### Pull Request 가이드라인

1. **브랜치 명명 규칙:**
   - `feature/기능명`: 새로운 기능
   - `fix/버그명`: 버그 수정
   - `docs/문서명`: 문서 개선
   - `refactor/리팩토링명`: 코드 리팩토링

2. **커밋 메시지 규칙:**
   ```
   type(scope): subject
   
   body
   
   footer
   ```

3. **PR 체크리스트:**
   - [ ] 테스트 코드 작성
   - [ ] 문서 업데이트
   - [ ] Breaking changes 확인
   - [ ] 코드 스타일 준수

## 🐛 문제 해결

### 일반적인 문제들

#### Q: "Unsupported class file major version" 오류
**A:** Java 버전 확인 및 호환 버전 사용:
```bash
java -version  # Java 8 이상 확인
```

#### Q: AI API 호출 실패
**A:** API 키 및 네트워크 연결 확인:
```bash
# 환경변수로 API 키 설정
export OPENAI_API_KEY="your-api-key"
java -jar commitchronicle-cli.jar
```

#### Q: Git 저장소를 찾을 수 없음
**A:** Git 저장소 경로 확인:
```bash
# 현재 디렉토리가 Git 저장소인지 확인
git status

# 또는 명시적으로 경로 지정
java -jar commitchronicle-cli.jar --path /path/to/git/repo
```

### 로그 레벨 설정
```bash
# 디버그 모드 실행
java -Dlogging.level.root=DEBUG -jar commitchronicle-cli.jar
```

## 📄 라이센스

이 프로젝트는 MIT 라이센스 하에 배포됩니다. 자세한 내용은 [LICENSE](LICENSE) 파일을 참조하세요.

---

## 🔗 관련 링크

- **GitHub Repository**: https://github.com/hj4645/commit-chronicle
- **JitPack**: https://jitpack.io/#hj4645/commit-chronicle
- **Issues**: https://github.com/hj4645/commit-chronicle/issues
- **Wiki**: https://github.com/hj4645/commit-chronicle/wiki

---

**📧 Contact**: hj4645@example.com  
**🌟 Star us on GitHub**: https://github.com/hj4645/commit-chronicle