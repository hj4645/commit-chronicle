package com.commitchronicle.ai

import com.commitchronicle.model.Commit
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.format.DateTimeFormatter

/**
 * OpenAI API를 사용하여 커밋 요약을 생성하는 AISummarizer 구현체
 */
class OpenAISummarizer(private val apiKey: String) : AISummarizer {
    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            jackson()
        }
    }
    
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    
    override suspend fun summarize(commits: List<Commit>): String {
        if (commits.isEmpty()) {
            return "변경 사항이 없습니다."
        }
        
        val commitsText = formatCommitsForPrompt(commits)
        val prompt = """
            아래 Git 커밋 목록을 간결하고 명확하게 요약해주세요:
            
            $commitsText
            
            다음 형식으로 요약해주세요:
            1. 주요 변경 사항 (3~5줄)
            2. 기술적 세부 사항 (필요한 경우)
        """.trimIndent()
        
        return callOpenAI(prompt)
    }
    
    override suspend fun generatePRDraft(commits: List<Commit>, title: String?): String {
        if (commits.isEmpty()) {
            return "변경 사항이 없습니다."
        }
        
        val commitsText = formatCommitsForPrompt(commits)
        val promptTitle = title ?: "이 PR의 제목을 생성해주세요."
        
        val prompt = """
            아래 Git 커밋 목록을 기반으로 풀 리퀘스트(PR) 설명을 작성해주세요:
            
            $commitsText
            
            다음 형식으로 PR 초안을 작성해주세요:
            
            # $promptTitle
            
            ## 요약
            // 주요 변경 사항에 대한 간결한 요약
            
            ## 변경 내용
            // 변경 내용을 bullet 포인트로 나열
            
            ## 테스트 방법
            // 이 변경 사항을 테스트하는 방법
        """.trimIndent()
        
        return callOpenAI(prompt)
    }
    
    override suspend fun generateChangelog(commits: List<Commit>, groupByType: Boolean): String {
        if (commits.isEmpty()) {
            return "변경 사항이 없습니다."
        }
        
        val commitsText = formatCommitsForPrompt(commits)
        val groupingInstruction = if (groupByType) {
            "변경 사항을 기능(Feature), 버그 수정(Bug Fix), 문서(Documentation), 리팩토링(Refactoring) 등으로 분류해주세요."
        } else {
            "변경 사항을 시간순으로 나열해주세요."
        }
        
        val prompt = """
            아래 Git 커밋 목록을 기반으로 변경 로그(Changelog)를 작성해주세요:
            
            $commitsText
            
            $groupingInstruction
            
            다음 형식으로 변경 로그를 작성해주세요:
            
            # 변경 로그
            // 주요 변경 사항을 Markdown 형식으로 정리
        """.trimIndent()
        
        return callOpenAI(prompt)
    }
    
    private fun formatCommitsForPrompt(commits: List<Commit>): String {
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
    
    private suspend fun callOpenAI(prompt: String): String = withContext(Dispatchers.IO) {
        try {
            val response: OpenAIResponse = httpClient.post("https://api.openai.com/v1/chat/completions") {
                header("Authorization", "Bearer $apiKey")
                contentType(ContentType.Application.Json)
                setBody(OpenAIRequest(
                    model = "gpt-4-turbo-preview",
                    messages = listOf(
                        Message(role = "system", content = "당신은 개발자를 위한 유용한 요약과 문서를 제공하는 AI 조수입니다."),
                        Message(role = "user", content = prompt)
                    ),
                    temperature = 0.7
                ))
            }.body()
            
            return@withContext response.choices.firstOrNull()?.message?.content ?: "응답을 생성할 수 없습니다."
        } catch (e: Exception) {
            return@withContext "OpenAI API 호출 중 오류 발생: ${e.message}"
        }
    }
    
    // OpenAI API 요청 및 응답 모델
    data class OpenAIRequest(
        val model: String,
        val messages: List<Message>,
        val temperature: Double = 0.7
    )
    
    data class OpenAIResponse(
        val id: String,
        val choices: List<Choice>
    )
    
    data class Message(
        val role: String,
        val content: String
    )
    
    data class Choice(
        val index: Int,
        val message: Message
    )
} 