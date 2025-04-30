package com.commitchronicle.ai

import com.commitchronicle.language.Locale

/**
 * AI 프로바이더 타입을 정의하는 열거형
 */
enum class AIProviderType {
    OPENAI,
    CLAUDE,
    PERPLEXITY,
    DEEPSEEK,
    GEMINI
}

/**
 * AI 프로바이더 설정 인터페이스
 */
interface AIProviderConfig {
    val apiKey: String
    val modelName: String?
    val locale: Locale?
}