# Commit Chronicle

[![JitPack](https://jitpack.io/v/hj4645/commit-chronicle.svg)](https://jitpack.io/#hj4645/commit-chronicle)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=flat&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![GitHub Issues](https://img.shields.io/github/issues/hj4645/commit-chronicle.svg)](https://github.com/hj4645/commit-chronicle/issues)
[![GitHub Stars](https://img.shields.io/github/stars/hj4645/commit-chronicle.svg)](https://github.com/hj4645/commit-chronicle/stargazers)
[![CI](https://github.com/hj4645/commit-chronicle/workflows/JitPack%20Release/badge.svg)](https://github.com/hj4645/commit-chronicle/actions)

AIを活用したGitコミット分析・要約ライブラリです。コミット履歴を分析してPull Requestドラフト、変更ログ、コミット要約を自動生成します。

## 🌐 多言語ドキュメント

- **[한국어 (Korean)](README.md)** - 韓国語ドキュメント
- **[English](README_EN.md)** - 英語ドキュメント  
- **[中文 (Chinese)](README_ZH.md)** - 中国語ドキュメント
- **[日本語 (Japanese)](README_JA.md)** - 現在のドキュメント

## 🚀 主な機能

- **AIベースのコミット分析**: OpenAI、Claude、Gemini、Perplexity、DeepSeekをサポート
- **自動PR下書き生成**: コミット履歴に基づいたPRテンプレート作成
- **多言語サポート**: 韓国語、英語、中国語、日本語
- **GitHubテンプレート検出**: 既存のPRテンプレートを自動適用
- **CLIツール**: すぐに使えるコマンドラインインターフェース

## 📦 インストール方法

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

### 1. ライブラリとしてCLI使用

依存関係を追加後、エイリアスを設定してCLIとして使用できます。

#### 🚀 簡単なエイリアス設定方法

**macOS/Linux:**
```bash
# ワンライナーでエイリアス設定
echo "alias cch=\"java -jar \$(find ~/.gradle/caches -name \"*commit-chronicle*\" -type f | grep \"\.jar$\" | head -1)\"" >> ~/.zshrc && source ~/.zshrc

# またはステップ別設定
JAR_PATH=$(find ~/.gradle/caches -name "*commit-chronicle*" -type f | grep "\.jar$" | head -1)
echo "alias cch=\"java -jar $JAR_PATH\"" >> ~/.zshrc
source ~/.zshrc
```

**Windows (PowerShell):**
```powershell
# JARファイルパスを検索
$jarPath = Get-ChildItem -Path "$env:USERPROFILE\.gradle\caches" -Recurse -Name "*commit-chronicle*.jar" | Select-Object -First 1
$fullPath = Join-Path "$env:USERPROFILE\.gradle\caches" $jarPath

# エイリアス設定
echo "function cch { java -jar `"$fullPath`" @args }" >> $PROFILE
. $PROFILE
```

#### 手動設定方法

**JARファイルパス確認：**
```bash
# macOS/Linux
find ~/.gradle/caches -name "*commit-chronicle*" -type f | grep "\.jar$"

# Windows (PowerShell)
Get-ChildItem -Path "$env:USERPROFILE\.gradle\caches" -Recurse -Name "*commit-chronicle*.jar"
```

**エイリアス設定：**
```bash
# macOS/Linux
alias cch="java -jar /実際の/jar/ファイル/パス/commit-chronicle-1.0.0.jar"

# Windows (PowerShell)
function cch { java -jar "C:\実際の\jar\ファイル\パス\commit-chronicle-1.0.0.jar" @args }
```

### 2. 初期設定

初回実行時にインタラクティブな設定が開始されます：

```bash
cch settings
```

**設定プロセス：**

1. **言語選択**
   - 한국어 (Korean)
   - English
   - 中文 (Chinese)
   - 日本語 (Japanese)

2. **AIモデル選択**
   - OpenAI (gpt-4o, gpt-4o-mini, gpt-4-turbo)
   - Claude (claude-3-5-haiku-20241022, claude-3-haiku-20240307, claude-3-5-sonnet-20241022, claude-3-sonnet-20240229, claude-3-7-sonnet-20250219)
   - Gemini (gemini-2.5-pro, gemini-2.0-flash, gemini-2.5-flash)
   - Perplexity (llama-3.1-sonar-large-128k-online, llama-3.1-sonar-small-128k-online)
   - DeepSeek (deepseek-chat, deepseek-coder)

3. **APIキー入力**
   - 選択したAIモデルのAPIキーを入力

4. **分析設定**
   - デフォルト分析期間（日数、デフォルト：7日）
   - 最大コミット数（デフォルト：50個）

**設定例：**
```
Update current settings

Select setting to update:
Selected: Language

Select language (current: en):
Selected: 日本語 (Japanese)
Language updated to: ja (AI responses will use this language)

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

### 3. CLIコマンド使用

#### コミット要約生成

```bash
# デフォルト設定を使用（設定された期間とコミット数）
cch summarize

# オプション指定
cch summarize -d 14 -l 100           # 14日間、最大100コミット
cch summarize --days 7 --limit 50    # 7日間、最大50コミット
cch summarize --path /path/to/repo   # 特定のリポジトリパス
```

#### PR下書き生成

```bash
# 現在のブランチのPR下書きを生成
cch pr

# オプション指定
cch pr -d 7 -l 20                    # 7日間、最大20コミット
cch pr --path /path/to/repo          # 特定のリポジトリパス
```

#### 設定管理

```bash
# 設定変更（インタラクティブメニュー）
cch settings

# ヘルプ表示
cch settings --help
cch --help
```

### 4. キーボード方向キー設定方法

設定メニューではキーボード方向キーを使用してオプションを選択できます：

- **↑/↓ 矢印**: オプション選択
- **Enter**: 選択確認
- **Esc**: 前のメニューに戻る

**設定メニュー構造：**
```
Main Menu
├── Language (言語設定)
├── AI Provider (AIモデル設定)
│   ├── Provider Selection
│   ├── Model Selection  
│   └── API Key Input
├── Analysis Settings (分析設定)
│   ├── Default Days
│   └── Commit Limit
└── Done (完了)
```

## 🔧 設定ファイル

設定は `~/.commit-chronicle/config.json` にローカル保存されます：

```json
{
  "apiKey": "your-api-key",
  "providerType": "openai",
  "locale": "ja",
  "defaultDays": 7,
  "defaultLimit": 50
}
```

**設定の永続性：**
- 一度設定すると継続して保持されます
- 異なるプロジェクトでも同じ設定を使用
- 必要に応じて `cch settings` でいつでも変更可能

## 🔧 GitHubテンプレート自動検出

### サポートされるテンプレートパス

**PRテンプレート（優先順位順）：**
```
.github/pull_request_template_[locale].md    # 多言語サポート
.github/pull_request_template.md
.github/PULL_REQUEST_TEMPLATE.md
.github/PULL_REQUEST_TEMPLATE/pull_request_template.md
docs/pull_request_template.md
docs/PULL_REQUEST_TEMPLATE.md
pull_request_template.md
PULL_REQUEST_TEMPLATE.md
```

**多言語テンプレート例：**
- `.github/pull_request_template_ko.md` (韓国語)
- `.github/pull_request_template_en.md` (英語)
- `.github/pull_request_template_zh.md` (中国語)
- `.github/pull_request_template_ja.md` (日本語)

### テンプレート適用方式

1. **自動検出**: 上記パスでテンプレートファイルを自動的に検索
2. **言語別優先順位**: 設定された言語に合うテンプレートを優先適用
3. **フォールバック処理**: 該当言語テンプレートがない場合はデフォルトテンプレートを使用
4. **動的適用**: AIがテンプレート構造に合わせて内容を自動生成

## 🤖 サポートされるAIモデル

| プロバイダー | モデル |
|-------------|--------|
| OpenAI | gpt-4o, gpt-4o-mini, gpt-4-turbo |
| Claude | claude-3-5-haiku-20241022, claude-3-haiku-20240307, claude-3-5-sonnet-20241022, claude-3-sonnet-20240229, claude-3-7-sonnet-20250219 |
| Gemini | gemini-2.5-pro, gemini-2.0-flash, gemini-2.5-flash |
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

- Issues: [GitHub Issues](https://github.com/hj4645/commit-chronicle/issues)
- Documentation: [Wiki](https://github.com/hj4645/commit-chronicle/wiki) 