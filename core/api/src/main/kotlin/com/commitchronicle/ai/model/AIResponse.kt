package com.commitchronicle.ai.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * AI 응답 모델
 */
data class AIResponse(
    val id: String? = null,
    val model: String? = null,
    val created: Long? = null,
    val choices: List<AIChoice> = emptyList(),
    val usage: TokenUsage? = null
) {
    fun getFirstChoiceContent(): String {
        return choices.firstOrNull()?.message?.content ?: "응답을 생성할 수 없습니다."
    }
}

/**
 * AI 선택 모델
 */
data class AIChoice(
    val index: Int = 0,
    val message: AIMessage? = null,
    @JsonProperty("finish_reason") val finishReason: String? = null
)

/**
 * 토큰 사용량 모델
 */
data class TokenUsage(
    @JsonProperty("prompt_tokens") val promptTokens: Int? = null,
    @JsonProperty("completion_tokens") val completionTokens: Int? = null,
    @JsonProperty("total_tokens") val totalTokens: Int? = null
) 