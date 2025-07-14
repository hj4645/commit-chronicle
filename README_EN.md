# 🚀 CommitChronicle

[![JitPack](https://jitpack.io/v/hj4645/commit-chronicle.svg)](https://jitpack.io/#hj4645/commit-chronicle)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=flat&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![GitHub Issues](https://img.shields.io/github/issues/hj4645/commit-chronicle.svg)](https://github.com/hj4645/commit-chronicle/issues)
[![GitHub Stars](https://img.shields.io/github/stars/hj4645/commit-chronicle.svg)](https://github.com/hj4645/commit-chronicle/stargazers)
[![CI](https://github.com/hj4645/commit-chronicle/workflows/CI/badge.svg)](https://github.com/hj4645/commit-chronicle/actions)

**AI-Powered Git Commit Analysis & PR Generation Tool**

CommitChronicle is a powerful tool that automatically generates Pull Request drafts, changelogs, and commit summaries using Git commit history and various AI providers (OpenAI, Claude, Gemini, DeepSeek, Perplexity).

---

## 📖 Multi-language Documentation

- **[한국어 (Korean)](README.md)** - Korean Documentation
- **[English](README_EN.md)** - Current Document  
- **[中文 (Chinese)](README_CN.md)** - 中文文档
- **[日本語 (Japanese)](README_JA.md)** - 日本語ドキュメント

---

## ✨ Key Features

🤖 **Multi-AI Support** - Supports 5 AI providers: OpenAI, Claude, Gemini, DeepSeek, Perplexity  
📝 **Auto PR Generation** - Automatic GitHub template detection and multilingual PR draft generation  
📊 **Smart Changelog** - Automatic grouping by commit type and changelog generation  
🎯 **Intelligent Commit Summary** - AI-based commit content analysis and summarization  
🔧 **Template System** - Custom templates and automatic GitHub template detection  
🌐 **Multi-language Support** - Korean/English interface and output support  
⚡ **CLI & Plugin** - Command-line tool and IntelliJ IDEA plugin  
🔒 **Branch Protection** - Automatic branch cleanup via Git Hooks  
☁️ **Cloud Deployment** - Easy dependency management through JitPack

## 🏗️ Project Structure

```
commit-chronicle/
├── core/                          # Core library
│   ├── api/                       # Interfaces and domain models
│   │   └── src/main/kotlin/com/commitchronicle/
│   │       ├── ai/               # AI summarization interfaces
│   │       ├── git/              # Git analysis interfaces
│   │       ├── language/         # Multi-language support
│   │       └── template/         # Template engine interfaces
│   └── impl/                     # Implementations
│       └── src/main/kotlin/com/commitchronicle/
│           ├── ai/               # AI provider implementations
│           │   ├── providers/    # OpenAI, Claude, Gemini, DeepSeek, Perplexity
│           │   └── factory/      # AI factory
│           ├── git/              # JGit implementation
│           ├── config/           # User configuration
│           └── template/         # Template engine implementation
├── cli/                          # CLI module
├── ide-plugin-intellij/          # IntelliJ plugin
└── .github/                      # GitHub Actions & templates
    ├── workflows/               # CI/CD pipeline
    └── templates/               # PR/Issue templates
```

## 🚀 Quick Start

### 📋 Requirements

- **Java**: 8~24 supported (Java 8 minimum requirement)
- **Kotlin**: 1.4.20 or higher
- **AI API Key**: One of OpenAI, Claude, Gemini, DeepSeek, Perplexity

### 📦 Installation

#### 1. Build Project
```bash
git clone https://github.com/hj4645/commit-chronicle.git
cd commit-chronicle
./gradlew build
```

#### 2. Create CLI Executable
```bash
./gradlew :cli:shadowJar
```

#### 3. Run JAR File
```bash
java -jar cli/build/libs/commitchronicle-cli-*-all.jar --help
```

## 🖥️ CLI Usage

### Basic Command Structure
```bash
java -jar commitchronicle-cli.jar [COMMAND] [OPTIONS]
```

### 🔧 Global Options

| Option | Description | Default | Example |
|--------|-------------|---------|---------|
| `--path` | Git repository path | Current directory | `--path /path/to/repo` |
| `--locale` | Language setting | System setting | `--locale ko` or `--locale en` |
| `--help` | Show help | - | `--help` |

### 🤖 AI Provider Setup

#### Supported AI Providers
You can interactively select an AI provider when running CLI:

```
Please select an AI provider:
1) OpenAI (ChatGPT)
2) Claude (Anthropic) 
3) Gemini (Google)
4) DeepSeek
5) Perplexity

Choose (1-5): 1
```

API keys are required for each provider:
- **OpenAI**: https://platform.openai.com/api-keys
- **Claude**: https://console.anthropic.com/
- **Gemini**: https://makersuite.google.com/app/apikey
- **DeepSeek**: https://platform.deepseek.com/
- **Perplexity**: https://www.perplexity.ai/settings/api

### 📝 Commit Summarization

```bash
# Basic summary (last 7 days)
java -jar commitchronicle-cli.jar

# Specific period summary
java -jar commitchronicle-cli.jar --days 30

# Limit number of commits
java -jar commitchronicle-cli.jar --limit 50

# Analyze specific branch
java -jar commitchronicle-cli.jar --branch develop

# Generate summary in English
java -jar commitchronicle-cli.jar --locale en
```

### 🎯 PR Draft Generation

```bash
# Auto-detect GitHub template and create PR
java -jar commitchronicle-cli.jar pr --title "Add new feature"

# Use custom template
java -jar commitchronicle-cli.jar pr --template my_template.md

# PR between specific branches
java -jar commitchronicle-cli.jar pr --base main --head feature/new-feature

# Generate English PR
java -jar commitchronicle-cli.jar pr --locale en
```

### 📊 Changelog Generation

```bash
# Basic changelog
java -jar commitchronicle-cli.jar changelog

# Group by type
java -jar commitchronicle-cli.jar changelog --group

# Changelog with custom template
java -jar commitchronicle-cli.jar changelog --template changelog_template.md

# Changelog between specific tags
java -jar commitchronicle-cli.jar changelog --from v1.0.0 --to v2.0.0
```

### 🌍 Multi-language Usage

#### Language Setting
```bash
# Korean (default)
java -jar commitchronicle-cli.jar --locale ko

# English
java -jar commitchronicle-cli.jar --locale en
```

#### Language-specific Output Examples

**Korean Output:**
```
✅ Analysis complete: Analyzed 15 commits
📝 Key changes:
- Added new AI providers (Claude, Gemini)
- Implemented multi-language support
- Added GitHub template auto-detection
```

**English Output:**
```
✅ Analysis complete: Analyzed 15 commits
📝 Key changes:
- Added new AI providers (Claude, Gemini)  
- Implemented multi-language support
- Added GitHub template auto-detection
```

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

### Template Variable System

Available variables in GitHub templates:

```markdown
# {{title}}

## 📝 Changes
{{commits.summary}}

## 📋 Commit List  
{{commits.list}}

## 📁 Changed Files
{{commits.files}}

## 👥 Contributors
{{commits.authors}}

## ✅ Checklist
- [ ] Tests completed
- [ ] Documentation updated  
- [ ] Breaking changes verified
- [ ] Code review completed
```

### Automatic Checkbox Processing

Checkboxes are automatically checked by analyzing commit content:

| Keywords | Auto-checked Items |
|----------|-------------------|
| `test`, `spec`, `junit` | Test-related checkboxes |
| `doc`, `readme`, `documentation` | Documentation-related checkboxes |
| `fix`, `bug`, `bugfix` | Bug fix checkboxes |
| `feat`, `feature`, `add` | New feature checkboxes |
| `refactor`, `refactoring` | Refactoring checkboxes |

## 🔌 IntelliJ IDEA Plugin

### Installation

1. **Build Plugin:**
   ```bash
   ./gradlew :ide-plugin-intellij:buildPlugin
   ```

2. **Install Plugin:**
   - Install the `ide-plugin-intellij/build/distributions/CommitChronicle-*.zip` file in IntelliJ

3. **Usage:**
   - Access `Tools > CommitChronicle` menu
   - Configure AI provider and API key
   - Execute desired features

### Plugin Features

- **Commit Summary**: Summarize current project's commit history
- **PR Generation**: Auto-detect GitHub templates and create PR drafts
- **Changelog**: Auto-generate release changelogs
- **Settings Management**: Configure AI providers and language settings

## 🛠️ Advanced Features

### Git Hook Setup

Git Hook for automatic branch cleanup:

```bash
# Create .git/hooks/post-merge file
cat > .git/hooks/post-merge << 'EOF'
#!/bin/bash

# Auto-delete remote branches after PR merge
current_branch=$(git rev-parse --abbrev-ref HEAD)

if [[ "$current_branch" == "main" || "$current_branch" == "develop" ]]; then
    # Find and delete merged branches from remote
    git remote prune origin
    
    # Clean up local merged branches
    git branch --merged | grep -v "main\|develop\|master" | xargs -n 1 git branch -d 2>/dev/null || true
fi
EOF

chmod +x .git/hooks/post-merge
```

### Custom Templates

#### Create Template File
```markdown
<!-- custom_pr_template.md -->
# 🚀 {{title}}

## 📋 Summary
{{commits.summary}}

## 🔄 Changes
{{#commits.changes}}
- **{{type}}**: {{message}}
{{/commits.changes}}

## 🧪 Testing
- [ ] Unit tests passed
- [ ] Integration tests passed

## 📖 Documentation
- [ ] README updated
- [ ] API documentation updated
```

#### Using Template
```bash
java -jar commitchronicle-cli.jar pr --template custom_pr_template.md
```

## 🏗️ Developer Guide

### Adding New AI Providers

1. **Create AI Config Class:**
```kotlin
// core/impl/src/main/kotlin/com/commitchronicle/ai/providers/newai/config/NewAIConfig.kt
data class NewAIConfig(
    val apiKey: String,
    val model: String = "default-model",
    val maxTokens: Int = 4096
)
```

2. **Implement AI Summarizer:**
```kotlin
// core/impl/src/main/kotlin/com/commitchronicle/ai/providers/newai/NewAISummarizer.kt
class NewAISummarizer(private val config: NewAIConfig) : BaseSummarizer() {
    override suspend fun callAI(prompt: String): String {
        // Implement AI API call
    }
}
```

3. **Register in Factory:**
```kotlin
// Update AISummarizerFactory.kt
"newai" -> NewAISummarizer(NewAIConfig(apiKey))
```

### Adding New Languages

1. **Create Message File:**
```properties
# core/impl/src/main/resources/messages_fr.properties (French example)
summary.title=Résumé des commits
pr.title=Projet de Pull Request
changelog.title=Journal des modifications
```

2. **Add Locale Code:**
```kotlin
// core/api/src/main/kotlin/com/commitchronicle/language/Locale.kt
enum class Locale(val code: String, val displayName: String) {
    // ... existing code ...
    FRENCH("fr", "Français")
}
```

## 🧪 Testing

### Run All Tests
```bash
./gradlew test
```

### Module-specific Tests
```bash
# Core module tests
./gradlew :core:api:test :core:impl:test

# CLI module tests  
./gradlew :cli:test

# Plugin tests
./gradlew :ide-plugin-intellij:test
```

### Performance Testing
```bash
# Large commit analysis test
java -jar commitchronicle-cli.jar --limit 1000 --days 365
```

## 📈 Performance Optimization

### Recommended Settings

| Repository Size | Recommended limit | Recommended days | Expected Processing Time |
|----------------|-------------------|------------------|-------------------------|
| Small (<100 commits) | 50 | 30 | 10-30 seconds |
| Medium (100-1000 commits) | 100 | 14 | 30-60 seconds |
| Large (1000+ commits) | 200 | 7 | 1-3 minutes |

### Memory Usage Optimization
```bash
# JVM memory settings
java -Xmx2g -Xms512m -jar commitchronicle-cli.jar
```

## 🤝 Contributing

### Development Environment Setup
```bash
git clone https://github.com/hj4645/commit-chronicle.git
cd commit-chronicle

# Development build
./gradlew build

# Code style check
./gradlew ktlintCheck

# Auto-fix code style
./gradlew ktlintFormat
```

### Pull Request Guidelines

1. **Branch Naming Convention:**
   - `feature/feature-name`: New features
   - `fix/bug-name`: Bug fixes
   - `docs/doc-name`: Documentation improvements
   - `refactor/refactor-name`: Code refactoring

2. **Commit Message Convention:**
   ```
   type(scope): subject
   
   body
   
   footer
   ```

3. **PR Checklist:**
   - [ ] Test code written
   - [ ] Documentation updated
   - [ ] Breaking changes verified
   - [ ] Code style compliance

## 🐛 Troubleshooting

### Common Issues

#### Q: "Unsupported class file major version" error
**A:** Check Java version and use compatible version:
```bash
java -version  # Verify Java 8 or higher
```

#### Q: AI API call failure
**A:** Check API key and network connection:
```bash
# Set API key as environment variable
export OPENAI_API_KEY="your-api-key"
java -jar commitchronicle-cli.jar
```

#### Q: Cannot find Git repository
**A:** Verify Git repository path:
```bash
# Check if current directory is a Git repository
git status

# Or explicitly specify path
java -jar commitchronicle-cli.jar --path /path/to/git/repo
```

### Log Level Configuration
```bash
# Run in debug mode
java -Dlogging.level.root=DEBUG -jar commitchronicle-cli.jar
```

## 📄 License

This project is distributed under the MIT License. See the [LICENSE](LICENSE) file for details.

---

## 🔗 Related Links

- **GitHub Repository**: https://github.com/hj4645/commit-chronicle
- **JitPack**: https://jitpack.io/#hj4645/commit-chronicle
- **Issues**: https://github.com/hj4645/commit-chronicle/issues
- **Wiki**: https://github.com/hj4645/commit-chronicle/wiki

---

**📧 Contact**: hj4645@example.com  
**🌟 Star us on GitHub**: https://github.com/hj4645/commit-chronicle 