package com.commitchronicle.ai

import com.commitchronicle.model.Commit
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.jackson.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Anthropic Claude API를 사용하여 커밋 요약을 생성하는 AISummarizer 구현체
 */
class ClaudeSummarizer(config: AIProviderConfig) : BaseSummarizer(config) {
    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            jackson()
        }
    }
    
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
        
        return callAIModel(prompt)
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
        
        return callAIModel(prompt)
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
        
        return callAIModel(prompt)
    }
    
    override protected suspend fun callAIModel(prompt: String): String = withContext(Dispatchers.IO) {
        try {
            println("Claude API 호출 중...")
            
            // 모델 설정 (기본값: claude-3-sonnet-20240229)
            val model = config.modelName ?: "claude-3-sonnet-20240229"
            
            // API 요청
            val apiResponse = httpClient.post("https://api.anthropic.com/v1/messages") {
                header("x-api-key", config.apiKey)
                header("anthropic-version", "2023-06-01")
                contentType(ContentType.Application.Json)
                setBody(ClaudeRequest(
                    model = model,
                    system = "당신은 개발자를 위한 유용한 요약과 문서를 제공하는 AI 조수입니다.",
                    messages = listOf(
                        ClaudeMessage(role = "user", content = prompt)
                    ),
                    max_tokens = 2000
                ))
            }
            
            // API 응답 디버깅
            val responseText = apiResponse.body<String>()
            println("API 응답: $responseText")
            
            // 응답에서 필요한 정보 추출
            if (!responseText.contains("error")) {
                try {
                    val response: ClaudeResponse = apiResponse.body()
                    return@withContext response.content ?: "응답을 생성할 수 없습니다."
                } catch (e: Exception) {
                    println("응답 파싱 오류: ${e.message}")
                    return@withContext "응답 파싱 오류: ${e.message}"
                }
            } else {
                return@withContext "API 응답 오류: $responseText"
            }
        } catch (e: Exception) {
            println("API 오류 상세: ${e.stackTraceToString()}")
            return@withContext "Claude API 호출 중 오류 발생: ${e.message}"
        }
    }
    
    // Claude API 요청 및 응답 모델
    data class ClaudeRequest(
        val model: String,
        val system: String,
        val messages: List<ClaudeMessage>,
        val max_tokens: Int = 2000,
        val temperature: Double = 0.7
    )
    
    data class ClaudeMessage(
        val role: String,
        val content: String
    )
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class ClaudeResponse(
        val id: String? = null,
        val type: String? = null,
        val role: String? = null,
        val content: String? = null,
        val model: String? = null,
        val stop_reason: String? = null,
        val stop_sequence: String? = null,
        val usage: ClaudeUsage? = null
    )
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class ClaudeUsage(
        val input_tokens: Int? = null,
        val output_tokens: Int? = null
    )
} 