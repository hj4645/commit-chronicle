package com.commitchronicle.ai.providers.gemini.config

import com.commitchronicle.ai.AIProviderConfig
import com.commitchronicle.language.Locale

/**
 * Gemini 설정
 */
data class GeminiConfig(
    override val apiKey: String,
    override val modelName: String? = "gemini-2.0-flash",
    override val locale: Locale? = null,
    val temperature: Double = 0.7,
    val maxOutputTokens: Int = 2048,
    val topP: Double = 1.0,
    val topK: Int = 40
) : AIProviderConfig