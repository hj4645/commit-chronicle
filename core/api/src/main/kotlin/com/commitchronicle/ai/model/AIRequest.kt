package com.commitchronicle.ai.model

/**
 * AI 요청 모델
 */
data class AIRequest(
    val model: String,
    val messages: List<AIMessage>,
    val temperature: Double = 0.7,
    val maxTokens: Int = 2000
) 