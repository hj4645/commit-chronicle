package com.commitchronicle.ai

/**
 * AISummarizer 구현체를 생성하는 팩토리 클래스
 */
object AISummarizerFactory {
    /**
     * 기본설정 - OpenAI로 API 키와 엔진 유형에 따라 적절한 AISummarizer 구현체를 생성합니다.
     *
     * @param apiKey OpenAI API 키
     * @param engine 사용할, AI 엔진 (기본값: "openai")
     * @return AISummarizer 구현체
     */
    fun create(apiKey: String, provider: AIProviderType = AIProviderType.OPENAI): AISummarizer {
        return create(DefaultAIProviderConfig(apiKey), provider)
    }

    /**
     * 커스텀 설정으로 AISummarizer 생성
     */
    fun create(config: AIProviderConfig, provider: AIProviderType = AIProviderType.OPENAI): AISummarizer {
        // ServiceLoader를 통한 구현체 로딩 또는 직접 인스턴스 생성
        return when (provider) {
            AIProviderType.OPENAI -> Class.forName("com.commitchronicle.ai.OpenAISummarizer")
                .getDeclaredConstructor(AIProviderConfig::class.java)
                .newInstance(config) as AISummarizer

            AIProviderType.CLAUDE -> Class.forName("com.commitchronicle.ai.ClaudeSummarizer")
                .getDeclaredConstructor(AIProviderConfig::class.java)
                .newInstance(config) as AISummarizer

            AIProviderType.PERPLEXITY -> Class.forName("com.commitchronicle.ai.PerplexitySummarizer")
                .getDeclaredConstructor(AIProviderConfig::class.java)
                .newInstance(config) as AISummarizer
        }
    }
} 