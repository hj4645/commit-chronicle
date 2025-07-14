# 🚀 CommitChronicle

[![JitPack](https://jitpack.io/v/hj4645/commit-chronicle.svg)](https://jitpack.io/#hj4645/commit-chronicle)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=flat&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![GitHub Issues](https://img.shields.io/github/issues/hj4645/commit-chronicle.svg)](https://github.com/hj4645/commit-chronicle/issues)
[![GitHub Stars](https://img.shields.io/github/stars/hj4645/commit-chronicle.svg)](https://github.com/hj4645/commit-chronicle/stargazers)
[![CI](https://github.com/hj4645/commit-chronicle/workflows/CI/badge.svg)](https://github.com/hj4645/commit-chronicle/actions)

**AI駆動のGitコミット分析＆PR生成ツール**

CommitChronicleは、Gitコミット履歴と複数のAIプロバイダー（OpenAI、Claude、Gemini、DeepSeek、Perplexity）を活用してPull Requestドラフト、変更ログ、コミット要約を自動生成する強力なツールです。

---

## 📖 多言語ドキュメント

- **[한국어 (Korean)](README.md)** - 韓国語ドキュメント
- **[English](README_EN.md)** - 英語ドキュメント  
- **[中文 (Chinese)](README_CN.md)** - 中国語ドキュメント
- **[日本語 (Japanese)](README_JA.md)** - 現在のドキュメント

---

## ✨ 主要機能

🤖 **マルチAI対応** - 5つのAIプロバイダーをサポート：OpenAI、Claude、Gemini、DeepSeek、Perplexity  
📝 **自動PR生成** - GitHubテンプレートの自動検出と多言語PRドラフト生成  
📊 **スマート変更ログ** - コミットタイプ別の自動グループ化と変更ログ生成  
🎯 **インテリジェントコミット要約** - AI基盤のコミット内容分析と要約  
🔧 **テンプレートシステム** - カスタムテンプレートと自動GitHubテンプレート検出  
🌐 **多言語サポート** - 韓国語/英語インターフェースと出力サポート  
⚡ **CLI & プラグイン** - コマンドラインツールとIntelliJ IDEAプラグイン  
🔒 **ブランチ保護** - Git Hookによる自動ブランチクリーンアップ  
☁️ **クラウドデプロイ** - JitPackによる簡単な依存関係管理

## 🏗️ プロジェクト構造

```
commit-chronicle/
├── core/                          # コアライブラリ
│   ├── api/                       # インターフェースとドメインモデル
│   │   └── src/main/kotlin/com/commitchronicle/
│   │       ├── ai/               # AI要約インターフェース
│   │       ├── git/              # Git分析インターフェース
│   │       ├── language/         # 多言語サポート
│   │       └── template/         # テンプレートエンジンインターフェース
│   └── impl/                     # 実装
│       └── src/main/kotlin/com/commitchronicle/
│           ├── ai/               # AIプロバイダー実装
│           │   ├── providers/    # OpenAI, Claude, Gemini, DeepSeek, Perplexity
│           │   └── factory/      # AIファクトリー
│           ├── git/              # JGit実装
│           ├── config/           # ユーザー設定
│           └── template/         # テンプレートエンジン実装
├── cli/                          # CLIモジュール
├── ide-plugin-intellij/          # IntelliJプラグイン
└── .github/                      # GitHub Actions & テンプレート
    ├── workflows/               # CI/CDパイプライン
    └── templates/               # PR/Issueテンプレート
```

## 🚀 クイックスタート

### 📋 要件

- **Java**: 8~24対応（Java 8最小要件）
- **Kotlin**: 1.4.20以上
- **AI APIキー**: OpenAI、Claude、Gemini、DeepSeek、Perplexityのいずれか

### 📦 インストール

#### 1. プロジェクトビルド
```bash
git clone https://github.com/hj4645/commit-chronicle.git
cd commit-chronicle
./gradlew build
```

#### 2. CLI実行ファイル作成
```bash
./gradlew :cli:shadowJar
```

#### 3. JARファイル実行
```bash
java -jar cli/build/libs/commitchronicle-cli-*-all.jar --help
```

## 🖥️ CLI使用方法

### 基本コマンド構造
```bash
java -jar commitchronicle-cli.jar [COMMAND] [OPTIONS]
```

### 🔧 グローバルオプション

| オプション | 説明 | デフォルト | 例 |
|-----------|------|-----------|-----|
| `--path` | Gitリポジトリパス | 現在のディレクトリ | `--path /path/to/repo` |
| `--locale` | 言語設定 | システム設定 | `--locale ko` または `--locale en` |
| `--help` | ヘルプ表示 | - | `--help` |

### 🤖 AIプロバイダー設定

#### サポートされているAIプロバイダー
CLI実行時にインタラクティブにAIプロバイダーを選択できます：

```
AIプロバイダーを選択してください：
1) OpenAI (ChatGPT)
2) Claude (Anthropic) 
3) Gemini (Google)
4) DeepSeek
5) Perplexity

選択 (1-5): 1
```

各プロバイダーにAPIキーが必要です：
- **OpenAI**: https://platform.openai.com/api-keys
- **Claude**: https://console.anthropic.com/
- **Gemini**: https://makersuite.google.com/app/apikey
- **DeepSeek**: https://platform.deepseek.com/
- **Perplexity**: https://www.perplexity.ai/settings/api

### 📝 コミット要約生成

```bash
# 基本要約（過去7日間）
java -jar commitchronicle-cli.jar

# 特定期間の要約
java -jar commitchronicle-cli.jar --days 30

# コミット数制限
java -jar commitchronicle-cli.jar --limit 50

# 特定ブランチの分析
java -jar commitchronicle-cli.jar --branch develop

# 英語で要約生成
java -jar commitchronicle-cli.jar --locale en
```

### 🎯 PRドラフト生成

```bash
# GitHubテンプレートを自動検出してPR作成
java -jar commitchronicle-cli.jar pr --title "新機能追加"

# カスタムテンプレート使用
java -jar commitchronicle-cli.jar pr --template my_template.md

# 特定ブランチ間のPR
java -jar commitchronicle-cli.jar pr --base main --head feature/new-feature

# 英語PR生成
java -jar commitchronicle-cli.jar pr --locale en
```

### 📊 変更ログ生成

```bash
# 基本変更ログ
java -jar commitchronicle-cli.jar changelog

# タイプ別グループ化
java -jar commitchronicle-cli.jar changelog --group

# カスタムテンプレートで変更ログ
java -jar commitchronicle-cli.jar changelog --template changelog_template.md

# 特定タグ間の変更ログ
java -jar commitchronicle-cli.jar changelog --from v1.0.0 --to v2.0.0
```

### 🌍 多言語使用方法

#### 言語設定
```bash
# 韓国語（デフォルト）
java -jar commitchronicle-cli.jar --locale ko

# 英語
java -jar commitchronicle-cli.jar --locale en
```

#### 言語別出力例

**韓国語出力：**
```
✅ 분석완료: 15개의 커밋을 분석했습니다
📝 주요 변경사항:
- 새로운 AI 제공업체 추가 (Claude, Gemini)
- 다국어 지원 구현
- GitHub 템플릿 자동 감지 기능
```

**英語出力：**
```
✅ Analysis complete: Analyzed 15 commits
📝 Key changes:
- Added new AI providers (Claude, Gemini)  
- Implemented multi-language support
- Added GitHub template auto-detection
```

## 🔧 GitHubテンプレート自動検出

### サポートされているテンプレートパス

**PRテンプレート（優先度順）：**
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
- `.github/pull_request_template_ko.md`（韓国語）
- `.github/pull_request_template_en.md`（英語）

### テンプレート変数システム

GitHubテンプレートで使用可能な変数：

```markdown
# {{title}}

## 📝 変更内容
{{commits.summary}}

## 📋 コミット一覧  
{{commits.list}}

## 📁 変更されたファイル
{{commits.files}}

## 👥 貢献者
{{commits.authors}}

## ✅ チェックリスト
- [ ] テスト完了
- [ ] ドキュメント更新  
- [ ] 破壊的変更の確認
- [ ] コードレビュー完了
```

### 自動チェックボックス処理

コミット内容を分析してチェックボックスを自動的にチェックします：

| キーワード | 自動チェック項目 |
|-----------|-----------------|
| `test`, `spec`, `junit` | テスト関連チェックボックス |
| `doc`, `readme`, `documentation` | ドキュメント関連チェックボックス |
| `fix`, `bug`, `bugfix` | バグ修正チェックボックス |
| `feat`, `feature`, `add` | 新機能チェックボックス |
| `refactor`, `refactoring` | リファクタリングチェックボックス |

## 🔌 IntelliJ IDEAプラグイン

### インストール

1. **プラグインビルド：**
   ```bash
   ./gradlew :ide-plugin-intellij:buildPlugin
   ```

2. **プラグインインストール：**
   - IntelliJで`ide-plugin-intellij/build/distributions/CommitChronicle-*.zip`ファイルをインストール

3. **使用方法：**
   - `Tools > CommitChronicle`メニューにアクセス
   - AIプロバイダーとAPIキーを設定
   - 希望する機能を実行

### プラグイン機能

- **コミット要約**：現在のプロジェクトのコミット履歴を要約
- **PR生成**：GitHubテンプレートを自動検出してPRドラフトを作成
- **変更ログ**：リリース用変更ログを自動生成
- **設定管理**：AIプロバイダーと言語設定を管理

## 🛠️ 高度な機能

### Git Hook設定

自動ブランチクリーンアップ用のGit Hook：

```bash
# .git/hooks/post-mergeファイル作成
cat > .git/hooks/post-merge << 'EOF'
#!/bin/bash

# PRマージ後にリモートブランチを自動削除
current_branch=$(git rev-parse --abbrev-ref HEAD)

if [[ "$current_branch" == "main" || "$current_branch" == "develop" ]]; then
    # マージされたブランチを見つけてリモートから削除
    git remote prune origin
    
    # ローカルでマージ済みブランチをクリーンアップ
    git branch --merged | grep -v "main\|develop\|master" | xargs -n 1 git branch -d 2>/dev/null || true
fi
EOF

chmod +x .git/hooks/post-merge
```

### カスタムテンプレート

#### テンプレートファイル作成
```markdown
<!-- custom_pr_template.md -->
# 🚀 {{title}}

## 📋 要約
{{commits.summary}}

## 🔄 変更内容
{{#commits.changes}}
- **{{type}}**: {{message}}
{{/commits.changes}}

## 🧪 テスト
- [ ] ユニットテスト成功
- [ ] 統合テスト成功

## 📖 ドキュメント
- [ ] README更新
- [ ] APIドキュメント更新
```

#### テンプレート使用
```bash
java -jar commitchronicle-cli.jar pr --template custom_pr_template.md
```

## 🏗️ 開発者ガイド

### 新しいAIプロバイダーの追加

1. **AI設定クラス作成：**
```kotlin
// core/impl/src/main/kotlin/com/commitchronicle/ai/providers/newai/config/NewAIConfig.kt
data class NewAIConfig(
    val apiKey: String,
    val model: String = "default-model",
    val maxTokens: Int = 4096
)
```

2. **AI要約器実装：**
```kotlin
// core/impl/src/main/kotlin/com/commitchronicle/ai/providers/newai/NewAISummarizer.kt
class NewAISummarizer(private val config: NewAIConfig) : BaseSummarizer() {
    override suspend fun callAI(prompt: String): String {
        // AI API呼び出しを実装
    }
}
```

3. **ファクトリーに登録：**
```kotlin
// AISummarizerFactory.ktを更新
"newai" -> NewAISummarizer(NewAIConfig(apiKey))
```

### 新しい言語の追加

1. **メッセージファイル作成：**
```properties
# core/impl/src/main/resources/messages_ja.properties（日本語例）
summary.title=コミット要約
pr.title=Pull Requestドラフト
changelog.title=変更ログ
```

2. **ロケールコード追加：**
```kotlin
// core/api/src/main/kotlin/com/commitchronicle/language/Locale.kt
enum class Locale(val code: String, val displayName: String) {
    // ... 既存のコード ...
    JAPANESE("ja", "日本語")
}
```

## 🧪 テスト

### 全テスト実行
```bash
./gradlew test
```

### モジュール別テスト
```bash
# コアモジュールテスト
./gradlew :core:api:test :core:impl:test

# CLIモジュールテスト  
./gradlew :cli:test

# プラグインテスト
./gradlew :ide-plugin-intellij:test
```

### パフォーマンステスト
```bash
# 大量コミット分析テスト
java -jar commitchronicle-cli.jar --limit 1000 --days 365
```

## 📈 パフォーマンス最適化

### 推奨設定

| リポジトリサイズ | 推奨limit | 推奨days | 予想処理時間 |
|----------------|-----------|----------|-------------|
| 小規模 (<100 commits) | 50 | 30 | 10-30秒 |
| 中規模 (100-1000 commits) | 100 | 14 | 30-60秒 |
| 大規模 (1000+ commits) | 200 | 7 | 1-3分 |

### メモリ使用量最適化
```bash
# JVMメモリ設定
java -Xmx2g -Xms512m -jar commitchronicle-cli.jar
```

## 🤝 貢献

### 開発環境セットアップ
```bash
git clone https://github.com/hj4645/commit-chronicle.git
cd commit-chronicle

# 開発ビルド
./gradlew build

# コードスタイルチェック
./gradlew ktlintCheck

# コードスタイル自動修正
./gradlew ktlintFormat
```

### Pull Requestガイドライン

1. **ブランチ命名規則：**
   - `feature/機能名`: 新機能
   - `fix/バグ名`: バグ修正
   - `docs/文書名`: ドキュメント改善
   - `refactor/リファクタリング名`: コードリファクタリング

2. **コミットメッセージ規則：**
   ```
   type(scope): subject
   
   body
   
   footer
   ```

3. **PRチェックリスト：**
   - [ ] テストコード作成
   - [ ] ドキュメント更新
   - [ ] 破壊的変更の確認
   - [ ] コードスタイル遵守

## 🐛 トラブルシューティング

### よくある問題

#### Q: "Unsupported class file major version"エラー
**A:** Javaバージョンを確認し、互換バージョンを使用：
```bash
java -version  # Java 8以上を確認
```

#### Q: AI API呼び出し失敗
**A:** APIキーとネットワーク接続を確認：
```bash
# APIキーを環境変数として設定
export OPENAI_API_KEY="your-api-key"
java -jar commitchronicle-cli.jar
```

#### Q: Gitリポジトリが見つからない
**A:** Gitリポジトリパスを確認：
```bash
# 現在のディレクトリがGitリポジトリかどうか確認
git status

# または明示的にパスを指定
java -jar commitchronicle-cli.jar --path /path/to/git/repo
```

### ログレベル設定
```bash
# デバッグモードで実行
java -Dlogging.level.root=DEBUG -jar commitchronicle-cli.jar
```

## 📄 ライセンス

このプロジェクトはMITライセンスの下で配布されています。詳細は[LICENSE](LICENSE)ファイルを参照してください。

---

## 🔗 関連リンク

- **GitHubリポジトリ**: https://github.com/hj4645/commit-chronicle
- **JitPack**: https://jitpack.io/#hj4645/commit-chronicle
- **Issues**: https://github.com/hj4645/commit-chronicle/issues
- **Wiki**: https://github.com/hj4645/commit-chronicle/wiki

---

**📧 連絡先**: hj4645@example.com  
**🌟 GitHubで星をつける**: https://github.com/hj4645/commit-chronicle 