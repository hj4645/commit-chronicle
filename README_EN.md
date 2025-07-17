# Commit Chronicle

[![JitPack](https://jitpack.io/v/hj4645/commit-chronicle.svg)](https://jitpack.io/#hj4645/commit-chronicle)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=flat&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![GitHub Issues](https://img.shields.io/github/issues/hj4645/commit-chronicle.svg)](https://github.com/hj4645/commit-chronicle/issues)
[![GitHub Stars](https://img.shields.io/github/stars/hj4645/commit-chronicle.svg)](https://github.com/hj4645/commit-chronicle/stargazers)
[![CI](https://github.com/hj4645/commit-chronicle/workflows/JitPack%20Release/badge.svg)](https://github.com/hj4645/commit-chronicle/actions)

An AI-powered Git commit analysis and summarization library. Analyze commit history to automatically generate Pull Request drafts, changelogs, and commit summaries.

## 🌐 Multi-language Documentation

- **[한국어 (Korean)](README.md)** - Korean Documentation
- **[English](README_EN.md)** - Current Document  
- **[中文 (Chinese)](README_ZH.md)** - Chinese Documentation
- **[日本語 (Japanese)](README_JA.md)** - Japanese Documentation

## 🚀 Key Features

- **AI-powered commit analysis**: Supports OpenAI, Claude, Gemini, Perplexity, DeepSeek
- **Automatic PR draft generation**: Creates PR templates based on commit history
- **Multi-language support**: Korean, English, Chinese, Japanese
- **GitHub template detection**: Automatically applies existing PR templates
- **CLI tool**: Ready-to-use command line interface

## 📦 Installation

### Gradle

```kotlin
repositories {
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    // Use specific version
    implementation("com.github.hj4645:commit-chronicle:1.0.0")
    
    // Or use latest version automatically
    implementation("com.github.hj4645:commit-chronicle")  // Latest release
    // Or
    implementation("com.github.hj4645:commit-chronicle:latest")  // Latest release
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
    <!-- Use specific version -->
    <dependency>
        <groupId>com.github.hj4645</groupId>
        <artifactId>commit-chronicle</artifactId>
        <version>1.0.0</version>
    </dependency>
    
    <!-- Or use latest version automatically -->
    <dependency>
        <groupId>com.github.hj4645</groupId>
        <artifactId>commit-chronicle</artifactId>
        <version>latest</version>  <!-- Latest release -->
    </dependency>
</dependencies>
```

## 🛠️ Usage

### 1. Using as Library for CLI

After adding the library as a dependency, you can set up an alias to use it as CLI.

#### 🚀 Easy Alias Setup Method

**macOS/Linux:**
```bash
# One-liner alias setup
echo "alias cch='java -jar \$(find ~/.gradle/caches -name \"*commit-chronicle*\" -type f | grep \"\\.jar\$\" | head -1)'" >> ~/.zshrc && source ~/.zshrc

# Or step-by-step setup
JAR_PATH=$(find ~/.gradle/caches -name "*commit-chronicle*" -type f | grep "\.jar$" | head -1)
echo "alias cch='java -jar $JAR_PATH'" >> ~/.zshrc
source ~/.zshrc
```

**Windows (PowerShell):**
```powershell
# Find JAR file path
$jarPath = Get-ChildItem -Path "$env:USERPROFILE\.gradle\caches" -Recurse -Name "*commit-chronicle*.jar" | Select-Object -First 1
$fullPath = Join-Path "$env:USERPROFILE\.gradle\caches" $jarPath

# Set up alias
echo "function cch { java -jar `"$fullPath`" @args }" >> $PROFILE
. $PROFILE
```

#### Manual Setup Method

**Find JAR file path:**
```bash
# macOS/Linux
find ~/.gradle/caches -name "*commit-chronicle*" -type f | grep "\.jar$"

# Windows (PowerShell)
Get-ChildItem -Path "$env:USERPROFILE\.gradle\caches" -Recurse -Name "*commit-chronicle*.jar"
```

**Set up alias:**
```bash
# macOS/Linux
alias cch="java -jar /actual/jar/file/path/commit-chronicle-1.0.0.jar"

# Windows (PowerShell)
function cch { java -jar "C:\actual\jar\file\path\commit-chronicle-1.0.0.jar" @args }
```

### 2. Initial Setup

Interactive setup starts on first run:

```bash
cch settings
```

**Setup Process:**

1. **Language Selection**
   - 한국어 (Korean)
   - English
   - 中文 (Chinese)
   - 日本語 (Japanese)

2. **AI Model Selection**
   - OpenAI (gpt-4o, gpt-4o-mini, gpt-4-turbo)
   - Claude (claude-3-5-haiku-20241022, claude-3-haiku-20240307, claude-3-5-sonnet-20241022, claude-3-sonnet-20240229, claude-3-7-sonnet-20250219)
   - Gemini (gemini-2.5-pro, gemini-2.0-flash, gemini-2.5-flash)
   - Perplexity (llama-3.1-sonar-large-128k-online, llama-3.1-sonar-small-128k-online)
   - DeepSeek (deepseek-chat, deepseek-coder)

3. **API Key Input**
   - Enter API key for selected AI model

4. **Analysis Settings**
   - Default analysis period (days, default: 7)
   - Maximum commit count (default: 50)

**Setup Example:**
```
Update current settings

Select setting to update:
Selected: Language

Select language (current: en):
Selected: English
Language updated to: en (AI responses will use this language)

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

### 3. CLI Commands

#### Generate Commit Summary

```bash
# Use default settings (configured period and commit count)
cch summarize

# With options
cch summarize -d 14 -l 100           # 14 days, max 100 commits
cch summarize --days 7 --limit 50    # 7 days, max 50 commits
cch summarize --path /path/to/repo   # specific repository path
```

#### Generate PR Draft

```bash
# Generate PR draft for current branch
cch pr

# With options
cch pr -d 7 -l 20                    # 7 days, max 20 commits
cch pr --path /path/to/repo          # specific repository path
```

#### Settings Management

```bash
# Update settings (interactive menu)
cch settings

# Show help
cch settings --help
cch --help
```

### 4. Keyboard Navigation

Use keyboard arrow keys to navigate the settings menu:

- **↑/↓ arrows**: Select options
- **Enter**: Confirm selection
- **Esc**: Go back to previous menu

**Settings Menu Structure:**
```
Main Menu
├── Language (Language settings)
├── AI Provider (AI model settings)
│   ├── Provider Selection
│   ├── Model Selection  
│   └── API Key Input
├── Analysis Settings (Analysis settings)
│   ├── Default Days
│   └── Commit Limit
└── Done (Complete)
```

## 🔧 Configuration File

Settings are stored locally in `~/.commit-chronicle/config.json`:

```json
{
  "apiKey": "your-api-key",
  "providerType": "openai",
  "locale": "en",
  "defaultDays": 7,
  "defaultLimit": 50
}
```

**Configuration Persistence:**
- Settings persist once configured
- Same settings used across different projects
- Can be changed anytime with `cch settings`

## 🔧 GitHub Template Auto-Detection

### Supported Template Paths

**PR Templates (in priority order):**
```
.github/pull_request_template_[locale].md    # Multi-language support
.github/pull_request_template.md
.github/PULL_REQUEST_TEMPLATE.md
.github/PULL_REQUEST_TEMPLATE/pull_request_template.md
docs/pull_request_template.md
docs/PULL_REQUEST_TEMPLATE.md
pull_request_template.md
PULL_REQUEST_TEMPLATE.md
```

**Multi-language Template Examples:**
- `.github/pull_request_template_ko.md` (Korean)
- `.github/pull_request_template_en.md` (English)
- `.github/pull_request_template_zh.md` (Chinese)
- `.github/pull_request_template_ja.md` (Japanese)

### Template Application Process

1. **Auto-detection**: Automatically finds template files in above paths
2. **Language Priority**: Prioritizes templates matching configured language
3. **Fallback Handling**: Uses default template if language-specific template not found
4. **Dynamic Application**: AI automatically generates content matching template structure

## 🤖 Supported AI Models

| Provider | Models |
|----------|--------|
| OpenAI | gpt-4o, gpt-4o-mini, gpt-4-turbo |
| Claude | claude-3-5-haiku-20241022, claude-3-haiku-20240307, claude-3-5-sonnet-20241022, claude-3-sonnet-20240229, claude-3-7-sonnet-20250219 |
| Gemini | gemini-2.5-pro, gemini-2.0-flash, gemini-2.5-flash |
| Perplexity | llama-3.1-sonar-large-128k-online, llama-3.1-sonar-small-128k-online |
| DeepSeek | deepseek-chat, deepseek-coder |

## 📁 Project Structure

```
commit-chronicle/
├── core/
│   ├── api/           # Interfaces and models
│   └── impl/          # Implementation
├── cli/               # CLI tool
└── build.gradle.kts   # Build configuration
```

## 🌐 Multi-language Support

- **한국어** (ko)
- **English** (en)
- **中文** (zh)
- **日本語** (ja)

## 🔒 Security

- API keys are securely stored in user's home directory
- No sensitive information included in JAR files
- Independent configuration management per user

## 📄 License

MIT License

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📞 Support

- Issues: [GitHub Issues](https://github.com/hj4645/commit-chronicle/issues)
- Documentation: [Wiki](https://github.com/hj4645/commit-chronicle/wiki) 