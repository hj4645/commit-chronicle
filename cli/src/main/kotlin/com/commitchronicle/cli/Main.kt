package com.commitchronicle.cli

import com.commitchronicle.ai.AIProviderConfig
import com.commitchronicle.ai.AIProviderType
import com.commitchronicle.ai.factory.AISummarizerFactory
import com.commitchronicle.ai.providers.claude.config.ClaudeConfig
import com.commitchronicle.ai.providers.deepseek.config.DeepSeekConfig
import com.commitchronicle.ai.providers.gemini.config.GeminiConfig
import com.commitchronicle.ai.providers.openai.config.OpenAIConfig
import com.commitchronicle.ai.providers.perplexity.config.PerplexityConfig
import com.commitchronicle.git.GitAnalyzerFactory
import com.commitchronicle.template.TemplateEngineFactory
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.*
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

class InteractiveSetup {
    fun run(): UserConfig {
        println("Welcome to Commit Chronicle!")
        println("Let's set up your configuration.\n")

        val languageOptions = SupportedLanguage.getLanguageOptions() + listOf("exit" to "Exit Setup")
        
        val locale = try {
            InteractiveMenu.showMenu("Select your language:", languageOptions)
        } catch (e: InterruptedException) {
            println("Setup cancelled.")
            return UserConfig()
        }
        
        if (locale == "exit") {
            println("Setup cancelled.")
            return UserConfig()
        }

        // Set locale immediately after selection (for initial setup only)
        // 시스템 메시지는 영어 고정

        val providerOptions = AIProvider.getProviderOptions() + listOf("back" to "← Back", "exit" to "Exit Setup")
        
        val provider = try {
            InteractiveMenu.showMenu("\nSelect AI provider:", providerOptions)
        } catch (e: InterruptedException) {
            println("Setup cancelled.")
            return UserConfig()
        }

        when (provider) {
            "back" -> {
                println("Going back to language selection...")
                return run()
            }
            "exit" -> {
                println("Setup cancelled.")
                return UserConfig()
            }
        }

        // Model selection
        val availableModels = Settings.getAvailableModels(provider) + listOf("back" to "← Back", "exit" to "Exit Setup")
        val selectedModel = try {
            InteractiveMenu.showMenu("\nSelect model for $provider:", availableModels)
        } catch (e: InterruptedException) {
            Settings.getDefaultModel(provider)
        }

        when (selectedModel) {
            "back" -> {
                println("Going back to provider selection...")
                return run()
            }
            "exit" -> {
                println("Setup cancelled.")
                return UserConfig()
            }
        }
        
        // API Key input
        print("\nEnter API key for $provider (press Enter to skip, type 'back' to go back): ")
        val apiKey = readLine() ?: ""
        
        when {
            apiKey.lowercase() == "back" -> {
                println("Going back to model selection...")
                return run() // Restart setup
            }
            apiKey.isEmpty() -> {
                println("Warning: No API key provided. You can set it later using 'settings' command.")
            }
        }
        
        val config = UserConfig(
            apiKey = apiKey.takeIf { it.isNotEmpty() },
            providerType = provider,
            modelName = selectedModel,
            locale = locale
        )
        
        UserConfig.save(config)
        println("\nConfiguration saved successfully!")
        println("You can now use 'summarize' or 'pr' commands.")
        println("Use 'settings' command to change configuration later.\n")
        
        return config
    }
}

class Settings : CliktCommand(
    name = "settings",
    help = "Manage your configuration settings"
) {
    private val show by option("--show", help = MessageBundleProvider.getMessage("option.show.description")).flag()
    private val reset by option("--reset", help = MessageBundleProvider.getMessage("option.reset.description")).flag()

    companion object {
        fun getAvailableModels(provider: String): List<Pair<String, String>> {
            return AIProvider.fromKey(provider)?.models?.map { it.toPair() }
                ?: listOf("default" to "Default Model")
        }

        fun getDefaultModel(provider: String): String {
            return AIProvider.fromKey(provider)?.defaultModel?.id ?: "default"
        }
    }

    override fun run() {
        if (reset) {
            UserConfig.save(UserConfig())
            echo("Configuration has been reset to defaults")
            return
        }

        if (show) {
            val config = UserConfig.load()
            echo("""
                Current configuration:
                - API Key: ${config.apiKey?.let { "***" } ?: "not set"}
                - AI Provider: ${config.providerType ?: "not set"}
                - Model: ${config.modelName ?: "default"}
                - Language: ${config.locale ?: "not set"}
                - Default Days: ${config.defaultDays}
                - Default Limit: ${config.defaultLimit}
            """.trimIndent())
            return
        }

        val currentConfig = UserConfig.load()
        
        println("Update current settings")

        val settingsOptions = listOf(
            "language" to "Language",
            "provider" to "AI Provider",
            "analysis" to "Analysis Settings",
            "done" to "Done"
        )
        
        var updatedConfig = currentConfig

        while (true) {
            val choice = try {
                InteractiveMenu.showMenu("\nSelect setting to update:", settingsOptions)
            } catch (e: InterruptedException) {
                break
            }

            when (choice) {
                "language" -> {
                    val languageOptions = SupportedLanguage.getLanguageOptions()

                    val currentLocaleIndex = languageOptions.indexOfFirst { it.first == (updatedConfig.locale ?: "en") }
                    val reorderedLanguageOptions = if (currentLocaleIndex >= 0) {
                        listOf(languageOptions[currentLocaleIndex]) + languageOptions.filterIndexed { index, _ -> index != currentLocaleIndex }
                    } else {
                        languageOptions
                    }

                    val languageOptionsWithBack = reorderedLanguageOptions + listOf("back" to "← Back")

                    val newLocale = try {
                        InteractiveMenu.showMenu("\nSelect language (current: ${updatedConfig.locale ?: "en"}):", languageOptionsWithBack)
                    } catch (e: InterruptedException) {
                        continue
                    }

                    if (newLocale == "back") {
                        continue
                    }

                    updatedConfig = updatedConfig.copy(locale = newLocale)
                    println("Language updated to: $newLocale (AI responses will use this language)")
                }

                "provider" -> {
                    val providerOptions = AIProvider.values().map { provider ->
                        val hasApiKey = currentConfig.providerType == provider.key && !currentConfig.apiKey.isNullOrEmpty()
                        provider.key to "${provider.displayName}${if (hasApiKey) " (API Key ✓)" else ""}"
                    }

                    val currentProviderIndex = providerOptions.indexOfFirst { it.first == (updatedConfig.providerType ?: "openai") }
                    val reorderedProviderOptions = if (currentProviderIndex >= 0) {
                        listOf(providerOptions[currentProviderIndex]) + providerOptions.filterIndexed { index, _ -> index != currentProviderIndex }
        } else {
                        providerOptions
                    }

                    val providerOptionsWithBack = reorderedProviderOptions + listOf("back" to "← Back")

                    val newProvider = try {
                        InteractiveMenu.showMenu("\nSelect AI provider (current: ${updatedConfig.providerType ?: "openai"}):", providerOptionsWithBack)
                    } catch (e: InterruptedException) {
                        continue
                    }

                    if (newProvider == "back") {
                        continue
                    }

                    val availableModels = getAvailableModels(newProvider)
                    val currentModel = if (newProvider == updatedConfig.providerType) updatedConfig.modelName else null
                    val currentModelIndex = availableModels.indexOfFirst { it.first == currentModel }
                    val reorderedModelOptions = if (currentModelIndex >= 0) {
                        listOf(availableModels[currentModelIndex]) + availableModels.filterIndexed { index, _ -> index != currentModelIndex }
        } else {
                        availableModels
                    }

                    val modelOptionsWithBack = reorderedModelOptions + listOf("back" to "← Back")

                    val newModel = try {
                        InteractiveMenu.showMenu("\nSelect model for $newProvider (current: ${currentModel ?: getDefaultModel(newProvider)}):", modelOptionsWithBack)
                    } catch (e: InterruptedException) {
                        continue
                    }

                    if (newModel == "back") {
                        continue
                    }

                    // Handle API key
                    val hasExistingKey = newProvider == currentConfig.providerType && !currentConfig.apiKey.isNullOrEmpty()
                    val newApiKey = if (hasExistingKey) {
                        val keyOptions = listOf(
                            "keep" to "Keep existing API key",
                            "change" to "Change API key",
                            "back" to "← Back"
                        )

                        val keyChoice = try {
                            InteractiveMenu.showMenu("\nAPI key for $newProvider (current: ***):", keyOptions)
                        } catch (e: InterruptedException) {
                            continue
                        }

                        when (keyChoice) {
                            "back" -> continue
                            "change" -> {
                                print("\nEnter new API key for $newProvider (press Enter to cancel): ")
                                val input = readLine()
                                if (input.isNullOrEmpty()) {
                                    println("API key change cancelled")
                                    continue
                                }
                                input
                            }
                            else -> currentConfig.apiKey
                        }
                    } else {
                        print("\nEnter API key for $newProvider (press Enter to skip): ")
                        val input = readLine()
                        if (input.isNullOrEmpty()) {
                            println("API key setup skipped")
                            continue
                        }
                        input
                    }

                    updatedConfig = updatedConfig.copy(providerType = newProvider, modelName = newModel, apiKey = newApiKey)
                    println("Provider updated to: $newProvider")
                    println("Model updated to: $newModel")
                }

                "analysis" -> {
                    print("\nEnter default days for analysis (current: ${updatedConfig.defaultDays}, press Enter to keep current): ")
                    val daysInput = readLine()
                    val newDays = if (daysInput.isNullOrEmpty()) {
                        updatedConfig.defaultDays
        } else {
                        daysInput.toIntOrNull()?.takeIf { it > 0 } ?: run {
                            println("Invalid input. Keeping current value: ${updatedConfig.defaultDays}")
                            updatedConfig.defaultDays
                        }
                    }

                    // Limit setting
                    print("\nEnter default commit limit (current: ${updatedConfig.defaultLimit}, press Enter to keep current): ")
                    val limitInput = readLine()
                    val newLimit = if (limitInput.isNullOrEmpty()) {
                        updatedConfig.defaultLimit
        } else {
                        limitInput.toIntOrNull()?.takeIf { it > 0 } ?: run {
                            println("Invalid input. Keeping current value: ${updatedConfig.defaultLimit}")
                            updatedConfig.defaultLimit
                        }
                    }

                    updatedConfig = updatedConfig.copy(defaultDays = newDays, defaultLimit = newLimit)
                    println("Analysis settings updated - Days: $newDays, Limit: $newLimit")
                }

                "done" -> break
            }
        }
        
        UserConfig.save(updatedConfig)
        echo("Configuration saved successfully")
    }
}

// Summarize command
class Summarize : CliktCommand(
    name = "summarize",
    help = "Analyze and summarize Git commits"
) {
    private val days by option("-d", "--days", help = "Number of days to analyze").int()
    private val limit by option("-l", "--limit", help = "Maximum number of commits to analyze").int()
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
            
            val actualDays = days ?: config.defaultDays
            val actualLimit = limit ?: config.defaultLimit
            val commits = gitAnalyzer.getCommits(actualDays).take(actualLimit)
            if (commits.isEmpty()) {
                echo("No commits found in the last $actualDays days")
                return@runBlocking
            }
            
            echo("Found ${commits.size} commits (last $actualDays days, limit $actualLimit). Analyzing...")
            val summary = aiSummarizer.summarize(commits)
            
            echo("\n=== Summary ===\n")
            echo(summary)
        } catch (e: Exception) {
            echo("Error occurred: ${e.message}", err = true)
        }
    }
    
    private fun createAIConfig(config: UserConfig): Pair<AIProviderConfig, AIProviderType> {
        val locale = Locale.fromCode(config.locale ?: "en")
        val aiConfig = when (config.providerType?.lowercase()) {
            "claude" -> ClaudeConfig(apiKey = config.apiKey!!, modelName = config.modelName, locale = locale)
            "perplexity" -> PerplexityConfig(apiKey = config.apiKey!!, modelName = config.modelName, locale = locale)
            "deepseek" -> DeepSeekConfig(apiKey = config.apiKey!!, modelName = config.modelName, locale = locale)
            "gemini" -> GeminiConfig(apiKey = config.apiKey!!, modelName = config.modelName, locale = locale)
            else -> OpenAIConfig(apiKey = config.apiKey!!, modelName = config.modelName, locale = locale)
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

// PR command
class PR : CliktCommand(
    name = "pr",
    help = "Generate PR draft for current branch using AI"
) {
    private val days by option("-d", "--days", help = "Number of days to analyze").int()
    private val limit by option("-l", "--limit", help = "Maximum number of commits to analyze").int()
    private val path by option("--path", help = "Git repository path").path(mustExist = true, canBeFile = false).default(java.nio.file.Paths.get("."))
    
    override fun run() = runBlocking {
        val config = UserConfig.load()
        
        if (config.apiKey.isNullOrEmpty()) {
            echo("API key is required. Please run 'settings' to configure.")
            return@runBlocking
        }

        try {
            MessageBundleProvider.setLocale(Locale.fromCode(config.locale ?: "en"))
            
            val gitAnalyzer = GitAnalyzerFactory.create(path.absolutePathString())
            val aiConfig = createAIConfig(config)
            val aiSummarizer = AISummarizerFactory.create(aiConfig.first, aiConfig.second)
            
            val actualDays = days ?: config.defaultDays
            val actualLimit = limit ?: config.defaultLimit
            val currentBranch = gitAnalyzer.getCurrentBranch()
            
            // Warn if on main/master branch
            if (currentBranch.lowercase() in listOf("main", "master")) {
                echo("⚠️  Warning: You are on '$currentBranch' branch.")
                echo("PRs are usually created from feature branches, not main branches.")
                echo("Please switch to your feature branch and run the command again.")
                echo("Example: git checkout feature/my-feature")
                return@runBlocking
            }
            
            echo("Analyzing recent commits in '$currentBranch' branch (last $actualDays days)...")
            val commits = gitAnalyzer.getCommits(actualDays).take(actualLimit)
            
            if (commits.isEmpty()) {
                echo("No commits found in the last $actualDays days")
                return@runBlocking
            }

            val commitsWithChanges = commits.filter { it.changes.isNotEmpty() && !it.isMergeCommit }
            val mergeCommitCount = commits.count { it.isMergeCommit }
            
            if (commitsWithChanges.isEmpty()) {
                echo("⚠️  No meaningful changes found for PR generation.")
                if (mergeCommitCount > 0) {
                    echo("Found $mergeCommitCount merge commits, but no actual file changes.")
                    echo("This usually means you're on a branch that only contains merged changes.")
                }
                echo("Please ensure you're on a feature branch with your own commits.")
                return@runBlocking
            }
            
            if (mergeCommitCount > commitsWithChanges.size) {
                echo("⚠️  Most commits are merge commits (${mergeCommitCount}/${commits.size}).")
                echo("Consider running this command on your feature branch before merging.")
            }
            
            echo("Found ${commitsWithChanges.size} meaningful commits (${commits.size} total). Generating PR draft...")

            val commitsForPR = commitsWithChanges
            // Detect GitHub PR template
            val templateDetector = TemplateEngineFactory.createGitHubTemplateDetector()
            val githubTemplatePath = templateDetector.findPRTemplate(path.absolutePathString(), Locale.fromCode(config.locale ?: "en"))

            val prDraft = if (githubTemplatePath != null) {
                echo("GitHub PR template detected: $githubTemplatePath")
                val templateParser = TemplateEngineFactory.createGitHubTemplateParser(Locale.fromCode(config.locale ?: "en"))
                val templateContent = templateDetector.readTemplateContent(githubTemplatePath)
                
                if (templateContent != null) {
                    templateParser.parseAndRender(templateContent, commitsForPR, null)
                } else {
                    echo("Unable to read template file. Using AI generation instead.")
                    aiSummarizer.generatePRDraft(commitsForPR, null)
                }
            } else {
                aiSummarizer.generatePRDraft(commitsForPR, null)
            }
            
            echo("\n=== PR Draft ===\n")
            echo(prDraft)
        } catch (e: Exception) {
            echo("Error occurred: ${e.message}", err = true)
        }
    }
    
    private fun createAIConfig(config: UserConfig): Pair<AIProviderConfig, AIProviderType> {
        val locale = Locale.fromCode(config.locale ?: "en")
        val aiConfig = when (config.providerType?.lowercase()) {
            "claude" -> ClaudeConfig(apiKey = config.apiKey!!, modelName = config.modelName, locale = locale)
            "perplexity" -> PerplexityConfig(apiKey = config.apiKey!!, modelName = config.modelName, locale = locale)
            "deepseek" -> DeepSeekConfig(apiKey = config.apiKey!!, modelName = config.modelName, locale = locale)
            "gemini" -> GeminiConfig(apiKey = config.apiKey!!, modelName = config.modelName, locale = locale)
            else -> OpenAIConfig(apiKey = config.apiKey!!, modelName = config.modelName, locale = locale)
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

            if (config.apiKey.isNullOrEmpty() || config.providerType.isNullOrEmpty()) {
                echo("First-time setup detected.")
                InteractiveSetup().run()
                return
            }

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

// Main
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
