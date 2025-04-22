package com.commitchronicle.ai

/**
 * AISummarizer 구현체를 생성하는 팩토리 클래스
 */
object AISummarizerFactory {
    /**
     * API 키와 엔진 유형에 따라 적절한 AISummarizer 구현체를 생성합니다.
     *
     * @param apiKey OpenAI API 키
     * @param engine 사용할, AI 엔진 (기본값: "openai")
     * @return AISummarizer 구현체
     */
    fun create(apiKey: String, engine: String = "openai"): AISummarizer {
        return when (engine) {
            // 향후 다른 AI 엔진 추가 가능성을 위해 when 구문 유지
            else -> OpenAISummarizer(apiKey)
        }
    }
} 