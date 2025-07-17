# Commit Chronicle

[![JitPack](https://jitpack.io/v/hj4645/commit-chronicle.svg)](https://jitpack.io/#hj4645/commit-chronicle)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=flat&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![GitHub Issues](https://img.shields.io/github/issues/hj4645/commit-chronicle.svg)](https://github.com/hj4645/commit-chronicle/issues)
[![GitHub Stars](https://img.shields.io/github/stars/hj4645/commit-chronicle.svg)](https://github.com/hj4645/commit-chronicle/stargazers)
[![CI](https://github.com/hj4645/commit-chronicle/workflows/JitPack%20Release/badge.svg)](https://github.com/hj4645/commit-chronicle/actions)

AI를 활용한 Git 커밋 분석 및 요약 라이브러리입니다. 커밋 히스토리를 분석하여 Pull Request 초안, 변경 로그, 커밋 요약을 자동 생성합니다.

## 🌐 다국어 문서

- **[한국어 (Korean)](README.md)** - 현재 문서
- **[English](README_EN.md)** - English Documentation  
- **[中文 (Chinese)](README_ZH.md)** - 中文文档
- **[日本語 (Japanese)](README_JA.md)** - 日本語ドキュメント

## 🚀 주요 기능

- **AI 기반 커밋 분석**: OpenAI, Claude, Gemini, Perplexity, DeepSeek 지원
- **PR 초안 자동 생성**: 커밋 히스토리 기반 PR 템플릿 생성
- **다국어 지원**: 한국어, 영어, 중국어, 일본어
- **GitHub 템플릿 감지**: 기존 PR 템플릿 자동 적용
- **CLI 도구**: 명령줄에서 바로 사용 가능

## 📦 설치 방법

### Gradle

```kotlin
repositories {
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    // 특정 버전 사용
    implementation("com.github.hj4645:commit-chronicle:1.0.0")
    
    // 또는 최신 버전 자동 사용
    implementation("com.github.hj4645:commit-chronicle")  // 최신 릴리스
    // 또는
    implementation("com.github.hj4645:commit-chronicle:latest")  // 최신 릴리스
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
    <!-- 특정 버전 사용 -->
    <dependency>
        <groupId>com.github.hj4645</groupId>
        <artifactId>commit-chronicle</artifactId>
        <version>1.0.0</version>
    </dependency>
    
    <!-- 또는 최신 버전 자동 사용 -->
    <dependency>
        <groupId>com.github.hj4645</groupId>
        <artifactId>commit-chronicle</artifactId>
        <version>latest</version>  <!-- 최신 릴리스 -->
    </dependency>
</dependencies>
```

## 🛠️ 사용 방법

### 1. 라이브러리 설치 후 CLI 사용

라이브러리를 dependency로 추가한 후, alias를 설정하여 CLI로 사용할 수 있습니다.

#### 🚀 간편한 alias 설정 방법 (자동 최신 버전 탐지)

**macOS/Linux:**
```bash
# 자동 최신 버전 탐지 alias (권장) - 새 버전이 다운로드되면 자동으로 최신 버전 사용
echo "alias cch='java -jar \$(find ~/.gradle/caches -path \"*commit-chronicle*\" -name \"*.jar\" -type f -print0 | xargs -0 ls -t | head -1)'" >> ~/.zshrc && source ~/.zshrc

# 또는 단계별로 설정
echo 'alias cch="java -jar \$(find ~/.gradle/caches -path \"*commit-chronicle*\" -name \"*.jar\" -type f -print0 | xargs -0 ls -t | head -1)"' >> ~/.zshrc
source ~/.zshrc
```

**Windows (PowerShell):**
```powershell
# JAR 파일 경로 찾기
$jarPath = Get-ChildItem -Path "$env:USERPROFILE\.gradle\caches" -Recurse -Name "*commit-chronicle*.jar" | Select-Object -First 1
$fullPath = Join-Path "$env:USERPROFILE\.gradle\caches" $jarPath

# alias 설정
echo "function cch { java -jar `"$fullPath`" @args }" >> $PROFILE
. $PROFILE
```

#### 수동 설정 방법

**JAR 파일 경로 확인:**
```bash
# macOS/Linux
find ~/.gradle/caches -name "*commit-chronicle*" -type f | grep "\.jar$"

# Windows (PowerShell)
Get-ChildItem -Path "$env:USERPROFILE\.gradle\caches" -Recurse -Name "*commit-chronicle*.jar"
```

**alias 설정:**
```bash
# macOS/Linux
alias cch="java -jar /실제/jar/파일/경로/commit-chronicle-1.0.0.jar"

# Windows (PowerShell)
function cch { java -jar "C:\실제\jar\파일\경로\commit-chronicle-1.0.0.jar" @args }
```

### 2. 초기 설정

처음 실행 시 인터랙티브 설정이 시작됩니다:

```bash
cch settings
```

**설정 과정:**

1. **언어 선택**
   - 한국어 (Korean)
   - English
   - 中文 (Chinese)
   - 日本語 (Japanese)

2. **AI 모델 선택**
   - OpenAI (gpt-4o, gpt-4o-mini, gpt-4-turbo)
   - Claude (claude-3-5-haiku-20241022, claude-3-haiku-20240307, claude-3-5-sonnet-20241022, claude-3-sonnet-20240229, claude-3-7-sonnet-20250219)
   - Gemini (gemini-2.5-pro, gemini-2.0-flash, gemini-2.5-flash)
   - Perplexity (llama-3.1-sonar-large-128k-online, llama-3.1-sonar-small-128k-online)
   - DeepSeek (deepseek-chat, deepseek-coder)

3. **API 키 입력**
   - 선택한 AI 모델의 API 키 입력

4. **분석 설정**
   - 기본 분석 기간 (일 단위, 기본값: 7일)
   - 최대 커밋 수 (기본값: 50개)

**설정 예시:**
```
Update current settings

Select setting to update:
Selected: Language

Select language (current: en):
Selected: 한국어 (Korean)
Language updated to: ko (AI responses will use this language)

Select setting to update:
Selected: AI Provider

Select AI provider (current: openai):
Selected: OpenAI (API Key ✓)

Select model for openai (current: gpt-4o):
Selected: GPT-4o (Latest)

API key for openai (current: ***):
Selected: Keep existing API key

Select setting to update:
Selected: Analysis Settings

Enter default days for analysis (current: 7, press Enter to keep current): 14

Enter default commit limit (current: 50, press Enter to keep current): 100
Analysis settings updated - Days: 14, Limit: 100

Select setting to update:
Selected: Done
Configuration saved successfully
```

### 3. CLI 명령어 사용

#### 커밋 요약 생성

```bash
# 기본 설정 사용 (설정된 기간과 커밋 수)
cch summarize

# 옵션 지정
cch summarize -d 14 -l 100           # 14일간, 최대 100개 커밋
cch summarize --days 7 --limit 50    # 7일간, 최대 50개 커밋
cch summarize --path /path/to/repo   # 특정 저장소 경로
```

#### PR 초안 생성

```bash
# 현재 브랜치의 PR 초안 생성
cch pr

# 옵션 지정
cch pr -d 7 -l 20                    # 7일간, 최대 20개 커밋
cch pr --path /path/to/repo          # 특정 저장소 경로
```

#### 설정 관리

```bash
# 설정 변경 (인터랙티브 메뉴)
cch settings

# 도움말 보기
cch settings --help
cch --help
```

### 4. 키보드 방향키 설정 방법

설정 메뉴에서는 키보드 방향키를 사용하여 옵션을 선택할 수 있습니다:

- **↑/↓ 화살표**: 옵션 선택
- **Enter**: 선택 확인
- **Esc**: 이전 메뉴로 돌아가기

**설정 메뉴 구조:**
```
Main Menu
├── Language (언어 설정)
├── AI Provider (AI 모델 설정)
│   ├── Provider Selection
│   ├── Model Selection  
│   └── API Key Input
├── Analysis Settings (분석 설정)
│   ├── Default Days
│   └── Commit Limit
└── Done (완료)
```

## 🔧 설정 파일

설정은 `~/.commit-chronicle/config.json`에 로컬 저장됩니다:

```json
{
  "apiKey": "your-api-key",
  "providerType": "openai",
  "locale": "ko",
  "defaultDays": 7,
  "defaultLimit": 50
}
```

**설정 지속성:**
- 한 번 설정하면 계속 유지됩니다
- 다른 프로젝트에서도 동일한 설정 사용
- 필요시 `cch settings`로 언제든 변경 가능

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
- `.github/pull_request_template_zh.md` (중국어)
- `.github/pull_request_template_ja.md` (일본어)

### 템플릿 적용 방식

1. **자동 감지**: 위 경로에서 템플릿 파일을 자동으로 찾습니다
2. **언어별 우선순위**: 설정된 언어에 맞는 템플릿을 우선 적용
3. **폴백 처리**: 해당 언어 템플릿이 없으면 기본 템플릿 사용
4. **동적 적용**: AI가 템플릿 구조에 맞춰 내용을 자동 생성

## 🤖 지원하는 AI 모델

| 프로바이더 | 모델 |
|-------------|--------|
| OpenAI | gpt-4o, gpt-4o-mini, gpt-4-turbo |
| Claude | claude-3-5-haiku-20241022, claude-3-haiku-20240307, claude-3-5-sonnet-20241022, claude-3-sonnet-20240229, claude-3-7-sonnet-20250219 |
| Gemini | gemini-2.5-pro, gemini-2.0-flash, gemini-2.5-flash |
| Perplexity | llama-3.1-sonar-large-128k-online, llama-3.1-sonar-small-128k-online |
| DeepSeek | deepseek-chat, deepseek-coder |

## 📁 프로젝트 구조

```
commit-chronicle/
├── core/
│   ├── api/           # 인터페이스와 모델
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

## 🤝 기여

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📞 지원

- Issues: [GitHub Issues](https://github.com/hj4645/commit-chronicle/issues)
- Documentation: [Wiki](https://github.com/hj4645/commit-chronicle/wiki)