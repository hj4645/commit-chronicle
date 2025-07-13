package com.commitchronicle.template

import com.commitchronicle.git.model.Commit
import com.commitchronicle.language.Locale
import com.commitchronicle.language.MessageBundle
import com.commitchronicle.language.MessageBundleImpl
import java.util.regex.Pattern

/**
 * GitHub 템플릿 파서 및 데이터 바인딩 클래스
 */
class GitHubTemplateParser(private val locale: Locale = Locale.ENGLISH) {
    
    private val messageBundle: MessageBundle = MessageBundleImpl()
    
    companion object {
        // GitHub 템플릿에서 사용되는 일반적인 패턴들
        private val CHECKBOX_PATTERN = Pattern.compile("^\\s*-\\s*\\[\\s*\\]\\s*(.+)$", Pattern.MULTILINE)
        private val SECTION_PATTERN = Pattern.compile("^#+\\s*(.+)$", Pattern.MULTILINE)
        private val COMMENT_PATTERN = Pattern.compile("<!--.*?-->", Pattern.DOTALL)
        
        // 템플릿 변수 패턴
        private val TEMPLATE_VAR_PATTERN = Pattern.compile("\\{\\{\\s*([\\w.]+)\\s*\\}\\}")
    }
    
    /**
     * GitHub 템플릿을 파싱하여 커밋 정보와 함께 렌더링합니다.
     */
    fun parseAndRender(templateContent: String, commits: List<Commit>, title: String? = null): String {
        var result = templateContent
        
        // 주석 제거 (GitHub 템플릿에서 자주 사용됨)
        result = COMMENT_PATTERN.matcher(result).replaceAll("")
        
        // 템플릿 변수 치환
        result = replaceTemplateVariables(result, commits, title)
        
        // 체크박스 자동 처리
        result = processCheckboxes(result, commits)
        
        return result.trim()
    }
    
    /**
     * 템플릿 변수를 실제 값으로 치환합니다.
     */
    private fun replaceTemplateVariables(content: String, commits: List<Commit>, title: String?): String {
        val matcher = TEMPLATE_VAR_PATTERN.matcher(content)
        val result = StringBuffer()
        
        while (matcher.find()) {
            val variableName = matcher.group(1)
            val value = getVariableValue(variableName, commits, title)
            matcher.appendReplacement(result, value.replace("$", "\\$"))
        }
        
        matcher.appendTail(result)
        return result.toString()
    }
    
    /**
     * 변수명에 따라 적절한 값을 반환합니다.
     */
    private fun getVariableValue(variableName: String, commits: List<Commit>, title: String?): String {
        return when (variableName.lowercase()) {
            "title" -> title ?: getLocalizedMessage("default.pr.title")
            "commits.count" -> commits.size.toString()
            "commits.summary" -> generateCommitSummary(commits)
            "commits.list" -> generateCommitList(commits)
            "commits.files" -> generateFilesList(commits)
            "commits.authors" -> generateAuthorsList(commits)
            "date" -> java.time.LocalDate.now().toString()
            "datetime" -> java.time.LocalDateTime.now().toString()
            else -> "{{$variableName}}" // 알 수 없는 변수는 그대로 유지
        }
    }
    
    /**
     * 체크박스를 자동으로 처리합니다.
     */
    private fun processCheckboxes(content: String, commits: List<Commit>): String {
        val matcher = CHECKBOX_PATTERN.matcher(content)
        val result = StringBuffer()
        
        while (matcher.find()) {
            val checkboxText = matcher.group(1)
            val isChecked = shouldCheckBox(checkboxText, commits)
            val replacement = "- [${if (isChecked) "x" else " "}] $checkboxText"
            matcher.appendReplacement(result, replacement)
        }
        
        matcher.appendTail(result)
        return result.toString()
    }
    
    /**
     * 체크박스를 자동으로 체크할지 결정합니다.
     */
    private fun shouldCheckBox(checkboxText: String, commits: List<Commit>): Boolean {
        val lowerText = checkboxText.lowercase()
        
        // 일반적인 체크박스 패턴들
        return when {
            lowerText.contains("test") && hasTestRelatedCommits(commits) -> true
            lowerText.contains("documentation") && hasDocumentationCommits(commits) -> true
            lowerText.contains("breaking") && hasBreakingChanges(commits) -> true
            lowerText.contains("fix") && hasBugFixes(commits) -> true
            lowerText.contains("feature") && hasNewFeatures(commits) -> true
            else -> false
        }
    }
    
    /**
     * 커밋 요약을 생성합니다.
     */
    private fun generateCommitSummary(commits: List<Commit>): String {
        if (commits.isEmpty()) return getLocalizedMessage("template.no.changes")
        
        val summary = StringBuilder()
        summary.append(getLocalizedMessage("template.commit.summary", commits.size.toString()))
        
        val authors = commits.map { it.author }.distinct()
        if (authors.size > 1) {
            summary.append(getLocalizedMessage("template.participants", authors.joinToString(", ")))
        }
        
        return summary.toString()
    }
    
    /**
     * 커밋 목록을 생성합니다.
     */
    private fun generateCommitList(commits: List<Commit>): String {
        return commits.joinToString("\n") { commit ->
            "- ${commit.message.lines().first()} (${commit.author})"
        }
    }
    
    /**
     * 변경된 파일 목록을 생성합니다.
     */
    private fun generateFilesList(commits: List<Commit>): String {
        val allFiles = commits.flatMap { it.changes.map { change -> change.path } }.distinct()
        return allFiles.joinToString("\n") { "- $it" }
    }
    
    /**
     * 작성자 목록을 생성합니다.
     */
    private fun generateAuthorsList(commits: List<Commit>): String {
        val authors = commits.map { it.author }.distinct()
        return authors.joinToString(", ")
    }
    
    // 헬퍼 메서드들
    private fun hasTestRelatedCommits(commits: List<Commit>): Boolean {
        return commits.any { commit ->
            commit.message.lowercase().contains("test") || 
            commit.changes.any { it.path.lowercase().contains("test") }
        }
    }
    
    private fun hasDocumentationCommits(commits: List<Commit>): Boolean {
        return commits.any { commit ->
            commit.message.lowercase().contains("doc") || 
            commit.changes.any { it.path.lowercase().contains("readme") || it.path.lowercase().contains(".md") }
        }
    }
    
    private fun hasBreakingChanges(commits: List<Commit>): Boolean {
        return commits.any { commit ->
            commit.message.lowercase().contains("breaking") || 
            commit.message.lowercase().contains("major")
        }
    }
    
    private fun hasBugFixes(commits: List<Commit>): Boolean {
        return commits.any { commit ->
            commit.message.lowercase().contains("fix") || 
            commit.message.lowercase().contains("bug")
        }
    }
    
    private fun hasNewFeatures(commits: List<Commit>): Boolean {
        return commits.any { commit ->
            commit.message.lowercase().contains("feat") || 
            commit.message.lowercase().contains("feature") ||
            commit.message.lowercase().contains("add")
        }
    }
    
    /**
     * 로케일에 따른 메시지를 반환합니다.
     */
    private fun getLocalizedMessage(key: String, vararg args: String): String {
        val message = messageBundle.getMessage(key, locale)
        return if (args.isNotEmpty()) {
            String.format(message, *args)
        } else {
            message
        }
    }
} 