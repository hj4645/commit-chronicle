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
            json(kotlinx.serialization.json.Json {
                ignoreUnknownKeys = true
            })
        }
    }

    override suspend fun summarize(commits: List<Commit>): String {
        if (commits.isEmpty()) {
            return "No changes found."
        }
        return callAIModel(buildSummaryPrompt(commits))
    }

    override suspend fun generatePRDraft(commits: List<Commit>, title: String?): String {
        if (commits.isEmpty()) {
            return "No changes found."
        }
        return callAIModel(buildPRPrompt(commits, title))
    }

    override suspend fun generateChangelog(commits: List<Commit>, groupByType: Boolean): String {
        if (commits.isEmpty()) {
            return "No changes found."
        }
        return callAIModel(buildChangelogPrompt(commits, groupByType))
    }

    override suspend fun callAIModel(prompt: String): String = withContext(Dispatchers.IO) {
        try {
            println("Calling Claude API...")

            // Model configuration (default: claude-3-5-sonnet-20241022)
            val model = config.modelName ?: "claude-3-5-sonnet-20241022"

            // API request
            val apiResponse = httpClient.post("https://api.anthropic.com/v1/messages") {
                header("x-api-key", config.apiKey)
                header("anthropic-version", "2023-06-01")
                header("Accept-Charset", "utf-8")
                contentType(ContentType.Application.Json)
                setBody(
                    ClaudeRequest(
                        model = model,
                        system = "You are an AI assistant that provides useful summaries and documentation for developers.",
                        messages = listOf(
                            ClaudeMessage(role = "user", content = prompt)
                        ),
                        max_tokens = 2000,
                        temperature = 0.7
                    )
                )
            }

            // Get response text for parsing
            val responseText = apiResponse.body<String>()

            // Extract content from response
            try {
                val response: ClaudeResponse = apiResponse.body()
                
                // Check if response contains error
                if (responseText.contains("\"type\":\"error\"")) {
                    return@withContext "API response error: $responseText"
                }
                
                val contentText = response.content.firstOrNull { it.type == "text" }?.text
                return@withContext contentText ?: "Unable to generate response."
            } catch (e: Exception) {
                println("Response parsing error: ${e.message}")
                println("JSON input: ${responseText.take(200)}.....")
                return@withContext "Response parsing error: ${e.message}"
            }
        } catch (e: Exception) {
            println("API error details: ${e.stackTraceToString()}")
            return@withContext "Error calling Claude API: ${e.message}"
        }
    }

    // Claude API 요청 및 응답 모델
    @Serializable
    data class ClaudeRequest(
        val model: String,
        val system: String,
        val messages: List<ClaudeMessage>,
        val max_tokens: Int,
        val temperature: Double
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
        val content: List<ClaudeContent> = emptyList(),
        val model: String? = null,
        val stop_reason: String? = null,
        val stop_sequence: String? = null,
        val usage: ClaudeUsage? = null
    )

    @Serializable
    data class ClaudeContent(
        val type: String? = null,
        val text: String? = null
    )

    @Serializable
    data class ClaudeUsage(
        val input_tokens: Int? = null,
        val output_tokens: Int? = null,
        val cache_creation_input_tokens: Int? = null,
        val cache_read_input_tokens: Int? = null
    )
}