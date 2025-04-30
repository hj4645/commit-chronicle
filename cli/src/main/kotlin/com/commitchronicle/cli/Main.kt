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
    private val model by option("-m", "--model", help = MessageBundleProvider.getMessage("option.model.description"))
    private val locale by option("--locale", help = MessageBundleProvider.getMessage("option.locale.description"))
        .choice(*Locale.values().map { it.code to "${it.code} (${it.languageName})" }.toTypedArray(), ignoreCase = true)
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
                - ${MessageBundleProvider.getMessage("option.model.description")}: ${config.modelName ?: MessageBundleProvider.getMessage("message.config.not-set")}
                - ${MessageBundleProvider.getMessage("option.locale.description")}: ${config.locale ?: MessageBundleProvider.getMessage("message.config.not-set")}
            """.trimIndent())
            return
        }

        val currentConfig = UserConfig.load()
        val newConfig = currentConfig.copy(
            apiKey = apiKey ?: currentConfig.apiKey,
            providerType = providerType ?: currentConfig.providerType,
            modelName = model ?: currentConfig.modelName,
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
        
    val model by option("-m", "--model", help = MessageBundleProvider.getMessage("option.model.description"))
        .default(config.modelName ?: "")
    
    val locale by option("--locale", help = MessageBundleProvider.getMessage("option.locale.description"))
        .choice(*Locale.values().map { it.code to "${it.code} (${it.languageName})" }.toTypedArray(), ignoreCase = true)
        .default(config.locale ?: Locale.ENGLISH.code)
}

// Git 옵션 그룹
class GitOptions : OptionGroup() {
    val path by option("--path", help = MessageBundleProvider.getMessage("option.repo.description"))
        .path(mustExist = true, canBeFile = false)
        .required()
        
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
                modelName = aiOptions.model,
                locale = Locale.fromCode(aiOptions.locale)
            )
            "claude" -> ClaudeConfig(
                apiKey = aiOptions.apiKey,
                modelName = aiOptions.model,
                locale = Locale.fromCode(aiOptions.locale)
            )
            "deepseek" -> DeepSeekConfig(
                apiKey = aiOptions.apiKey,
                modelName = aiOptions.model,
                locale = Locale.fromCode(aiOptions.locale)
            )
            "gemini" -> GeminiConfig(
                apiKey = aiOptions.apiKey,
                modelName = aiOptions.model,
                locale = Locale.fromCode(aiOptions.locale)
            )
            "perplexity" -> PerplexityConfig(
                apiKey = aiOptions.apiKey,
                modelName = aiOptions.model,
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
    
    protected fun writeOutput(content: String, outputFile: String?) {
        if (outputFile != null) {
            File(outputFile).writeText(content)
            echo("${MessageBundleProvider.getMessage("message.output.saved")}: $outputFile")
        } else {
            echo("\n${MessageBundleProvider.getMessage("message.output.result")}\n")
            echo(content)
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
    private val outputFile by option("-o", "--output", help = MessageBundleProvider.getMessage("option.output.description"))
    
    override fun run() = runBlocking {
        // locale이 설정되어 있으면 먼저 적용
        if (aiOptions.locale != null) {
            MessageBundleProvider.setLocale(Locale.fromCode(aiOptions.locale))
        }

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
            writeOutput(summary, outputFile)
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
    private val title by option("-t", "--title", help = MessageBundleProvider.getMessage("option.title.description"))
    private val outputFile by option("-o", "--output", help = MessageBundleProvider.getMessage("option.output.description"))
    private val templatePath by option("--template", help = MessageBundleProvider.getMessage("option.template.description")).path(mustExist = true, canBeFile = true)
    
    override fun run() = runBlocking {
        // locale이 설정되어 있으면 먼저 적용
        if (aiOptions.locale != null) {
            MessageBundleProvider.setLocale(Locale.fromCode(aiOptions.locale))
        }

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
            val prDraft = if (templatePath != null) {
                val templateEngine = TemplateEngineFactory.create()
                val summary = aiSummarizer.summarize(commits)
                
                val data = mapOf(
                    "commits" to commits,
                    "summary" to summary,
                    "title" to (title ?: MessageBundleProvider.getMessage("message.default-pr-title"))
                )
                
                templateEngine.render(templatePath!!.toString(), data)
            } else {
                aiSummarizer.generatePRDraft(commits, title)
            }
            
            writeOutput(prDraft, outputFile)
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
    private val outputFile by option("-o", "--output", help = MessageBundleProvider.getMessage("option.output.description"))
    private val templatePath by option("--template", help = MessageBundleProvider.getMessage("option.template.description")).path(mustExist = true, canBeFile = true)
    
    override fun run() = runBlocking {
        // locale이 설정되어 있으면 먼저 적용
        if (aiOptions.locale != null) {
            MessageBundleProvider.setLocale(Locale.fromCode(aiOptions.locale))
        }

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
            val changelog = if (templatePath != null) {
                val templateEngine = TemplateEngineFactory.create()
                val summary = aiSummarizer.summarize(commits)

                val data = mapOf(
                    "commits" to commits,
                    "summary" to summary,
                    "groupByType" to groupByType
                )

                templateEngine.render(templatePath!!.toString(), data)
            } else {
                aiSummarizer.generateChangelog(commits, groupByType)
            }

            writeOutput(changelog, outputFile)
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
