package com.commitchronicle.ai.providers.perplexity.config

import com.commitchronicle.ai.AIProviderConfig
import com.commitchronicle.language.Locale

/**
 * Perplexity 설정
 */
data class PerplexityConfig(
    override val apiKey: String,
    override val modelName: String? = "pplx-7b-online",
    override val locale: Locale? = null,
    val temperature: Double = 0.7,
    val maxTokens: Int = 2000,
    val topP: Double = 1.0,
    val presencePenalty: Double = 0.0,
    val frequencyPenalty: Double = 0.0
) : AIProviderConfig