package com.commitchronicle.ai

import com.commitchronicle.model.Commit
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.modelcontextprotocol.kotlin.sdk.Implementation
import io.modelcontextprotocol.kotlin.sdk.client.Client
import io.modelcontextprotocol.kotlin.sdk.client.SseClientTransport
import io.modelcontextprotocol.kotlin.sdk.CreateMessageRequest
import io.modelcontextprotocol.kotlin.sdk.CompleteResult
import io.modelcontextprotocol.kotlin.sdk.SamplingMessage
import java.time.format.DateTimeFormatter

/**
 * MCP (Model Context Protocol)를 사용하여 커밋 요약을 생성하는 AISummarizer 구현체
 * JDK 17 이상에서만 동작합니다.
 */
class McpSummarizer(private val apiKey: String) : AISummarizer {
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

        return callMcpModel(prompt)
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

        return callMcpModel(prompt)
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

        return callMcpModel(prompt)
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

    private suspend fun callMcpModel(prompt: String): String {
        return try {
            // MCP 클라이언트 생성
            val client = Client(
                clientInfo = Implementation(
                    name = "CommitChronicle",
                    version = "1.0.0"
                )
            )

            // HTTP 클라이언트 생성
            val httpClient = HttpClient(CIO)

            // SSE 트랜스포트 설정
            val transport = SseClientTransport(
                client = httpClient,
                urlString = "https://api.openai.com/v1/chat/completions",
                requestBuilder = {
                    method = HttpMethod.Post
                    headers {
                        append("X-API-Key", apiKey)  // MCP 인증 방식
                        append("Content-Type", "application/json")
                    }
                }
            )

            // 커넥션 생성 및 요청 실행
            val connection = client.connect(transport)
            try {
                // 2. 요청 생성
                val request = ClientRequest.Builder()
                    .model("mcp-gpt-4")
                    .messages(
                        listOf(
                            Message(
                                role = Message.Role.SYSTEM,
                                content = "당신은 개발자를 위한 유용한 요약과 문서를 제공하는 AI 조수입니다."
                            ),
                            Message(
                                role = Message.Role.USER,
                                content = prompt
                            )
                        )
                    )
                    .temperature(0.7)
                    .maxTokens(2000)
                    .build()

                // 3. 요청 실행
                val response = connection.executeRequest(request)

                // 4. 응답 처리
                response.getResult<ChatCompletionResult>().choices.first().message.content
            } finally {
                connection.close() // 명시적 연결 종료
            }
        } catch (e: ClientRequestException) {
            // 4xx 에러 처리
            "API 호출 실패(클라이언트 오류): ${e.response.status}, ${e.localizedMessage}"
        } catch (e: ServerResponseException) {
            // 5xx 에러 처리
            "API 호출 실패(서버 오류): ${e.response.status}, ${e.localizedMessage}"
        } catch (e: Exception) {
            // 기타 예외 처리
            "API 호출 실패: ${e.localizedMessage}"
        }
    }
}