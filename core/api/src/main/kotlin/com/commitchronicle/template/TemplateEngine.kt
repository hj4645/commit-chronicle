package com.commitchronicle.template

/**
 * 템플릿 엔진 인터페이스
 */
interface TemplateEngine {
    /**
     * 파일 경로의 템플릿을 렌더링합니다.
     *
     * @param templatePath 템플릿 파일 경로
     * @param data 템플릿에 전달할 데이터
     * @return 렌더링된 텍스트
     */
    fun render(templatePath: String, data: Map<String, Any>): String
    
    /**
     * 템플릿 문자열을 렌더링합니다.
     *
     * @param templateContent 템플릿 내용
     * @param data 템플릿에 전달할 데이터
     * @return 렌더링된 텍스트
     */
    fun renderString(templateContent: String, data: Map<String, Any>): String
} 