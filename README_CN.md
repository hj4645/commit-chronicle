# 🚀 CommitChronicle

[![JitPack](https://jitpack.io/v/hj4645/commit-chronicle.svg)](https://jitpack.io/#hj4645/commit-chronicle)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=flat&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![GitHub Issues](https://img.shields.io/github/issues/hj4645/commit-chronicle.svg)](https://github.com/hj4645/commit-chronicle/issues)
[![GitHub Stars](https://img.shields.io/github/stars/hj4645/commit-chronicle.svg)](https://github.com/hj4645/commit-chronicle/stargazers)
[![CI](https://github.com/hj4645/commit-chronicle/workflows/CI/badge.svg)](https://github.com/hj4645/commit-chronicle/actions)

**AI驱动的Git提交分析和PR生成工具**

CommitChronicle 是一个强大的工具，利用Git提交历史和多种AI提供商（OpenAI、Claude、Gemini、DeepSeek、Perplexity）自动生成Pull Request草稿、变更日志和提交摘要。

---

## 📖 多语言文档

- **[한국어 (Korean)](README.md)** - 韩语文档
- **[English](README_EN.md)** - 英语文档  
- **[中文 (Chinese)](README_CN.md)** - 当前文档
- **[日本語 (Japanese)](README_JA.md)** - 日语文档

---

## ✨ 主要功能

🤖 **多AI支持** - 支持5个AI提供商：OpenAI、Claude、Gemini、DeepSeek、Perplexity  
📝 **自动PR生成** - 自动检测GitHub模板并生成多语言PR草稿  
📊 **智能变更日志** - 按提交类型自动分组和生成变更日志  
🎯 **智能提交摘要** - 基于AI的提交内容分析和摘要  
🔧 **模板系统** - 自定义模板和自动GitHub模板检测  
🌐 **多语言支持** - 韩语/英语界面和输出支持  
⚡ **CLI & 插件** - 命令行工具和IntelliJ IDEA插件  
🔒 **分支保护** - 通过Git Hook自动清理分支  
☁️ **云部署** - 通过JitPack轻松管理依赖

## 🏗️ 项目结构

```
commit-chronicle/
├── core/                          # 核心库
│   ├── api/                       # 接口和领域模型
│   │   └── src/main/kotlin/com/commitchronicle/
│   │       ├── ai/               # AI摘要接口
│   │       ├── git/              # Git分析接口
│   │       ├── language/         # 多语言支持
│   │       └── template/         # 模板引擎接口
│   └── impl/                     # 实现
│       └── src/main/kotlin/com/commitchronicle/
│           ├── ai/               # AI提供商实现
│           │   ├── providers/    # OpenAI, Claude, Gemini, DeepSeek, Perplexity
│           │   └── factory/      # AI工厂
│           ├── git/              # JGit实现
│           ├── config/           # 用户配置
│           └── template/         # 模板引擎实现
├── cli/                          # CLI模块
├── ide-plugin-intellij/          # IntelliJ插件
└── .github/                      # GitHub Actions & 模板
    ├── workflows/               # CI/CD管道
    └── templates/               # PR/Issue模板
```

## 🚀 快速开始

### 📋 要求

- **Java**: 支持8~24（最低要求Java 8）
- **Kotlin**: 1.4.20或更高版本
- **AI API密钥**: OpenAI、Claude、Gemini、DeepSeek、Perplexity之一

### 📦 安装

#### 1. 构建项目
```bash
git clone https://github.com/hj4645/commit-chronicle.git
cd commit-chronicle
./gradlew build
```

#### 2. 创建CLI可执行文件
```bash
./gradlew :cli:shadowJar
```

#### 3. 运行JAR文件
```bash
java -jar cli/build/libs/commitchronicle-cli-*-all.jar --help
```

## 🖥️ CLI使用方法

### 基本命令结构
```bash
java -jar commitchronicle-cli.jar [COMMAND] [OPTIONS]
```

### 🔧 全局选项

| 选项 | 说明 | 默认值 | 示例 |
|------|------|--------|------|
| `--path` | Git仓库路径 | 当前目录 | `--path /path/to/repo` |
| `--locale` | 语言设置 | 系统设置 | `--locale ko` 或 `--locale en` |
| `--help` | 显示帮助 | - | `--help` |

### 🤖 AI提供商设置

#### 支持的AI提供商
运行CLI时可以交互式选择AI提供商：

```
请选择AI提供商：
1) OpenAI (ChatGPT)
2) Claude (Anthropic) 
3) Gemini (Google)
4) DeepSeek
5) Perplexity

选择 (1-5): 1
```

每个提供商都需要API密钥：
- **OpenAI**: https://platform.openai.com/api-keys
- **Claude**: https://console.anthropic.com/
- **Gemini**: https://makersuite.google.com/app/apikey
- **DeepSeek**: https://platform.deepseek.com/
- **Perplexity**: https://www.perplexity.ai/settings/api

### 📝 提交摘要生成

```bash
# 基本摘要（最近7天）
java -jar commitchronicle-cli.jar

# 指定时间段摘要
java -jar commitchronicle-cli.jar --days 30

# 限制提交数量
java -jar commitchronicle-cli.jar --limit 50

# 分析指定分支
java -jar commitchronicle-cli.jar --branch develop

# 生成英语摘要
java -jar commitchronicle-cli.jar --locale en
```

### 🎯 PR草稿生成

```bash
# 自动检测GitHub模板并创建PR
java -jar commitchronicle-cli.jar pr --title "添加新功能"

# 使用自定义模板
java -jar commitchronicle-cli.jar pr --template my_template.md

# 指定分支间的PR
java -jar commitchronicle-cli.jar pr --base main --head feature/new-feature

# 生成英语PR
java -jar commitchronicle-cli.jar pr --locale en
```

### 📊 变更日志生成

```bash
# 基本变更日志
java -jar commitchronicle-cli.jar changelog

# 按类型分组
java -jar commitchronicle-cli.jar changelog --group

# 使用自定义模板生成变更日志
java -jar commitchronicle-cli.jar changelog --template changelog_template.md

# 指定标签间的变更日志
java -jar commitchronicle-cli.jar changelog --from v1.0.0 --to v2.0.0
```

### 🌍 多语言使用

#### 语言设置
```bash
# 韩语（默认）
java -jar commitchronicle-cli.jar --locale ko

# 英语
java -jar commitchronicle-cli.jar --locale en
```

#### 语言特定输出示例

**韩语输出：**
```
✅ 分析완료: 15개의 커밋을 분석했습니다
📝 주요 변경사항:
- 새로운 AI 제공업체 추가 (Claude, Gemini)
- 다국어 지원 구현
- GitHub 템플릿 자동 감지 기능
```

**英语输出：**
```
✅ Analysis complete: Analyzed 15 commits
📝 Key changes:
- Added new AI providers (Claude, Gemini)  
- Implemented multi-language support
- Added GitHub template auto-detection
```

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
- `.github/pull_request_template_ko.md`（韩语）
- `.github/pull_request_template_en.md`（英语）

### 模板变量系统

GitHub模板中可用的变量：

```markdown
# {{title}}

## 📝 更改
{{commits.summary}}

## 📋 提交列表  
{{commits.list}}

## 📁 更改的文件
{{commits.files}}

## 👥 贡献者
{{commits.authors}}

## ✅ 检查清单
- [ ] 测试完成
- [ ] 文档更新  
- [ ] 重大更改验证
- [ ] 代码审查完成
```

### 自动复选框处理

通过分析提交内容自动勾选复选框：

| 关键词 | 自动勾选项目 |
|--------|-------------|
| `test`, `spec`, `junit` | 测试相关复选框 |
| `doc`, `readme`, `documentation` | 文档相关复选框 |
| `fix`, `bug`, `bugfix` | 错误修复复选框 |
| `feat`, `feature`, `add` | 新功能复选框 |
| `refactor`, `refactoring` | 重构复选框 |

## 🔌 IntelliJ IDEA插件

### 安装

1. **构建插件：**
   ```bash
   ./gradlew :ide-plugin-intellij:buildPlugin
   ```

2. **安装插件：**
   - 在IntelliJ中安装`ide-plugin-intellij/build/distributions/CommitChronicle-*.zip`文件

3. **使用：**
   - 访问`Tools > CommitChronicle`菜单
   - 配置AI提供商和API密钥
   - 执行所需功能

### 插件功能

- **提交摘要**：摘要当前项目的提交历史
- **PR生成**：自动检测GitHub模板并创建PR草稿
- **变更日志**：自动生成发布变更日志
- **设置管理**：配置AI提供商和语言设置

## 🛠️ 高级功能

### Git Hook设置

用于自动分支清理的Git Hook：

```bash
# 创建.git/hooks/post-merge文件
cat > .git/hooks/post-merge << 'EOF'
#!/bin/bash

# PR合并后自动删除远程分支
current_branch=$(git rev-parse --abbrev-ref HEAD)

if [[ "$current_branch" == "main" || "$current_branch" == "develop" ]]; then
    # 查找并删除已合并的远程分支
    git remote prune origin
    
    # 清理本地已合并的分支
    git branch --merged | grep -v "main\|develop\|master" | xargs -n 1 git branch -d 2>/dev/null || true
fi
EOF

chmod +x .git/hooks/post-merge
```

### 自定义模板

#### 创建模板文件
```markdown
<!-- custom_pr_template.md -->
# 🚀 {{title}}

## 📋 摘要
{{commits.summary}}

## 🔄 更改
{{#commits.changes}}
- **{{type}}**: {{message}}
{{/commits.changes}}

## 🧪 测试
- [ ] 单元测试通过
- [ ] 集成测试通过

## 📖 文档
- [ ] README更新
- [ ] API文档更新
```

#### 使用模板
```bash
java -jar commitchronicle-cli.jar pr --template custom_pr_template.md
```

## 🏗️ 开发者指南

### 添加新的AI提供商

1. **创建AI配置类：**
```kotlin
// core/impl/src/main/kotlin/com/commitchronicle/ai/providers/newai/config/NewAIConfig.kt
data class NewAIConfig(
    val apiKey: String,
    val model: String = "default-model",
    val maxTokens: Int = 4096
)
```

2. **实现AI摘要器：**
```kotlin
// core/impl/src/main/kotlin/com/commitchronicle/ai/providers/newai/NewAISummarizer.kt
class NewAISummarizer(private val config: NewAIConfig) : BaseSummarizer() {
    override suspend fun callAI(prompt: String): String {
        // 实现AI API调用
    }
}
```

3. **在工厂中注册：**
```kotlin
// 更新AISummarizerFactory.kt
"newai" -> NewAISummarizer(NewAIConfig(apiKey))
```

### 添加新语言

1. **创建消息文件：**
```properties
# core/impl/src/main/resources/messages_zh.properties（中文示例）
summary.title=提交摘要
pr.title=Pull Request草稿
changelog.title=变更日志
```

2. **添加语言环境代码：**
```kotlin
// core/api/src/main/kotlin/com/commitchronicle/language/Locale.kt
enum class Locale(val code: String, val displayName: String) {
    // ... 现有代码 ...
    CHINESE("zh", "中文")
}
```

## 🧪 测试

### 运行所有测试
```bash
./gradlew test
```

### 模块特定测试
```bash
# 核心模块测试
./gradlew :core:api:test :core:impl:test

# CLI模块测试  
./gradlew :cli:test

# 插件测试
./gradlew :ide-plugin-intellij:test
```

### 性能测试
```bash
# 大量提交分析测试
java -jar commitchronicle-cli.jar --limit 1000 --days 365
```

## 📈 性能优化

### 推荐设置

| 仓库大小 | 推荐limit | 推荐days | 预期处理时间 |
|---------|-----------|----------|-------------|
| 小型 (<100 commits) | 50 | 30 | 10-30秒 |
| 中型 (100-1000 commits) | 100 | 14 | 30-60秒 |
| 大型 (1000+ commits) | 200 | 7 | 1-3分钟 |

### 内存使用优化
```bash
# JVM内存设置
java -Xmx2g -Xms512m -jar commitchronicle-cli.jar
```

## 🤝 贡献

### 开发环境设置
```bash
git clone https://github.com/hj4645/commit-chronicle.git
cd commit-chronicle

# 开发构建
./gradlew build

# 代码风格检查
./gradlew ktlintCheck

# 自动修复代码风格
./gradlew ktlintFormat
```

### Pull Request指南

1. **分支命名约定：**
   - `feature/功能名`: 新功能
   - `fix/错误名`: 错误修复
   - `docs/文档名`: 文档改进
   - `refactor/重构名`: 代码重构

2. **提交消息约定：**
   ```
   type(scope): subject
   
   body
   
   footer
   ```

3. **PR检查清单：**
   - [ ] 编写测试代码
   - [ ] 更新文档
   - [ ] 验证重大更改
   - [ ] 代码风格合规

## 🐛 故障排除

### 常见问题

#### Q: "Unsupported class file major version"错误
**A:** 检查Java版本并使用兼容版本：
```bash
java -version  # 验证Java 8或更高版本
```

#### Q: AI API调用失败
**A:** 检查API密钥和网络连接：
```bash
# 将API密钥设置为环境变量
export OPENAI_API_KEY="your-api-key"
java -jar commitchronicle-cli.jar
```

#### Q: 找不到Git仓库
**A:** 验证Git仓库路径：
```bash
# 检查当前目录是否为Git仓库
git status

# 或明确指定路径
java -jar commitchronicle-cli.jar --path /path/to/git/repo
```

### 日志级别配置
```bash
# 在调试模式下运行
java -Dlogging.level.root=DEBUG -jar commitchronicle-cli.jar
```

## 📄 许可证

本项目在MIT许可证下分发。详情请参见[LICENSE](LICENSE)文件。

---

## 🔗 相关链接

- **GitHub仓库**: https://github.com/hj4645/commit-chronicle
- **JitPack**: https://jitpack.io/#hj4645/commit-chronicle
- **Issues**: https://github.com/hj4645/commit-chronicle/issues
- **Wiki**: https://github.com/hj4645/commit-chronicle/wiki

---

**📧 联系**: hj4645@example.com  
**🌟 在GitHub上给我们星星**: https://github.com/hj4645/commit-chronicle 