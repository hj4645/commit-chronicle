package com.commitchronicle.ai.providers.claude.config

import com.commitchronicle.ai.AIProviderConfig
import com.commitchronicle.language.Locale

/**
 * Claude 설정
 */
data class ClaudeConfig(
    override val apiKey: String,
    override val modelName: String? = "claude-3-opus-20240229",
    override val locale: Locale? = null,
    val temperature: Double = 0.7,
    val maxTokens: Int = 4000,
    val systemPrompt: String? = null
) : AIProviderConfig