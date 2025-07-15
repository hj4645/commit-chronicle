package com.commitchronicle.ai.base

import com.commitchronicle.ai.AIProviderConfig
import com.commitchronicle.ai.AISummarizer
import com.commitchronicle.git.model.Commit
import java.time.format.DateTimeFormatter
import com.commitchronicle.language.Locale

/**
 * 모든 AI 요약 구현체를 위한 기본 추상 클래스
 */
abstract class BaseSummarizer(protected val config: AIProviderConfig) : AISummarizer {

    protected val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    protected val locale: Locale = config.locale ?: Locale.ENGLISH

    /**
     * 프롬프트용 커밋 텍스트 포맷팅
     */
    protected fun formatCommitsForPrompt(commits: List<Commit>): String {
        return commits.joinToString("\n\n") { commit ->
            buildString {
                append("Commit: ${commit.shortId}\n")
                append("Author: ${commit.author} (${commit.email})\n")
                append("Date: ${commit.date.format(dateFormatter)}\n")
                append("Message: ${commit.message}\n")

                if (commit.changes.isNotEmpty()) {
                    append("Changed files:\n")
                    commit.changes.forEach { change ->
                        val processedDiff = DiffProcessor.processDiff(change.diff)
                        val changeSummary = DiffProcessor.generateChangeSummary(change.diff)
                        
                        append("- ${change.path} (${change.changeType.name}, $changeSummary)\n")
                        
                        if (processedDiff.isNotEmpty()) {
                            append("  Key changes:\n")
                            processedDiff.lines().forEach { line ->
                                append("    $line\n")
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 요약용 프롬프트 생성
     */
    protected fun buildSummaryPrompt(commits: List<Commit>): String {
        val commitsText = formatCommitsForPrompt(commits)
        val language = locale.languageName
        
        return """
            Please analyze the following git commits and provide a concise summary.
            Focus on the main changes, new features, and important fixes.
            Format the summary in a clear and structured way.
            
            IMPORTANT: Please provide the response in $language language.
            
            $commitsText
        """.trimIndent()
    }

    /**
     * PR 초안용 프롬프트 생성
     */
    protected fun buildPRPrompt(commits: List<Commit>, title: String?): String {
        val commitsText = formatCommitsForPrompt(commits)
        val changesSummary = generateChangesSummaryForPR(commits)
        val language = locale.languageName
        
        return """
            Please generate a comprehensive PR description based on the following git commits and code changes.
            ${title?.let { "PR Title: $it" } ?: ""}
            
            Focus on:
            1. Overview of changes made (based on actual code changes below)
            2. Technical implementation details
            3. Impact and benefits of these changes
            4. Breaking changes (if any)
            5. Testing considerations
            
            IMPORTANT: Please provide the response in $language language.
            Use the actual code changes to provide specific details about what was modified.
            
            ## Changes Summary
            $changesSummary
            
            ## Detailed Commits
            $commitsText
        """.trimIndent()
    }

    /**
     * PR용 변경사항 요약 생성
     */
    private fun generateChangesSummaryForPR(commits: List<Commit>): String {
        val allChanges = commits.flatMap { it.changes }
        val filesByType = allChanges.groupBy { getFileType(it.path) }
        
        val summary = StringBuilder()
        
        // 파일 타입별 변경 통계
        summary.append("### Files Changed by Type\n")
        filesByType.forEach { (fileType, changes) ->
            val fileCount = changes.size
            val meaningfulChanges = changes.count { DiffProcessor.processDiff(it.diff).isNotEmpty() }
            summary.append("- **$fileType**: $fileCount files ($meaningfulChanges with meaningful changes)\n")
        }
        
        // 주요 변경사항 하이라이트
        summary.append("\n### Key Changes\n")
        allChanges.forEach { change ->
            val processedDiff = DiffProcessor.processDiff(change.diff)
            if (processedDiff.isNotEmpty()) {
                val changeSummary = DiffProcessor.generateChangeSummary(change.diff)
                summary.append("**${change.path}** (${change.changeType.name}): $changeSummary\n")
                
                // 핵심 변경사항 몇 줄만 보여주기
                val keyLines = processedDiff.lines().take(3)
                if (keyLines.isNotEmpty()) {
                    summary.append("```\n")
                    keyLines.forEach { summary.append("$it\n") }
                    if (processedDiff.lines().size > 3) {
                        summary.append("... (${processedDiff.lines().size - 3} more lines)\n")
                    }
                    summary.append("```\n\n")
                }
            }
        }
        
        return summary.toString()
    }
    
    /**
     * 파일 확장자를 기반으로 파일 타입 결정
     */
    private fun getFileType(filePath: String): String {
        return when (filePath.substringAfterLast('.', "")) {
            "kt" -> "Kotlin"
            "java" -> "Java"
            "js", "ts" -> "JavaScript/TypeScript"
            "py" -> "Python"
            "go" -> "Go"
            "rs" -> "Rust"
            "cpp", "c", "h" -> "C/C++"
            "md" -> "Documentation"
            "yml", "yaml" -> "Configuration"
            "json" -> "JSON"
            "xml" -> "XML"
            "gradle", "kts" -> "Build Script"
            "properties" -> "Properties"
            else -> "Other"
        }
    }

    /**
     * 템플릿 기반 PR 초안 생성 (기본 구현)
     */
    override suspend fun generatePRDraftWithTemplate(commits: List<Commit>, template: String): String {
        return callAIModel(buildPRPromptWithTemplate(commits, template))
    }
    
    /**
     * 템플릿 기반 PR 초안용 프롬프트 생성
     */
    protected fun buildPRPromptWithTemplate(commits: List<Commit>, template: String): String {
        val commitsText = formatCommitsForPrompt(commits)
        val changesSummary = generateChangesSummaryForPR(commits)
        val language = locale.languageName
        
        return """
            Please generate a comprehensive PR description based on the following git commits and code changes.
            You MUST follow the provided template structure exactly and fill in detailed, meaningful content.
            
            TEMPLATE TO FOLLOW:
            $template
            
            INSTRUCTIONS:
            1. Use the exact template structure and sections
            2. Fill each section with detailed, technical information based on the actual code changes
            3. Be specific about what was changed, why it was changed, and the impact
            4. Include technical implementation details where relevant
            5. For checklists, mark items as checked [x] or unchecked [ ] based on the changes
            6. Write in $language language
            7. DO NOT include template variable syntax like {{}} in your response
            
            ## Changes Summary
            $changesSummary
            
            ## Detailed Commits
            $commitsText
            
            Generate the PR description following the template structure above.
        """.trimIndent()
    }

    /**
     * 변경로그용 프롬프트 생성
     */
    protected fun buildChangelogPrompt(commits: List<Commit>, groupByType: Boolean): String {
        val commitsText = formatCommitsForPrompt(commits)
        val groupingInstruction = if (groupByType) {
            "Group the changes by type (feature, bugfix, documentation, refactoring, etc.)"
        } else {
            "List the changes in chronological order"
        }
        val language = locale.languageName
        
        return """
            Please generate a changelog based on the following git commits.
            
            $commitsText
            
            $groupingInstruction
            
            IMPORTANT: Please provide the response in $language language.
            
            Format the changelog in Markdown.
        """.trimIndent()
    }

    /**
     * AI 모델에 요청 전송
     */
    protected abstract suspend fun callAIModel(prompt: String): String
}