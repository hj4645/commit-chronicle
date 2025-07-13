package com.commitchronicle.cli

import com.commitchronicle.ai.AIProviderConfig
import com.commitchronicle.ai.AIProviderType
import com.commitchronicle.ai.factory.AISummarizerFactory
import com.commitchronicle.ai.providers.claude.config.ClaudeConfig
import com.commitchronicle.ai.providers.deepseek.config.DeepSeekConfig
import com.commitchronicle.ai.providers.gemini.config.GeminiConfig
import com.commitchronicle.ai.providers.openai.config.OpenAIConfig
import com.commitchronicle.ai.providers.perplexity.config.PerplexityConfig
import com.commitchronicle.git.GitAnalyzer
import com.commitchronicle.git.GitAnalyzerFactory
import com.commitchronicle.git.model.Commit
import com.commitchronicle.template.TemplateEngineFactory
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.path
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.io.path.absolutePathString
import com.commitchronicle.config.UserConfig
import com.commitchronicle.language.MessageBundle
import com.commitchronicle.language.MessageBundleImpl
import com.commitchronicle.language.Locale

object MessageBundleProvider {
    private val messageBundle: MessageBundle = MessageBundleImpl()
    private var currentLocale: Locale = Locale.ENGLISH
    
    fun setLocale(locale: Locale) {
        currentLocale = locale
    }
    
    fun getMessage(key: String): String {
        return messageBundle.getMessage(key, currentLocale)
    }
}

// 설정 커맨드
class Config : CliktCommand(
    name = "config",
    help = MessageBundleProvider.getMessage("command.config.description")
) {
    private val apiKey by option("-k", "--key", help = MessageBundleProvider.getMessage("option.api-key.description"))
    private val providerType by option("-p", "--provider", help = MessageBundleProvider.getMessage("option.provider.description"))
        .choice("openai", "claude", "perplexity", "deepseek", "gemini", ignoreCase = true)
    private val locale by option("--locale", help = MessageBundleProvider.getMessage("option.locale.description"))
        .choice("en", "ko", ignoreCase = true)
    private val show by option("--show", help = MessageBundleProvider.getMessage("option.show.description")).flag()
    private val reset by option("--reset", help = MessageBundleProvider.getMessage("option.reset.description")).flag()

    override fun run() {
        // locale이 설정되어 있으면 먼저 적용
        if (locale != null) {
            val newLocale = Locale.fromCode(locale!!)
            MessageBundleProvider.setLocale(newLocale)
            echo("로케일이 ${newLocale.languageName}로 설정되었습니다.")
        }

        if (reset) {
            UserConfig.save(UserConfig())
            echo(MessageBundleProvider.getMessage("message.config.reset"))
            return
        }

        if (show) {
            val config = UserConfig.load()
            echo("""
                ${MessageBundleProvider.getMessage("message.config.current")}:
                - ${MessageBundleProvider.getMessage("option.api-key.description")}: ${config.apiKey?.let { "***" } ?: MessageBundleProvider.getMessage("message.config.not-set")}
                - ${MessageBundleProvider.getMessage("option.provider.description")}: ${config.providerType ?: MessageBundleProvider.getMessage("message.config.not-set")}
                - ${MessageBundleProvider.getMessage("option.locale.description")}: ${config.locale ?: MessageBundleProvider.getMessage("message.config.not-set")}
            """.trimIndent())
            return
        }

        val currentConfig = UserConfig.load()
        val newConfig = currentConfig.copy(
            apiKey = apiKey ?: currentConfig.apiKey,
            providerType = providerType ?: currentConfig.providerType,
            locale = locale ?: currentConfig.locale
        )

        UserConfig.save(newConfig)
        // 설정 저장 후 로케일을 다시 설정
        if (locale != null) {
            val newLocale = Locale.fromCode(locale!!)
            MessageBundleProvider.setLocale(newLocale)
        }
        echo(MessageBundleProvider.getMessage("message.config.saved"))
    }
}

// AIProvider 옵션 그룹
class AIOptions : OptionGroup() {
    private val config = UserConfig.load()
    
    val apiKey by option("-k", "--key", help = MessageBundleProvider.getMessage("option.api-key.description"))
        .default(config.apiKey ?: "")
    
    val providerType by option("-p", "--provider", help = MessageBundleProvider.getMessage("option.provider.description"))
        .choice("openai", "claude", "perplexity", "deepseek", "gemini", ignoreCase = true)
        .default(config.providerType ?: "openai")
    
    val locale by option("--locale", help = MessageBundleProvider.getMessage("option.locale.description"))
        .choice("en", "ko", ignoreCase = true)
        .default(config.locale ?: Locale.ENGLISH.code)
}

// Git 옵션 그룹
class GitOptions : OptionGroup() {
    val path by option("--path", help = MessageBundleProvider.getMessage("option.repo.description"))
        .path(mustExist = true, canBeFile = false)
        .default(java.nio.file.Paths.get("."))
        
    val branch by option("-b", "--branch", help = MessageBundleProvider.getMessage("option.branch.description"))
        
    val days by option("-d", "--days", help = MessageBundleProvider.getMessage("option.days.description"))
        .int()
        .default(7)
        
    val limit by option("-l", "--limit", help = MessageBundleProvider.getMessage("option.limit.description"))
        .int()
        .default(50)
        
    val fromCommit by option("--from", help = MessageBundleProvider.getMessage("option.from-commit.description"))
    
    val toCommit by option("--to", help = MessageBundleProvider.getMessage("option.to-commit.description"))
        .default("HEAD")
}

// 공통 기능을 위한 추상 클래스
abstract class BaseCommand(
    name: String,
    help: String,
    printHelpOnEmptyArgs: Boolean = true
) : CliktCommand(
    name = name,
    help = help,
    printHelpOnEmptyArgs = printHelpOnEmptyArgs
) {
    protected val gitOptions by GitOptions()
    protected val aiOptions by AIOptions()
    
    protected fun createAISummarizer(): Pair<AIProviderConfig, AIProviderType> {
        val config = when (aiOptions.providerType.lowercase()) {
            "openai" -> OpenAIConfig(
                apiKey = aiOptions.apiKey,
                locale = Locale.fromCode(aiOptions.locale)
            )
            "claude" -> ClaudeConfig(
                apiKey = aiOptions.apiKey,
                locale = Locale.fromCode(aiOptions.locale)
            )
            "deepseek" -> DeepSeekConfig(
                apiKey = aiOptions.apiKey,
                locale = Locale.fromCode(aiOptions.locale)
            )
            "gemini" -> GeminiConfig(
                apiKey = aiOptions.apiKey,
                locale = Locale.fromCode(aiOptions.locale)
            )
            "perplexity" -> PerplexityConfig(
                apiKey = aiOptions.apiKey,
                locale = Locale.fromCode(aiOptions.locale)
            )
            else -> throw IllegalArgumentException("지원하지 않는 AI 프로바이더입니다: ${aiOptions.providerType}")
        }
        
        val provider = when (aiOptions.providerType.lowercase()) {
            "claude" -> AIProviderType.CLAUDE
            "perplexity" -> AIProviderType.PERPLEXITY
            "deepseek" -> AIProviderType.DEEPSEEK
            "gemini" -> AIProviderType.GEMINI
            else -> AIProviderType.OPENAI
        }
        
        return config to provider
    }
    
    protected suspend fun getCommits(gitAnalyzer: GitAnalyzer): List<Commit> {
        return if (gitOptions.fromCommit != null) {
            gitAnalyzer.getCommitRange(gitOptions.fromCommit!!, gitOptions.toCommit)
        } else {
            gitAnalyzer.getCommits(gitOptions.days).take(gitOptions.limit)
        }
    }
    

}

// 메인 커맨드
class CommitChronicle : CliktCommand(
    help = MessageBundleProvider.getMessage("cli.description"),
    printHelpOnEmptyArgs = true,
    invokeWithoutSubcommand = true
) {
    override fun run() {
        if (currentContext.invokedSubcommand == null) {
            echo("""
                ${MessageBundleProvider.getMessage("cli.description")}
                
                ${MessageBundleProvider.getMessage("cli.help.main")}
                
                ${MessageBundleProvider.getMessage("cli.help.summarize")}
                ${MessageBundleProvider.getMessage("cli.help.summarize.desc")}
                ${MessageBundleProvider.getMessage("cli.help.summarize.example")}
                
                ${MessageBundleProvider.getMessage("cli.help.pr")}
                ${MessageBundleProvider.getMessage("cli.help.pr.desc")}
                ${MessageBundleProvider.getMessage("cli.help.pr.example")}
                
                ${MessageBundleProvider.getMessage("cli.help.changelog")}
                ${MessageBundleProvider.getMessage("cli.help.changelog.desc")}
                ${MessageBundleProvider.getMessage("cli.help.changelog.example")}
                
                ${MessageBundleProvider.getMessage("cli.help.config")}
                ${MessageBundleProvider.getMessage("cli.help.config.desc")}
                ${MessageBundleProvider.getMessage("cli.help.config.example")}
                
                ${MessageBundleProvider.getMessage("cli.help.more")}
                ${MessageBundleProvider.getMessage("cli.help.example")}
            """.trimIndent())
        }
    }
}

// 요약 커맨드
class Summarize : BaseCommand(
    name = "summarize",
    help = MessageBundleProvider.getMessage("command.summarize.description")
) {
    
    override fun run() = runBlocking {
        // locale이 설정되어 있으면 먼저 적용
        MessageBundleProvider.setLocale(Locale.fromCode(aiOptions.locale))

        try {
            val gitAnalyzer = GitAnalyzerFactory.create(gitOptions.path.absolutePathString())
            val (config, provider) = createAISummarizer()
            val aiSummarizer = AISummarizerFactory.create(config, provider)
            
            val commits = getCommits(gitAnalyzer)
            if (commits.isEmpty()) {
                echo(MessageBundleProvider.getMessage("error.no-commits"))
                return@runBlocking
            }
            
            echo("${commits.size}${MessageBundleProvider.getMessage("message.analyzing-commits")}")
            val summary = aiSummarizer.summarize(commits)
            
            echo("\n${MessageBundleProvider.getMessage("message.output.result")}\n")
            echo(summary)
        } catch (e: Exception) {
            echo("${MessageBundleProvider.getMessage("error.occurred")}: ${e.message}", err = true)
            e.printStackTrace()
        }
    }
}

// PR 초안 생성 커맨드
class GeneratePR : BaseCommand(
    name = "pr",
    help = MessageBundleProvider.getMessage("command.generate-pr.description")
) {
    
    override fun run() = runBlocking {
        // locale이 설정되어 있으면 먼저 적용
        MessageBundleProvider.setLocale(Locale.fromCode(aiOptions.locale))

        try {
            val gitAnalyzer = GitAnalyzerFactory.create(gitOptions.path.absolutePathString())
            val (config, provider) = createAISummarizer()
            val aiSummarizer = AISummarizerFactory.create(config, provider)
            
            val commits = getCommits(gitAnalyzer)
            if (commits.isEmpty()) {
                echo(MessageBundleProvider.getMessage("error.no-commits"))
                return@runBlocking
            }
            
            echo("${commits.size}${MessageBundleProvider.getMessage("message.analyzing-commits")}")
            
            // GitHub 템플릿 자동 감지 시도
            val templateDetector = TemplateEngineFactory.createGitHubTemplateDetector()
            val githubTemplatePath = templateDetector.findPRTemplate(gitOptions.path.absolutePathString(), Locale.fromCode(aiOptions.locale))
            
            val prDraft = if (githubTemplatePath != null) {
                echo("GitHub PR 템플릿을 감지했습니다: $githubTemplatePath")
                val templateParser = TemplateEngineFactory.createGitHubTemplateParser(Locale.fromCode(aiOptions.locale))
                val templateContent = templateDetector.readTemplateContent(githubTemplatePath)
                
                if (templateContent != null) {
                    templateParser.parseAndRender(templateContent, commits, null)
                } else {
                    echo("템플릿 파일을 읽을 수 없습니다. AI 생성 모드로 전환합니다.")
                    aiSummarizer.generatePRDraft(commits, null)
                }
            } else {
                // GitHub 템플릿이 없으면 AI 생성 사용
                aiSummarizer.generatePRDraft(commits, null)
            }
            
            // 콘솔에 결과 출력
            echo("\n${MessageBundleProvider.getMessage("message.output.result")}\n")
            echo(prDraft)
        } catch (e: Exception) {
            echo("${MessageBundleProvider.getMessage("error.occurred")}: ${e.message}", err = true)
            e.printStackTrace()
        }
    }
}

// 변경 로그 생성 커맨드
class GenerateChangelog : BaseCommand(
    name = "changelog",
    help = MessageBundleProvider.getMessage("command.generate-changelog.description")
) {
    private val groupByType by option("--group", help = MessageBundleProvider.getMessage("option.group.description")).flag()
    
    override fun run() = runBlocking {
        // locale이 설정되어 있으면 먼저 적용
        MessageBundleProvider.setLocale(Locale.fromCode(aiOptions.locale))

        try {
            val gitAnalyzer = GitAnalyzerFactory.create(gitOptions.path.absolutePathString())
            val (config, provider) = createAISummarizer()
            val aiSummarizer = AISummarizerFactory.create(config, provider)
            
            val commits = getCommits(gitAnalyzer)
            if (commits.isEmpty()) {
                echo(MessageBundleProvider.getMessage("error.no-commits"))
                return@runBlocking
            }

            echo("${commits.size}${MessageBundleProvider.getMessage("message.analyzing-commits")}")
            val changelog = aiSummarizer.generateChangelog(commits, groupByType)

            echo("\n${MessageBundleProvider.getMessage("message.output.result")}\n")
            echo(changelog)
        } catch (e: Exception) {
            echo("${MessageBundleProvider.getMessage("error.occurred")}: ${e.message}", err = true)
            e.printStackTrace()
        }
    }
}

// 메인 함수
fun main(args: Array<String>) {
    try {
        // 설정 파일에서 로케일을 먼저 로드
        val config = UserConfig.load()
        val locale = config.getLocale() ?: Locale.ENGLISH
        MessageBundleProvider.setLocale(locale)
        
        val command = CommitChronicle()
            .subcommands(Summarize(), GeneratePR(), GenerateChangelog(), Config())
        command.main(args)
    } catch (e: Exception) {
        System.err.println("오류 발생: ${e.message}")
        e.printStackTrace()
    }
} 
