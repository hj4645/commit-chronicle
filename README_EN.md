# Commit Chronicle

AI-powered Git commit analysis and summarization library. Automatically generates Pull Request drafts, changelogs, and commit summaries by analyzing commit history.

## 🚀 Key Features

- **AI-powered Commit Analysis**: Supports OpenAI, Claude, Gemini, Perplexity, DeepSeek
- **Automatic PR Draft Generation**: Creates PR templates based on commit history
- **Multi-language Support**: Korean, English, Chinese, Japanese
- **GitHub Template Detection**: Automatically applies existing PR templates
- **Branch Validation**: Warns on main/master branches and filters meaningful commits
- **CLI Tool**: Ready-to-use command-line interface

## 📦 Installation

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

## 🛠️ Usage

### CLI Usage

#### 1. Initial Setup

```bash
java -jar commitchronicle-0.1.0.jar
```

Interactive setup starts on first run:
- Language selection (Korean, English, 中文, 日本語)
- AI model selection (OpenAI, Claude, Gemini, Perplexity, DeepSeek)
- API key input

#### 2. Generate Commit Summary

```bash
# Default settings (last 7 days, max 50 commits)
java -jar commitchronicle-0.1.0.jar summarize

# With options
java -jar commitchronicle-0.1.0.jar summarize -d 14 -l 100
java -jar commitchronicle-0.1.0.jar summarize --path /path/to/repo
```

#### 3. Generate PR Draft

```bash
# Generate PR draft for current branch
java -jar commitchronicle-0.1.0.jar pr

# With options
java -jar commitchronicle-0.1.0.jar pr -d 7 -l 20
```

#### 4. Settings Management

```bash
# Show current settings
java -jar commitchronicle-0.1.0.jar settings --show

# Update settings
java -jar commitchronicle-0.1.0.jar settings

# Reset settings
java -jar commitchronicle-0.1.0.jar settings --reset
```

### Library Usage

```kotlin
import com.commitchronicle.git.GitAnalyzerFactory
import com.commitchronicle.ai.factory.AISummarizerFactory
import com.commitchronicle.ai.providers.openai.config.OpenAIConfig
import com.commitchronicle.ai.AIProviderType
import com.commitchronicle.language.Locale

// Create Git analyzer
val gitAnalyzer = GitAnalyzerFactory.create("/path/to/repo")

// AI configuration
val aiConfig = OpenAIConfig(
    apiKey = "your-api-key",
    locale = Locale.ENGLISH
)
val aiSummarizer = AISummarizerFactory.create(aiConfig, AIProviderType.OPENAI)

// Analyze commits
val commits = gitAnalyzer.getCommits(7) // Last 7 days
val summary = aiSummarizer.summarize(commits)
val prDraft = aiSummarizer.generatePRDraft(commits)

println("Summary: $summary")
println("PR Draft: $prDraft")
```

## 🔧 Configuration

Settings are stored in `~/.commit-chronicle/config.json`:

```json
{
  "apiKey": "your-api-key",
  "providerType": "openai",
  "locale": "en",
  "defaultDays": 7,
  "defaultLimit": 50
}
```

## 🤖 Supported AI Models

| Provider | Models |
|----------|--------|
| OpenAI | gpt-4o, gpt-4o-mini, gpt-4-turbo |
| Claude | claude-3-5-sonnet-20241022, claude-3-5-haiku-20241022 |
| Gemini | gemini-2.0-flash-exp, gemini-1.5-flash, gemini-1.5-pro |
| Perplexity | llama-3.1-sonar-large-128k-online, llama-3.1-sonar-small-128k-online |
| DeepSeek | deepseek-chat, deepseek-coder |

## 📁 Project Structure

```
commit-chronicle/
├── core/
│   ├── api/           # Interfaces and models
│   └── impl/          # Implementations
├── cli/               # CLI tool
└── build.gradle.kts   # Build configuration
```

## 🌐 Multi-language Support

- **한국어** (ko)
- **English** (en)
- **中文** (zh)
- **日本語** (ja)

## 🔒 Security

- API keys are safely stored in user home directory
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