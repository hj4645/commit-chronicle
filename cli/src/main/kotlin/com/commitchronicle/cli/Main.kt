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
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.path
import kotlinx.coroutines.runBlocking
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

// Interactive setup for first-time users
class InteractiveSetup {
    fun run(): UserConfig {
        println("Welcome to Commit Chronicle!")
        println("Let's set up your configuration.\n")
        
        // Language selection with arrow keys
        val languageOptions = listOf(
            "en" to "English",
            "ko" to "한국어 (Korean)",
            "zh" to "中文 (Chinese)",
            "ja" to "日本語 (Japanese)"
        )
        
        val locale = try {
            InteractiveMenu.showMenu("Select your language:", languageOptions)
        } catch (e: InterruptedException) {
            println("Setup cancelled.")
            return UserConfig()
        }
        
        // Set locale immediately after selection
        MessageBundleProvider.setLocale(Locale.fromCode(locale))
        
        // AI Provider selection with arrow keys
        val providerOptions = listOf(
            "openai" to "OpenAI",
            "claude" to "Claude",
            "perplexity" to "Perplexity",
            "deepseek" to "DeepSeek",
            "gemini" to "Gemini"
        )
        
        val provider = try {
            InteractiveMenu.showMenu("\n${MessageBundleProvider.getMessage("cli.help.provider.select")}", providerOptions)
        } catch (e: InterruptedException) {
            println("Setup cancelled.")
            return UserConfig()
        }
        
        // API Key input
        print("\n${MessageBundleProvider.getMessage("cli.help.apikey.enter")} $provider: ")
        val apiKey = readLine() ?: ""
        
        if (apiKey.isEmpty()) {
            println("${MessageBundleProvider.getMessage("cli.help.apikey.warning")}")
        }
        
        val config = UserConfig(
            apiKey = apiKey.takeIf { it.isNotEmpty() },
            providerType = provider,
            locale = locale
        )
        
        UserConfig.save(config)
        println("\n${MessageBundleProvider.getMessage("message.config.saved")}")
        println("${MessageBundleProvider.getMessage("cli.help.usage.commands")}")
        println("${MessageBundleProvider.getMessage("cli.help.usage.settings")}\n")
        
        return config
    }
}

// Settings command for configuration management
class Settings : CliktCommand(
    name = "settings",
    help = "Manage your configuration settings"
) {
    private val show by option("--show", help = MessageBundleProvider.getMessage("option.show.description")).flag()
    private val reset by option("--reset", help = MessageBundleProvider.getMessage("option.reset.description")).flag()
    
    override fun run() {
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
        
        // Interactive settings update
        val currentConfig = UserConfig.load()
        
        println("${MessageBundleProvider.getMessage("cli.help.settings.update")}")
        
        // Language selection with arrow keys
        val languageOptions = listOf(
            "en" to "English",
            "ko" to "한국어 (Korean)",
            "zh" to "中文 (Chinese)",
            "ja" to "日本語 (Japanese)"
        )
        
        val currentLocaleIndex = languageOptions.indexOfFirst { it.first == (currentConfig.locale ?: "en") }
        val reorderedLanguageOptions = if (currentLocaleIndex >= 0) {
            listOf(languageOptions[currentLocaleIndex]) + languageOptions.filterIndexed { index, _ -> index != currentLocaleIndex }
        } else {
            languageOptions
        }
        
        val newLocale = try {
            InteractiveMenu.showMenu("\n${MessageBundleProvider.getMessage("option.locale.description")} (${MessageBundleProvider.getMessage("cli.help.current")}: ${currentConfig.locale ?: "en"}):", reorderedLanguageOptions)
        } catch (e: InterruptedException) {
            currentConfig.locale ?: "en"
        }
        
        // Update locale for next messages
        MessageBundleProvider.setLocale(Locale.fromCode(newLocale))
        
        // Provider selection with arrow keys
        val providerOptions = listOf(
            "openai" to "OpenAI",
            "claude" to "Claude",
            "perplexity" to "Perplexity",
            "deepseek" to "DeepSeek",
            "gemini" to "Gemini"
        )
        
        val currentProviderIndex = providerOptions.indexOfFirst { it.first == (currentConfig.providerType ?: "openai") }
        val reorderedProviderOptions = if (currentProviderIndex >= 0) {
            listOf(providerOptions[currentProviderIndex]) + providerOptions.filterIndexed { index, _ -> index != currentProviderIndex }
        } else {
            providerOptions
        }
        
        val newProvider = try {
            InteractiveMenu.showMenu("\n${MessageBundleProvider.getMessage("option.provider.description")} (${MessageBundleProvider.getMessage("cli.help.current")}: ${currentConfig.providerType ?: "openai"}):", reorderedProviderOptions)
        } catch (e: InterruptedException) {
            currentConfig.providerType ?: "openai"
        }
        
        // API Key input
        print("\n${MessageBundleProvider.getMessage("option.api-key.description")} (${MessageBundleProvider.getMessage("cli.help.current")}: ${currentConfig.apiKey?.let { "***" } ?: MessageBundleProvider.getMessage("message.config.not-set")}): ")
        val newApiKey = readLine()?.takeIf { it.isNotEmpty() } ?: currentConfig.apiKey
        
        val newConfig = UserConfig(
            apiKey = newApiKey,
            providerType = newProvider,
            locale = newLocale
        )
        
        UserConfig.save(newConfig)
        // Update locale after saving
        MessageBundleProvider.setLocale(Locale.fromCode(newLocale))
        echo(MessageBundleProvider.getMessage("message.config.saved"))
    }
}

// Summarize command - simplified
class Summarize : CliktCommand(
    name = "summarize",
    help = "Analyze and summarize Git commits"
) {
    private val days by option("-d", "--days", help = MessageBundleProvider.getMessage("option.days.description")).int().default(7)
    private val limit by option("-l", "--limit", help = MessageBundleProvider.getMessage("option.limit.description")).int().default(50)
    private val branch by option("-b", "--branch", help = MessageBundleProvider.getMessage("option.branch.description"))
    private val path by option("--path", help = MessageBundleProvider.getMessage("option.repo.description")).path(mustExist = true, canBeFile = false).default(java.nio.file.Paths.get("."))
    
    override fun run() = runBlocking {
        val config = UserConfig.load()
        
        if (config.apiKey.isNullOrEmpty()) {
            echo("${MessageBundleProvider.getMessage("error.api-key-required")}")
            return@runBlocking
        }
        
        try {
            MessageBundleProvider.setLocale(Locale.fromCode(config.locale ?: "en"))
            
            val gitAnalyzer = GitAnalyzerFactory.create(path.absolutePathString())
            val aiConfig = createAIConfig(config)
            val aiSummarizer = AISummarizerFactory.create(aiConfig.first, aiConfig.second)
            
            val commits = gitAnalyzer.getCommits(days).take(limit)
            if (commits.isEmpty()) {
                echo(MessageBundleProvider.getMessage("error.no-commits"))
                return@runBlocking
            }
            
            echo("${commits.size}${MessageBundleProvider.getMessage("message.analyzing-commits")}")
            val summary = aiSummarizer.summarize(commits)
            
            echo("\n=== ${MessageBundleProvider.getMessage("message.output.result").uppercase()} ===\n")
            echo(summary)
        } catch (e: Exception) {
            echo("${MessageBundleProvider.getMessage("error.occurred")}: ${e.message}", err = true)
        }
    }
    
    private fun createAIConfig(config: UserConfig): Pair<AIProviderConfig, AIProviderType> {
        val locale = Locale.fromCode(config.locale ?: "en")
        val aiConfig = when (config.providerType?.lowercase()) {
            "claude" -> ClaudeConfig(apiKey = config.apiKey!!, locale = locale)
            "perplexity" -> PerplexityConfig(apiKey = config.apiKey!!, locale = locale)
            "deepseek" -> DeepSeekConfig(apiKey = config.apiKey!!, locale = locale)
            "gemini" -> GeminiConfig(apiKey = config.apiKey!!, locale = locale)
            else -> OpenAIConfig(apiKey = config.apiKey!!, locale = locale)
        }
        
        val provider = when (config.providerType?.lowercase()) {
            "claude" -> AIProviderType.CLAUDE
            "perplexity" -> AIProviderType.PERPLEXITY
            "deepseek" -> AIProviderType.DEEPSEEK
            "gemini" -> AIProviderType.GEMINI
            else -> AIProviderType.OPENAI
        }
        
        return aiConfig to provider
    }
}

// PR command - simplified
class PR : CliktCommand(
    name = "pr",
    help = "Generate PR draft using AI"
) {
    private val days by option("-d", "--days", help = MessageBundleProvider.getMessage("option.days.description")).int().default(7)
    private val limit by option("-l", "--limit", help = MessageBundleProvider.getMessage("option.limit.description")).int().default(50)
    private val branch by option("-b", "--branch", help = MessageBundleProvider.getMessage("option.branch.description"))
    private val path by option("--path", help = MessageBundleProvider.getMessage("option.repo.description")).path(mustExist = true, canBeFile = false).default(java.nio.file.Paths.get("."))
    
    override fun run() = runBlocking {
        val config = UserConfig.load()
        
        if (config.apiKey.isNullOrEmpty()) {
            echo("${MessageBundleProvider.getMessage("error.api-key-required")}")
            return@runBlocking
        }
        
        try {
            MessageBundleProvider.setLocale(Locale.fromCode(config.locale ?: "en"))
            
            val gitAnalyzer = GitAnalyzerFactory.create(path.absolutePathString())
            val aiConfig = createAIConfig(config)
            val aiSummarizer = AISummarizerFactory.create(aiConfig.first, aiConfig.second)
            
            val commits = gitAnalyzer.getCommits(days).take(limit)
            if (commits.isEmpty()) {
                echo(MessageBundleProvider.getMessage("error.no-commits"))
                return@runBlocking
            }
            
            echo("${commits.size}${MessageBundleProvider.getMessage("message.analyzing-commits")}")
            
            // Try to detect GitHub PR template
            val templateDetector = TemplateEngineFactory.createGitHubTemplateDetector()
            val githubTemplatePath = templateDetector.findPRTemplate(path.absolutePathString(), Locale.fromCode(config.locale ?: "en"))
            
            val prDraft = if (githubTemplatePath != null) {
                echo("${MessageBundleProvider.getMessage("cli.help.template.detected")}: $githubTemplatePath")
                val templateParser = TemplateEngineFactory.createGitHubTemplateParser(Locale.fromCode(config.locale ?: "en"))
                val templateContent = templateDetector.readTemplateContent(githubTemplatePath)
                
                if (templateContent != null) {
                    templateParser.parseAndRender(templateContent, commits, null)
                } else {
                    echo(MessageBundleProvider.getMessage("cli.help.template.fallback"))
                    aiSummarizer.generatePRDraft(commits, null)
                }
            } else {
                aiSummarizer.generatePRDraft(commits, null)
            }
            
            echo("\n=== ${MessageBundleProvider.getMessage("command.generate-pr.description").uppercase()} ===\n")
            echo(prDraft)
        } catch (e: Exception) {
            echo("${MessageBundleProvider.getMessage("error.occurred")}: ${e.message}", err = true)
        }
    }
    
    private fun createAIConfig(config: UserConfig): Pair<AIProviderConfig, AIProviderType> {
        val locale = Locale.fromCode(config.locale ?: "en")
        val aiConfig = when (config.providerType?.lowercase()) {
            "claude" -> ClaudeConfig(apiKey = config.apiKey!!, locale = locale)
            "perplexity" -> PerplexityConfig(apiKey = config.apiKey!!, locale = locale)
            "deepseek" -> DeepSeekConfig(apiKey = config.apiKey!!, locale = locale)
            "gemini" -> GeminiConfig(apiKey = config.apiKey!!, locale = locale)
            else -> OpenAIConfig(apiKey = config.apiKey!!, locale = locale)
        }
        
        val provider = when (config.providerType?.lowercase()) {
            "claude" -> AIProviderType.CLAUDE
            "perplexity" -> AIProviderType.PERPLEXITY
            "deepseek" -> AIProviderType.DEEPSEEK
            "gemini" -> AIProviderType.GEMINI
            else -> AIProviderType.OPENAI
        }
        
        return aiConfig to provider
    }
}

// Main command
class CommitChronicle : CliktCommand(
    help = "A tool for analyzing and summarizing Git commits using AI",
    printHelpOnEmptyArgs = true,
    invokeWithoutSubcommand = true
) {
    override fun run() {
        if (currentContext.invokedSubcommand == null) {
            val config = UserConfig.load()
            
            // Check if first-time setup is needed
            if (config.apiKey.isNullOrEmpty() || config.providerType.isNullOrEmpty()) {
                echo("First-time setup detected.")
                InteractiveSetup().run()
                return
            }
            
            // Set locale for help messages
            MessageBundleProvider.setLocale(Locale.fromCode(config.locale ?: "en"))
            
            echo("""
                ${MessageBundleProvider.getMessage("cli.description")}
                
                ${MessageBundleProvider.getMessage("cli.help.main")}
                  summarize    ${MessageBundleProvider.getMessage("command.summarize.description")}
                  pr          ${MessageBundleProvider.getMessage("command.generate-pr.description")}
                  settings    ${MessageBundleProvider.getMessage("command.config.description")}
                
                ${MessageBundleProvider.getMessage("cli.help.example")}
                  commitchronicle summarize
                  commitchronicle pr
                  commitchronicle settings --show
                
                ${MessageBundleProvider.getMessage("cli.help.more")}
            """.trimIndent())
        }
    }
}

// Main function
fun main(args: Array<String>) {
    try {
        val config = UserConfig.load()
        val locale = config.getLocale() ?: Locale.ENGLISH
        MessageBundleProvider.setLocale(locale)
        
        val command = CommitChronicle()
            .subcommands(Summarize(), PR(), Settings())
        command.main(args)
    } catch (e: Exception) {
        System.err.println("Error: ${e.message}")
        e.printStackTrace()
    }
} 
