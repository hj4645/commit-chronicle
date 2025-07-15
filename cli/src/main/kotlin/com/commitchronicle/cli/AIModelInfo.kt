package com.commitchronicle.cli

enum class AIProvider(
    val displayName: String,
    val models: List<AIModel>
) {
    OPENAI("OpenAI", listOf(
        AIModel("gpt-4o", "GPT-4o (Latest)"),
        AIModel("gpt-4o-mini", "GPT-4o Mini (Latest)"),
        AIModel("gpt-4-turbo", "GPT-4 Turbo"),
        AIModel("gpt-4", "GPT-4"),
        AIModel("gpt-3.5-turbo", "GPT-3.5 Turbo")
    )),
    CLAUDE("Claude", listOf(
        AIModel("claude-3-5-haiku-20241022", "Claude 3.5 Haiku (Latest)"),
        AIModel("claude-3-haiku-20240307", "Claude 3 Haiku (Fast)"),
        AIModel("claude-3-5-sonnet-20241022", "Claude 3.5 Sonnet (Latest)"),
        AIModel("claude-3-7-sonnet-20250219", "Claude 3.7 Sonnet (Latest)")
    )),
    GEMINI("Gemini", listOf(
        AIModel("gemini-2.5-pro", "Gemini 2.5 Pro (Latest)"),
        AIModel("gemini-2.0-flash", "Gemini 2.0 Flash (Latest)"),
        AIModel("gemini-2.5-flash", "Gemini 2.5 Flash (Latest)")
    )),
    PERPLEXITY("Perplexity", listOf(
        AIModel("llama-3.1-sonar-small-128k-online", "Llama 3.1 Sonar Small"),
        AIModel("llama-3.1-sonar-large-128k-online", "Llama 3.1 Sonar Large"),
        AIModel("llama-3.1-sonar-huge-128k-online", "Llama 3.1 Sonar Huge")
    )),
    DEEPSEEK("DeepSeek", listOf(
        AIModel("deepseek-chat", "DeepSeek Chat"),
        AIModel("deepseek-coder", "DeepSeek Coder")
    ));

    val key: String get() = name.lowercase()
    val defaultModel: AIModel get() = models.first()
    
    companion object {
        fun fromKey(key: String): AIProvider? = values().find { it.key == key.lowercase() }
        
        fun getProviderOptions(): List<Pair<String, String>> = values().map { it.key to it.displayName }
    }
}

data class AIModel(
    val id: String,
    val displayName: String
) {
    fun toPair(): Pair<String, String> = id to displayName
}

enum class SupportedLanguage(
    val code: String,
    val displayName: String
) {
    ENGLISH("en", "English"),
    KOREAN("ko", "한국어 (Korean)"),
    CHINESE("zh", "中文 (Chinese)"),
    JAPANESE("ja", "日本語 (Japanese)");
    
    fun toPair(): Pair<String, String> = code to displayName
    
    companion object {
        fun getLanguageOptions(): List<Pair<String, String>> = values().map { it.toPair() }
        
        fun fromCode(code: String): SupportedLanguage? = values().find { it.code == code }
    }
} 