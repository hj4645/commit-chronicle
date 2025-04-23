package com.commitchronicle.ai

/**
 * AI 프로바이더 타입을 정의하는 열거형
 */
enum class AIProviderType {
    OPENAI,
    CLAUDE,
    PERPLEXITY
}

/**
 * AI 프로바이더 설정 인터페이스
 */
interface AIProviderConfig {
    val apiKey: String
    val modelName: String?
}

/**
 * 기본 AI 프로바이더 설정 구현체
 */
data class DefaultAIProviderConfig(
    override val apiKey: String,
    override val modelName: String? = null
) : AIProviderConfig 