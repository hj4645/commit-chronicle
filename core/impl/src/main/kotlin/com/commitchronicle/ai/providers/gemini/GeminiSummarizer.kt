package com.commitchronicle.ai.providers.gemini

import com.commitchronicle.ai.AIProviderConfig
import com.commitchronicle.ai.base.BaseSummarizer
import com.commitchronicle.git.model.Commit
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Google Gemini API를 사용하여 커밋 요약을 생성하는 AISummarizer 구현체
 */
class GeminiSummarizer(config: AIProviderConfig) : BaseSummarizer(config) {
    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 60_000
            connectTimeoutMillis = 30_000
            socketTimeoutMillis = 60_000
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

    override suspend fun callAIModel(prompt: String): String = withContext(Dispatchers.IO) {
        try {
            val model = config.modelName ?: "gemini-2.0-flash"

            val apiResponse =
                httpClient.post("https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=${config.apiKey}") {
                    header("Accept-Charset", "utf-8")
                    contentType(ContentType.Application.Json)
                    setBody(
                        GeminiRequest(
                            contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt)))),
                            generationConfig = GeminiGenerationConfig(temperature = 0.7, maxOutputTokens = 2000)
                        )
                    )
                }

            val responseText = apiResponse.body<String>()

            // Check HTTP status first
            if (apiResponse.status.value >= 400) {
                return@withContext "API error (${apiResponse.status.value}): $responseText"
            }
            
            try {
                val response: GeminiResponse = apiResponse.body()
                
                // Check if response has error structure (Gemini API error format)
                if (responseText.contains("\"error\":{") || responseText.startsWith("{\"error\"")) {
                    return@withContext "API response error: $responseText"
                }
                
                val text = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                return@withContext text ?: "Unable to generate response."
            } catch (e: Exception) {
                println("Response parsing error: ${e.message}")
                println("JSON input: ${responseText.take(200)}.....")
                return@withContext "Response parsing error: ${e.message}"
            }
        } catch (e: Exception) {
            println("API error details: ${e.stackTraceToString()}")
            return@withContext "Error calling Gemini API: ${e.message}"
        }
    }

    // Gemini API 요청 및 응답 모델
    @Serializable
    data class GeminiRequest(
        val contents: List<GeminiContent>,
        val generationConfig: GeminiGenerationConfig
    )

    @Serializable
    data class GeminiContent(
        val parts: List<GeminiPart>,
        val role: String? = null
    )

    @Serializable
    data class GeminiPart(
        val text: String
    )

    @Serializable
    data class GeminiGenerationConfig(
        val temperature: Double = 0.7,
        val maxOutputTokens: Int = 2000
    )

    @Serializable
    data class GeminiResponse(
        val candidates: List<GeminiCandidate> = emptyList(),
        val usageMetadata: GeminiUsageMetadata? = null,
        val modelVersion: String? = null,
        val responseId: String? = null
    )

    @Serializable
    data class GeminiCandidate(
        val content: GeminiContent? = null,
        val finishReason: String? = null,
        val index: Int? = null
    )
    
    @Serializable
    data class GeminiUsageMetadata(
        val promptTokenCount: Int? = null,
        val candidatesTokenCount: Int? = null,
        val totalTokenCount: Int? = null
    )
}