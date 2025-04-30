package com.commitchronicle.ai.providers.deepseek.config

import com.commitchronicle.ai.AIProviderConfig
import com.commitchronicle.language.Locale

/**
 * DeepSeek 설정
 */
data class DeepSeekConfig(
    override val apiKey: String,
    override val modelName: String? = "deepseek-chat",
    override val locale: Locale? = null,
    val temperature: Double = 0.7,
    val maxTokens: Int = 2000,
    val topP: Double = 1.0
) : AIProviderConfig