package com.commitchronicle.template

/**
 * TemplateEngine 구현체를 생성하는 팩토리 클래스
 */
object TemplateEngineFactory {
    /**
     * 엔진 유형에 따라 적절한 TemplateEngine 구현체를 생성합니다.
     *
     * @param engine 사용할 템플릿 엔진 (기본값: "markdown")
     * @return TemplateEngine 구현체
     */
    fun create(engine: String = "markdown"): TemplateEngine {
        return when (engine) {
            // 향후 다른 템플릿 엔진 추가 가능성을 위해 when 구문 유지
            else -> MarkdownTemplateEngine()
        }
    }
} 