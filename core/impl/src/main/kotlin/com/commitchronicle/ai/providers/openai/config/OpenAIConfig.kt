package com.commitchronicle.ai.providers.openai.config

import com.commitchronicle.ai.AIProviderConfig
import com.commitchronicle.language.Locale

/**
 * OpenAI 설정
 */
data class OpenAIConfig(
    override val apiKey: String,
    override val modelName: String? = "gpt-3.5-turbo",
    override val locale: Locale? = null,
    val temperature: Double = 0.7,
    val maxTokens: Int = 2000,
    val organization: String? = null
) : AIProviderConfig