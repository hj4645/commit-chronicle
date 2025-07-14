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
                        append("- ${change.path} (${change.changeType.name}, +${change.additions}, -${change.deletions})\n")
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
        val language = locale.languageName
        
        return """
            Please generate a PR description based on the following git commits.
            ${title?.let { "PR Title: $it" } ?: ""}
            
            Focus on:
            1. Changes made
            2. Impact of changes
            3. Testing done
            4. Additional notes
            
            IMPORTANT: Please provide the response in $language language.
            
            Commits:
            $commitsText
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