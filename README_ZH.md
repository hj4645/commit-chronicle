# Commit Chronicle

基于AI的Git提交分析和摘要库。通过分析提交历史自动生成Pull Request草稿、更改日志和提交摘要。

## 🚀 主要功能

- **AI驱动的提交分析**: 支持OpenAI、Claude、Gemini、Perplexity、DeepSeek
- **自动PR草稿生成**: 基于提交历史创建PR模板
- **多语言支持**: 韩语、英语、中文、日语
- **GitHub模板检测**: 自动应用现有的PR模板
- **分支验证**: 在main/master分支上警告并过滤有意义的提交
- **CLI工具**: 即用型命令行界面

## 📦 安装方法

### Gradle

```kotlin
repositories {
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("com.github.eulji:commit-chronicle:v0.1.0")
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
        <groupId>com.github.eulji</groupId>
        <artifactId>commit-chronicle</artifactId>
        <version>v0.1.0</version>
    </dependency>
</dependencies>
```

## 🛠️ 使用方法

### CLI使用

#### 1. 初始设置

```bash
java -jar commitchronicle-0.1.0.jar
```

首次运行时启动交互式设置：
- 语言选择（韩语、English、中文、日本語）
- AI模型选择（OpenAI、Claude、Gemini、Perplexity、DeepSeek）
- API密钥输入

#### 2. 生成提交摘要

```bash
# 默认设置（最近7天，最多50个提交）
java -jar commitchronicle-0.1.0.jar summarize

# 使用选项
java -jar commitchronicle-0.1.0.jar summarize -d 14 -l 100
java -jar commitchronicle-0.1.0.jar summarize --path /path/to/repo
```

#### 3. 生成PR草稿

```bash
# 为当前分支生成PR草稿
java -jar commitchronicle-0.1.0.jar pr

# 使用选项
java -jar commitchronicle-0.1.0.jar pr -d 7 -l 20
```

#### 4. 设置管理

```bash
# 显示当前设置
java -jar commitchronicle-0.1.0.jar settings --show

# 更新设置
java -jar commitchronicle-0.1.0.jar settings

# 重置设置
java -jar commitchronicle-0.1.0.jar settings --reset
```

### 库使用

```kotlin
import com.commitchronicle.git.GitAnalyzerFactory
import com.commitchronicle.ai.factory.AISummarizerFactory
import com.commitchronicle.ai.providers.openai.config.OpenAIConfig
import com.commitchronicle.ai.AIProviderType
import com.commitchronicle.language.Locale

// 创建Git分析器
val gitAnalyzer = GitAnalyzerFactory.create("/path/to/repo")

// AI配置
val aiConfig = OpenAIConfig(
    apiKey = "your-api-key",
    locale = Locale.CHINESE
)
val aiSummarizer = AISummarizerFactory.create(aiConfig, AIProviderType.OPENAI)

// 分析提交
val commits = gitAnalyzer.getCommits(7) // 最近7天
val summary = aiSummarizer.summarize(commits)
val prDraft = aiSummarizer.generatePRDraft(commits)

println("摘要: $summary")
println("PR草稿: $prDraft")
```

## 🔧 配置

设置存储在 `~/.commit-chronicle/config.json`：

```json
{
  "apiKey": "your-api-key",
  "providerType": "openai",
  "locale": "zh",
  "defaultDays": 7,
  "defaultLimit": 50
}
```

## 🤖 支持的AI模型

| 提供商 | 模型 |
|-------|------|
| OpenAI | gpt-4o, gpt-4o-mini, gpt-4-turbo |
| Claude | claude-3-5-sonnet-20241022, claude-3-5-haiku-20241022 |
| Gemini | gemini-2.0-flash-exp, gemini-1.5-flash, gemini-1.5-pro |
| Perplexity | llama-3.1-sonar-large-128k-online, llama-3.1-sonar-small-128k-online |
| DeepSeek | deepseek-chat, deepseek-coder |

## 📁 项目结构

```
commit-chronicle/
├── core/
│   ├── api/           # 接口和模型
│   └── impl/          # 实现
├── cli/               # CLI工具
└── build.gradle.kts   # 构建配置
```

## 🌐 多语言支持

- **한국어** (ko)
- **English** (en)
- **中文** (zh)
- **日本語** (ja)

## 🔒 安全性

- API密钥安全存储在用户主目录
- JAR文件中不包含敏感信息
- 每个用户独立的配置管理

## 📄 许可证

MIT License

## 🤝 贡献

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📞 支持

- Issues: [GitHub Issues](https://github.com/eulji/commit-chronicle/issues)
- Documentation: [Wiki](https://github.com/eulji/commit-chronicle/wiki) 