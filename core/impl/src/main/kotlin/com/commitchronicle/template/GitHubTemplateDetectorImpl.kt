package com.commitchronicle.template

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * GitHub 템플릿 감지 구현체
 */
class GitHubTemplateDetectorImpl : GitHubTemplateDetector {
    
    companion object {
        // GitHub에서 지원하는 PR 템플릿 경로들
        private val PR_TEMPLATE_PATHS = listOf(
            ".github/pull_request_template.md",
            ".github/PULL_REQUEST_TEMPLATE.md", 
            ".github/PULL_REQUEST_TEMPLATE/pull_request_template.md",
            "docs/pull_request_template.md",
            "docs/PULL_REQUEST_TEMPLATE.md",
            "pull_request_template.md",
            "PULL_REQUEST_TEMPLATE.md"
        )
        
        // 로케일별 PR 템플릿 경로들
        private fun getPRTemplatePathsForLocale(locale: com.commitchronicle.language.Locale): List<String> {
            val suffix = when (locale) {
                com.commitchronicle.language.Locale.ENGLISH -> "_en"
                com.commitchronicle.language.Locale.KOREAN -> "_ko"
                else -> ""
            }
            
            return listOf(
                ".github/pull_request_template${suffix}.md",
                ".github/PULL_REQUEST_TEMPLATE${suffix}.md", 
                ".github/PULL_REQUEST_TEMPLATE/pull_request_template${suffix}.md",
                "docs/pull_request_template${suffix}.md",
                "docs/PULL_REQUEST_TEMPLATE${suffix}.md",
                "pull_request_template${suffix}.md",
                "PULL_REQUEST_TEMPLATE${suffix}.md"
            ) + PR_TEMPLATE_PATHS // 로케일별 템플릿이 없으면 기본 템플릿 사용
        }
        
        // GitHub에서 지원하는 Issue 템플릿 경로들
        private val ISSUE_TEMPLATE_PATHS = listOf(
            ".github/ISSUE_TEMPLATE/",
            ".github/issue_template.md",
            ".github/ISSUE_TEMPLATE.md",
            "docs/issue_template.md",
            "docs/ISSUE_TEMPLATE.md",
            "issue_template.md",
            "ISSUE_TEMPLATE.md"
        )
    }
    
    override fun findPRTemplate(repositoryPath: String): String? {
        val repoPath = Paths.get(repositoryPath)
        
        for (templatePath in PR_TEMPLATE_PATHS) {
            val fullPath = repoPath.resolve(templatePath)
            if (Files.exists(fullPath) && Files.isRegularFile(fullPath)) {
                return fullPath.toString()
            }
        }
        
        return null
    }
    
    override fun findPRTemplate(repositoryPath: String, locale: com.commitchronicle.language.Locale): String? {
        val repoPath = Paths.get(repositoryPath)
        val templatePaths = getPRTemplatePathsForLocale(locale)
        
        for (templatePath in templatePaths) {
            val fullPath = repoPath.resolve(templatePath)
            if (Files.exists(fullPath) && Files.isRegularFile(fullPath)) {
                return fullPath.toString()
            }
        }
        
        return null
    }
    
    override fun findIssueTemplates(repositoryPath: String): List<String> {
        val repoPath = Paths.get(repositoryPath)
        val templates = mutableListOf<String>()
        
        for (templatePath in ISSUE_TEMPLATE_PATHS) {
            val fullPath = repoPath.resolve(templatePath)
            
            if (Files.exists(fullPath)) {
                if (Files.isRegularFile(fullPath)) {
                    // 단일 파일 템플릿
                    templates.add(fullPath.toString())
                } else if (Files.isDirectory(fullPath)) {
                    // 디렉토리 내의 모든 .md 파일들
                    try {
                        Files.walk(fullPath)
                            .filter { Files.isRegularFile(it) }
                            .filter { it.toString().endsWith(".md") || it.toString().endsWith(".yml") || it.toString().endsWith(".yaml") }
                            .forEach { templates.add(it.toString()) }
                    } catch (e: Exception) {
                        // 디렉토리 읽기 실패 시 무시
                    }
                }
            }
        }
        
        return templates
    }
    
    override fun findAllTemplates(repositoryPath: String): Map<String, String> {
        val templates = mutableMapOf<String, String>()
        
        // PR 템플릿 찾기
        findPRTemplate(repositoryPath)?.let { prTemplate ->
            templates["pr"] = prTemplate
        }
        
        // Issue 템플릿 찾기 (첫 번째 것만 사용)
        val issueTemplates = findIssueTemplates(repositoryPath)
        if (issueTemplates.isNotEmpty()) {
            templates["issue"] = issueTemplates.first()
        }
        
        return templates
    }
    
    /**
     * 템플릿 파일이 유효한지 확인합니다.
     */
    override fun isValidTemplate(templatePath: String): Boolean {
        val file = File(templatePath)
        return file.exists() && file.isFile && file.canRead() && file.length() > 0
    }
    
    /**
     * 템플릿 파일의 내용을 읽습니다.
     */
    override fun readTemplateContent(templatePath: String): String? {
        return try {
            if (isValidTemplate(templatePath)) {
                File(templatePath).readText(Charsets.UTF_8)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
} 