package com.commitchronicle.template

/**
 * GitHub 템플릿 감지 인터페이스
 */
interface GitHubTemplateDetector {
    /**
     * 저장소에서 GitHub PR 템플릿을 찾습니다.
     *
     * @param repositoryPath Git 저장소 경로
     * @return 찾은 템플릿 파일 경로 (없으면 null)
     */
    fun findPRTemplate(repositoryPath: String): String?
    
    /**
     * 저장소에서 로케일에 맞는 GitHub PR 템플릿을 찾습니다.
     *
     * @param repositoryPath Git 저장소 경로
     * @param locale 로케일
     * @return 찾은 템플릿 파일 경로 (없으면 null)
     */
    fun findPRTemplate(repositoryPath: String, locale: com.commitchronicle.language.Locale): String?
    
    /**
     * 저장소에서 GitHub Issue 템플릿을 찾습니다.
     *
     * @param repositoryPath Git 저장소 경로
     * @return 찾은 템플릿 파일 경로들 (없으면 빈 리스트)
     */
    fun findIssueTemplates(repositoryPath: String): List<String>
    
    /**
     * 저장소에서 모든 GitHub 템플릿을 찾습니다.
     *
     * @param repositoryPath Git 저장소 경로
     * @return 찾은 템플릿 정보 맵 (템플릿 타입 -> 파일 경로)
     */
    fun findAllTemplates(repositoryPath: String): Map<String, String>
    
    /**
     * 템플릿 파일이 유효한지 확인합니다.
     *
     * @param templatePath 템플릿 파일 경로
     * @return 유효한 템플릿인지 여부
     */
    fun isValidTemplate(templatePath: String): Boolean
    
    /**
     * 템플릿 파일의 내용을 읽습니다.
     *
     * @param templatePath 템플릿 파일 경로
     * @return 템플릿 내용 (읽기 실패 시 null)
     */
    fun readTemplateContent(templatePath: String): String?
} 