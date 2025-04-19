package com.commitchronicle.ai

import kotlin.reflect.full.primaryConstructor

/**
 * AISummarizer 구현체를 생성하는 팩토리 클래스
 */
object AISummarizerFactory {
    /**
     * API 키와 MCP 사용 여부에 따라 적절한 AISummarizer 구현체를 생성합니다.
     * MCP를 사용하는 경우 McpSummarizer를 동적으로 로드하고, 실패 시 OpenAISummarizer로 폴백합니다.
     *
     * @param apiKey OpenAI API 키
     * @param useMcp MCP를 사용할지 여부 (JDK 17+ 필요)
     * @return AISummarizer 구현체
     */
    fun create(apiKey: String, useMcp: Boolean): AISummarizer {
        return if (useMcp) {
            try {
                // 클래스 이름으로 인스턴스 로드
                val mcpClassName = "com.commitchronicle.ai.McpSummarizer"
                val classLoader = this.javaClass.classLoader
                val mcpJavaClass = classLoader.loadClass(mcpClassName)
                val mcpClass = mcpJavaClass.kotlin
                
                // 생성자 가져와서 인스턴스 생성
                val constructor = mcpClass.primaryConstructor
                    ?: throw IllegalStateException("McpSummarizer에 기본 생성자가 없습니다.")
                
                constructor.call(apiKey) as AISummarizer
            } catch (e: Exception) {
                // MCP 로드 실패 시 기본 구현체로 폴백
                OpenAISummarizer(apiKey)
            }
        } else {
            // MCP를 사용하지 않는 경우 기본 구현체 반환
            OpenAISummarizer(apiKey)
        }
    }
} 