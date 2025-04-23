package com.commitchronicle.ai

import com.commitchronicle.model.Commit
import java.time.format.DateTimeFormatter

/**
 * 모든 AI 요약 구현체를 위한 기본 추상 클래스
 */
abstract class BaseSummarizer(protected val config: AIProviderConfig) : AISummarizer {
    
    protected val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    
    /**
     * 프롬프트용 커밋 텍스트 포맷팅
     */
    protected fun formatCommitsForPrompt(commits: List<Commit>): String {
        return commits.joinToString("\n\n") { commit ->
            buildString {
                append("커밋: ${commit.shortId}\n")
                append("작성자: ${commit.author} (${commit.email})\n")
                append("날짜: ${commit.date.format(dateFormatter)}\n")
                append("메시지: ${commit.message}\n")
                
                if (commit.changes.isNotEmpty()) {
                    append("변경 파일:\n")
                    commit.changes.forEach { change ->
                        append("- ${change.path} (${change.changeType.name}, +${change.additions}, -${change.deletions})\n")
                    }
                }
            }
        }
    }
    
    /**
     * AI 모델에 요청 전송
     */
    protected abstract suspend fun callAIModel(prompt: String): String
} 