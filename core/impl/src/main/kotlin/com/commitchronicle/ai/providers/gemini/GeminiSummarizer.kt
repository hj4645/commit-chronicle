package com.commitchronicle.ai.providers.gemini

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
 * Google Gemini API를 사용하여 커밋 요약을 생성하는 AISummarizer 구현체
 */
class GeminiSummarizer(config: AIProviderConfig) : BaseSummarizer(config) {
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
            println("Gemini API 호출 중...")
            val model = config.modelName ?: "gemini-pro"

            val apiResponse =
                httpClient.post("https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent") {
                    header("Authorization", "Bearer ${config.apiKey}")
                    contentType(ContentType.Application.Json)
                    setBody(
                        GeminiRequest(
                            contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt)))),
                            generationConfig = GeminiGenerationConfig(temperature = 0.7, maxOutputTokens = 2000)
                        )
                    )
                }

            val responseText = apiResponse.body<String>()
            println("API 응답: $responseText")

            if (!responseText.contains("error")) {
                try {
                    val response: GeminiResponse = apiResponse.body()
                    return@withContext response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                        ?: "응답을 생성할 수 없습니다."
                } catch (e: Exception) {
                    println("응답 파싱 오류: ${e.message}")
                    return@withContext "응답 파싱 오류: ${e.message}"
                }
            } else {
                return@withContext "API 응답 오류: $responseText"
            }
        } catch (e: Exception) {
            println("API 오류 상세: ${e.stackTraceToString()}")
            return@withContext "Gemini API 호출 중 오류 발생: ${e.message}"
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
        val parts: List<GeminiPart>
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
        val candidates: List<GeminiCandidate> = emptyList()
    )

    @Serializable
    data class GeminiCandidate(
        val content: GeminiContent? = null
    )
}