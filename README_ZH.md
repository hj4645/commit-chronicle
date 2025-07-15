# Commit Chronicle

[![JitPack](https://jitpack.io/v/hj4645/commit-chronicle.svg)](https://jitpack.io/#hj4645/commit-chronicle)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=flat&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![GitHub Issues](https://img.shields.io/github/issues/hj4645/commit-chronicle.svg)](https://github.com/hj4645/commit-chronicle/issues)
[![GitHub Stars](https://img.shields.io/github/stars/hj4645/commit-chronicle.svg)](https://github.com/hj4645/commit-chronicle/stargazers)
[![CI](https://github.com/hj4645/commit-chronicle/workflows/JitPack%20Release/badge.svg)](https://github.com/hj4645/commit-chronicle/actions)

基于AI的Git提交分析和总结库。分析提交历史，自动生成Pull Request草稿、变更日志和提交摘要。

## 🌐 多语言文档

- **[한국어 (Korean)](README.md)** - 韩语文档
- **[English](README_EN.md)** - 英文文档  
- **[中文 (Chinese)](README_ZH.md)** - 当前文档
- **[日本語 (Japanese)](README_JA.md)** - 日语文档

## 🚀 主要功能

- **AI驱动的提交分析**: 支持OpenAI、Claude、Gemini、Perplexity、DeepSeek
- **自动PR草稿生成**: 基于提交历史创建PR模板
- **多语言支持**: 韩语、英语、中文、日语
- **GitHub模板检测**: 自动应用现有PR模板
- **CLI工具**: 命令行直接使用

## 📦 安装方法

### Gradle

```kotlin
repositories {
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("com.github.hj4645:commit-chronicle:1.0.0")
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
        <version>1.0.0</version>
    </dependency>
</dependencies>
```

## 🛠️ 使用方法

### 1. 作为库使用CLI

添加依赖后，可以设置别名来使用CLI：

```bash
# 在Gradle缓存中查找JAR文件路径
find ~/.gradle/caches -name "commitchronicle-0.1.0.jar" -type f

# 设置别名（示例 - 需要修改为实际路径）
alias cch="java -jar ~/.gradle/caches/modules-2/files-2.1/com.github.hj4645/commit-chronicle/v0.1.0/*/commitchronicle-0.1.0.jar"

# 或使用直接下载的JAR文件
alias cch="java -jar /path/to/commitchronicle-0.1.0.jar"
```

### 2. 初始设置

首次运行时会启动交互式设置：

```bash
cch settings
```

**设置过程：**

1. **语言选择**
   - 한국어 (Korean)
   - English
   - 中文 (Chinese)
   - 日本語 (Japanese)

2. **AI模型选择**
   - OpenAI (gpt-4o, gpt-4o-mini, gpt-4-turbo)
   - Claude (claude-3-5-sonnet-20241022, claude-3-5-haiku-20241022)
   - Gemini (gemini-2.0-flash-exp, gemini-1.5-flash, gemini-1.5-pro)
   - Perplexity (llama-3.1-sonar-large-128k-online, llama-3.1-sonar-small-128k-online)
   - DeepSeek (deepseek-chat, deepseek-coder)

3. **API密钥输入**
   - 输入所选AI模型的API密钥

4. **分析设置**
   - 默认分析期间（天数，默认：7天）
   - 最大提交数（默认：50个）

**设置示例：**
```
Update current settings

Select setting to update:
Selected: Language

Select language (current: en):
Selected: 中文 (Chinese)
Language updated to: zh (AI responses will use this language)

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

### 3. CLI命令使用

#### 生成提交摘要

```bash
# 使用默认设置（配置的期间和提交数）
cch summarize

# 使用选项
cch summarize -d 14 -l 100           # 14天，最多100个提交
cch summarize --days 7 --limit 50    # 7天，最多50个提交
cch summarize --path /path/to/repo   # 特定仓库路径
```

#### 生成PR草稿

```bash
# 为当前分支生成PR草稿
cch pr

# 使用选项
cch pr -d 7 -l 20                    # 7天，最多20个提交
cch pr --path /path/to/repo          # 特定仓库路径
```

#### 设置管理

```bash
# 更改设置（交互式菜单）
cch settings

# 查看帮助
cch settings --help
cch --help
```

### 4. 键盘方向键设置方法

设置菜单中可以使用键盘方向键选择选项：

- **↑/↓ 箭头**: 选择选项
- **Enter**: 确认选择
- **Esc**: 返回上一级菜单

**设置菜单结构：**
```
Main Menu
├── Language (语言设置)
├── AI Provider (AI模型设置)
│   ├── Provider Selection
│   ├── Model Selection  
│   └── API Key Input
├── Analysis Settings (分析设置)
│   ├── Default Days
│   └── Commit Limit
└── Done (完成)
```

## 🔧 配置文件

设置本地保存在 `~/.commit-chronicle/config.json`：

```json
{
  "apiKey": "your-api-key",
  "providerType": "openai",
  "locale": "zh",
  "defaultDays": 7,
  "defaultLimit": 50
}
```

**配置持久性：**
- 一次设置后会持续保持
- 不同项目使用相同设置
- 可随时通过 `cch settings` 更改

## 🔧 GitHub模板自动检测

### 支持的模板路径

**PR模板（按优先级排序）：**
```
.github/pull_request_template_[locale].md    # 多语言支持
.github/pull_request_template.md
.github/PULL_REQUEST_TEMPLATE.md
.github/PULL_REQUEST_TEMPLATE/pull_request_template.md
docs/pull_request_template.md
docs/PULL_REQUEST_TEMPLATE.md
pull_request_template.md
PULL_REQUEST_TEMPLATE.md
```

**多语言模板示例：**
- `.github/pull_request_template_ko.md` (韩语)
- `.github/pull_request_template_en.md` (英语)
- `.github/pull_request_template_zh.md` (中文)
- `.github/pull_request_template_ja.md` (日语)

### 模板应用方式

1. **自动检测**: 在上述路径中自动查找模板文件
2. **语言优先级**: 优先应用与配置语言匹配的模板
3. **回退处理**: 如果没有对应语言模板则使用默认模板
4. **动态应用**: AI自动生成符合模板结构的内容

## 🤖 支持的AI模型

| 提供商 | 模型 |
|--------|------|
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

- Issues: [GitHub Issues](https://github.com/hj4645/commit-chronicle/issues)
- Documentation: [Wiki](https://github.com/hj4645/commit-chronicle/wiki) 