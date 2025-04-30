package com.commitchronicle.ai.providers.perplexity

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
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Perplexity API를 사용하여 커밋 요약을 생성하는 AISummarizer 구현체
 */
class PerplexitySummarizer(config: AIProviderConfig) : BaseSummarizer(config) {
    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

    override suspend fun summarize(commits: List<Commit>): String {
        if (commits.isEmpty()) return "변경 사항이 없습니다."
        return callAIModel(buildSummaryPrompt(commits))
    }

    override suspend fun generatePRDraft(commits: List<Commit>, title: String?): String {
        if (commits.isEmpty()) return "변경 사항이 없습니다."
        return callAIModel(buildPRPrompt(commits, title))
    }

    override suspend fun generateChangelog(commits: List<Commit>, groupByType: Boolean): String {
        if (commits.isEmpty()) return "변경 사항이 없습니다."
        return callAIModel(buildChangelogPrompt(commits, groupByType))
    }

    override protected suspend fun callAIModel(prompt: String): String = withContext(Dispatchers.IO) {
        try {
            println("Perplexity API 호출 중...")
            val model = config.modelName ?: "sonar"

            val apiResponse = httpClient.post("https://api.perplexity.ai/chat/completions") {
                header("Authorization", "Bearer ${config.apiKey}")
                contentType(ContentType.Application.Json)
                setBody(
                    PerplexityRequest(
                        model = model,
                        messages = listOf(
                            PerplexityMessage(
                                role = "system",
                                content = "You are an AI assistant that provides useful summaries and documentation for developers."
                            ),
                            PerplexityMessage(role = "user", content = prompt)
                        ),
                        temperature = 0.7,
                        max_tokens = 2000
                    )
                )
            }

            val responseText = apiResponse.body<String>()
            println("API 응답: $responseText")

            if (!responseText.contains("error")) {
                try {
                    val response: PerplexityResponse = apiResponse.body()
                    return@withContext response.choices?.firstOrNull()?.message?.content ?: "응답을 생성할 수 없습니다."
                } catch (e: Exception) {
                    println("응답 파싱 오류: ${e.message}")
                    return@withContext "응답 파싱 오류: ${e.message}"
                }
            } else {
                return@withContext "API 응답 오류: $responseText"
            }
        } catch (e: Exception) {
            println("API 오류 상세: ${e.stackTraceToString()}")
            return@withContext "Perplexity API 호출 중 오류 발생: ${e.message}"
        }
    }

    // Perplexity API 요청 및 응답 모델
    @Serializable
    data class PerplexityRequest(
        val model: String,
        val messages: List<PerplexityMessage>,
        val temperature: Double = 0.7,
        val max_tokens: Int = 2000
    )

    @Serializable
    data class PerplexityMessage(
        val role: String,
        val content: String
    )

    @Serializable
    data class PerplexityResponse(
        val id: String? = null,
        val model: String? = null,
        val created: Long? = null,
        val choices: List<PerplexityChoice>? = null,
        val usage: PerplexityUsage? = null
    )

    @Serializable
    data class PerplexityChoice(
        val index: Int? = 0,
        val message: PerplexityMessage? = null,
        @SerialName("finish_reason") val finishReason: String? = null
    )

    @Serializable
    data class PerplexityUsage(
        @SerialName("prompt_tokens") val promptTokens: Int? = null,
        @SerialName("completion_tokens") val completionTokens: Int? = null,
        @SerialName("total_tokens") val totalTokens: Int? = null
    )
}