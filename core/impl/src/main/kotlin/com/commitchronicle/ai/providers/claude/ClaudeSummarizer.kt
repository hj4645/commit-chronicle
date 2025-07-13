package com.commitchronicle.ai.providers.claude

import com.commitchronicle.ai.AIProviderConfig
import com.commitchronicle.ai.base.BaseSummarizer
import com.commitchronicle.git.model.Commit
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

/**
 * Anthropic Claude API를 사용하여 커밋 요약을 생성하는 AISummarizer 구현체
 */
class ClaudeSummarizer(config: AIProviderConfig) : BaseSummarizer(config) {
    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

    override suspend fun summarize(commits: List<Commit>): String {
        if (commits.isEmpty()) {
            return "변경 사항이 없습니다."
        }
        return callAIModel(buildSummaryPrompt(commits))
    }

    override suspend fun generatePRDraft(commits: List<Commit>, title: String?): String {
        if (commits.isEmpty()) {
            return "변경 사항이 없습니다."
        }
        return callAIModel(buildPRPrompt(commits, title))
    }

    override suspend fun generateChangelog(commits: List<Commit>, groupByType: Boolean): String {
        if (commits.isEmpty()) {
            return "변경 사항이 없습니다."
        }
        return callAIModel(buildChangelogPrompt(commits, groupByType))
    }

    override suspend fun callAIModel(prompt: String): String = withContext(Dispatchers.IO) {
        try {
            println("Claude API 호출 중...")

            // 모델 설정 (기본값: claude-3-sonnet-20240229)
            val model = config.modelName ?: "claude-3-sonnet-20240229"

            // API 요청
            val apiResponse = httpClient.post("https://api.anthropic.com/v1/messages") {
                header("x-api-key", config.apiKey)
                header("anthropic-version", "2023-06-01")
                contentType(ContentType.Application.Json)
                setBody(
                    ClaudeRequest(
                        model = model,
                        system = "You are an AI assistant that provides useful summaries and documentation for developers.",
                        messages = listOf(
                            ClaudeMessage(role = "user", content = prompt)
                        ),
                        max_tokens = 2000
                    )
                )
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
    @Serializable
    data class ClaudeRequest(
        val model: String,
        val system: String,
        val messages: List<ClaudeMessage>,
        val max_tokens: Int = 2000,
        val temperature: Double = 0.7
    )

    @Serializable
    data class ClaudeMessage(
        val role: String,
        val content: String
    )

    @Serializable
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

    @Serializable
    data class ClaudeUsage(
        val input_tokens: Int? = null,
        val output_tokens: Int? = null
    )
}