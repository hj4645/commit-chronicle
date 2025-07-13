package com.commitchronicle.ai.providers.openai

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
 * OpenAI API를 사용하여 커밋 요약을 생성하는 AISummarizer 구현체
 */
class OpenAISummarizer(config: AIProviderConfig) : BaseSummarizer(config) {
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

    override suspend fun callAIModel(prompt: String): String = withContext(Dispatchers.IO) {
        try {
            println("OpenAI API 호출 중...")
            var model = config.modelName ?: "gpt-3.5-turbo"

            var apiResponse = httpClient.post("https://api.openai.com/v1/chat/completions") {
                header("Authorization", "Bearer ${config.apiKey}")
                contentType(ContentType.Application.Json)
                setBody(
                    OpenAIRequest(
                        model = model,
                        messages = listOf(
                            Message(
                                role = "system",
                                content = "You are an AI assistant that provides useful summaries and documentation for developers."
                            ),
                            Message(role = "user", content = prompt)
                        ),
                        temperature = 0.7,
                        max_tokens = 2000
                    )
                )
            }

            var responseText = apiResponse.body<String>()
            println("API 응답: $responseText")

            if (responseText.contains("error") &&
                (responseText.contains("model_not_found") ||
                        responseText.contains("does not exist or you do not have access to it"))
            ) {
                println("${model} 모델에 접근할 수 없습니다. gpt-3.5-turbo 모델로 시도합니다.")
                model = "gpt-3.5-turbo"

                apiResponse = httpClient.post("https://api.openai.com/v1/chat/completions") {
                    header("Authorization", "Bearer ${config.apiKey}")
                    contentType(ContentType.Application.Json)
                    setBody(
                        OpenAIRequest(
                            model = model,
                            messages = listOf(
                                Message(
                                    role = "system",
                                    content = "You are an AI assistant that provides useful summaries and documentation for developers."
                                ),
                                Message(role = "user", content = prompt)
                            ),
                            temperature = 0.7,
                            max_tokens = 2000
                        )
                    )
                }

                responseText = apiResponse.body<String>()
                println("GPT-3.5 API 응답: $responseText")
            }

            if (!responseText.contains("error")) {
                try {
                    val response: OpenAIResponse = apiResponse.body()
                    return@withContext response.getFirstChoiceContent()
                } catch (e: Exception) {
                    println("응답 파싱 오류: ${e.message}")
                    return@withContext "응답 파싱 오류: ${e.message}"
                }
            } else {
                return@withContext "API 응답 오류: $responseText"
            }
        } catch (e: Exception) {
            println("API 오류 상세: ${e.stackTraceToString()}")
            return@withContext "OpenAI API 호출 중 오류 발생: ${e.message}"
        }
    }

    @Serializable
    data class OpenAIRequest(
        val model: String,
        val messages: List<Message>,
        val temperature: Double = 0.7,
        val max_tokens: Int = 2000
    )

    @Serializable
    data class OpenAIResponse(
        val id: String? = null,
        @SerialName("object") val objectType: String? = null,
        val created: Long? = null,
        val model: String? = null,
        val choices: List<Choice>? = emptyList(),
        val usage: Usage? = null
    ) {
        fun getFirstChoiceContent(): String {
            return choices?.firstOrNull()?.message?.content ?: "응답을 생성할 수 없습니다."
        }
    }

    @Serializable
    data class Message(
        val role: String,
        val content: String
    )

    @Serializable
    data class Choice(
        val index: Int = 0,
        val message: Message? = null,
        @SerialName("finish_reason") val finishReason: String? = null
    )

    @Serializable
    data class Usage(
        @SerialName("prompt_tokens") val promptTokens: Int? = null,
        @SerialName("completion_tokens") val completionTokens: Int? = null,
        @SerialName("total_tokens") val totalTokens: Int? = null
    )
}