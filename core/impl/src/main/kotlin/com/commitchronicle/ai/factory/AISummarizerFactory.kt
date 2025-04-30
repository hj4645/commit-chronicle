package com.commitchronicle.ai.factory

import com.commitchronicle.ai.AIProviderConfig
import com.commitchronicle.ai.AIProviderType
import com.commitchronicle.ai.AISummarizer

/**
 * AISummarizer 구현체를 생성하는 팩토리 클래스
 */
object AISummarizerFactory {
    /**
     * 커스텀 설정으로 AISummarizer 생성
     */
    fun create(config: AIProviderConfig, provider: AIProviderType = AIProviderType.OPENAI): AISummarizer {
        // ServiceLoader를 통한 구현체 로딩 또는 직접 인스턴스 생성
        return when (provider) {
            AIProviderType.OPENAI -> Class.forName("com.commitchronicle.ai.providers.openai.OpenAISummarizer")
                .getDeclaredConstructor(AIProviderConfig::class.java)
                .newInstance(config) as AISummarizer

            AIProviderType.CLAUDE -> Class.forName("com.commitchronicle.ai.providers.claude.ClaudeSummarizer")
                .getDeclaredConstructor(AIProviderConfig::class.java)
                .newInstance(config) as AISummarizer

            AIProviderType.PERPLEXITY -> Class.forName("com.commitchronicle.ai.providers.perplexity.PerplexitySummarizer")
                .getDeclaredConstructor(AIProviderConfig::class.java)
                .newInstance(config) as AISummarizer

            AIProviderType.DEEPSEEK -> Class.forName("com.commitchronicle.ai.providers.deepseek.DeepSeekSummarizer")
                .getDeclaredConstructor(AIProviderConfig::class.java)
                .newInstance(config) as AISummarizer

            AIProviderType.GEMINI -> Class.forName("com.commitchronicle.ai.providers.gemini.GeminiSummarizer")
                .getDeclaredConstructor(AIProviderConfig::class.java)
                .newInstance(config) as AISummarizer
        }
    }
}