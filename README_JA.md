# Commit Chronicle

AIを活用したGitコミット分析・要約ライブラリです。コミット履歴を分析してPull Requestドラフト、変更ログ、コミット要約を自動生成します。

## 🚀 主な機能

- **AIベースのコミット分析**: OpenAI、Claude、Gemini、Perplexity、DeepSeekをサポート
- **自動PR下書き生成**: コミット履歴に基づいたPRテンプレート作成
- **多言語サポート**: 韓国語、英語、中国語、日本語
- **GitHubテンプレート検出**: 既存のPRテンプレートを自動適用
- **ブランチ検証**: main/masterブランチでの警告と意味のあるコミットのフィルタリング
- **CLIツール**: すぐに使えるコマンドラインインターフェース

## 📦 インストール方法

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

### CLI使用法

#### 1. 初期設定

```bash
java -jar commitchronicle-0.1.0.jar
```

初回実行時にインタラクティブな設定が開始されます：
- 言語選択（韓国語、English、中文、日本語）
- AIモデル選択（OpenAI、Claude、Gemini、Perplexity、DeepSeek）
- APIキー入力

#### 2. コミット要約生成

```bash
# デフォルト設定（過去7日間、最大50コミット）
java -jar commitchronicle-0.1.0.jar summarize

# オプション指定
java -jar commitchronicle-0.1.0.jar summarize -d 14 -l 100
java -jar commitchronicle-0.1.0.jar summarize --path /path/to/repo
```

#### 3. PR下書き生成

```bash
# 現在のブランチのPR下書きを生成
java -jar commitchronicle-0.1.0.jar pr

# オプション指定
java -jar commitchronicle-0.1.0.jar pr -d 7 -l 20
```

#### 4. 設定管理

```bash
# 現在の設定を表示
java -jar commitchronicle-0.1.0.jar settings --show

# 設定を変更
java -jar commitchronicle-0.1.0.jar settings

# 設定をリセット
java -jar commitchronicle-0.1.0.jar settings --reset
```

### ライブラリ使用法

```kotlin
import com.commitchronicle.git.GitAnalyzerFactory
import com.commitchronicle.ai.factory.AISummarizerFactory
import com.commitchronicle.ai.providers.openai.config.OpenAIConfig
import com.commitchronicle.ai.AIProviderType
import com.commitchronicle.language.Locale

// Git分析器を作成
val gitAnalyzer = GitAnalyzerFactory.create("/path/to/repo")

// AI設定
val aiConfig = OpenAIConfig(
    apiKey = "your-api-key",
    locale = Locale.JAPANESE
)
val aiSummarizer = AISummarizerFactory.create(aiConfig, AIProviderType.OPENAI)

// コミット分析
val commits = gitAnalyzer.getCommits(7) // 過去7日間
val summary = aiSummarizer.summarize(commits)
val prDraft = aiSummarizer.generatePRDraft(commits)

println("要約: $summary")
println("PR下書き: $prDraft")
```

## 🔧 設定

設定は `~/.commit-chronicle/config.json` に保存されます：

```json
{
  "apiKey": "your-api-key",
  "providerType": "openai",
  "locale": "ja",
  "defaultDays": 7,
  "defaultLimit": 50
}
```

## 🤖 サポートされるAIモデル

| プロバイダー | モデル |
|-------------|--------|
| OpenAI | gpt-4o, gpt-4o-mini, gpt-4-turbo |
| Claude | claude-3-5-sonnet-20241022, claude-3-5-haiku-20241022 |
| Gemini | gemini-2.0-flash-exp, gemini-1.5-flash, gemini-1.5-pro |
| Perplexity | llama-3.1-sonar-large-128k-online, llama-3.1-sonar-small-128k-online |
| DeepSeek | deepseek-chat, deepseek-coder |

## 📁 プロジェクト構造

```
commit-chronicle/
├── core/
│   ├── api/           # インターフェースとモデル
│   └── impl/          # 実装
├── cli/               # CLIツール
└── build.gradle.kts   # ビルド設定
```

## 🌐 多言語サポート

- **한국어** (ko)
- **English** (en)
- **中文** (zh)
- **日本語** (ja)

## 🔒 セキュリティ

- APIキーはユーザーのホームディレクトリに安全に保存
- JARファイルに機密情報は含まれません
- ユーザーごとの独立した設定管理

## 📄 ライセンス

MIT License

## 🤝 貢献

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📞 サポート

- Issues: [GitHub Issues](https://github.com/eulji/commit-chronicle/issues)
- Documentation: [Wiki](https://github.com/eulji/commit-chronicle/wiki) 