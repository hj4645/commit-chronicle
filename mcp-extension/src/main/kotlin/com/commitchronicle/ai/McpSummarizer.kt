package com.commitchronicle.ai

import com.commitchronicle.model.Commit
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import java.io.File
import java.net.ServerSocket
import java.nio.file.Files
import java.nio.file.Paths
import java.time.format.DateTimeFormatter
import kotlin.concurrent.thread

/**
 * GitHub 공식 MCP 서버를 사용하여 커밋 요약을 생성하는 AISummarizer 구현체
 * GitHub MCP 서버를 로컬에서 실행하고 이를 통해 Git 관련 작업을 수행합니다.
 * 
 * @param apiKey GitHub 또는 OpenAI에서 발급받은 API 키
 * @param mcpServerPath GitHub MCP 서버 바이너리 경로 (null인 경우 자동 다운로드)
 * @param mcpServerPort GitHub MCP 서버가 실행될 포트 (기본값: 랜덤 사용 가능 포트)
 */
class McpSummarizer(
    private val apiKey: String,
    private val mcpServerPath: String? = null,
    private val mcpServerPort: Int = findAvailablePort()
) : AISummarizer {
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    private val json = Json { ignoreUnknownKeys = true }
    
    private var serverProcess: Process? = null
    private val mcpServerEndpoint = "http://localhost:$mcpServerPort"
    private val httpClient = HttpClient(CIO) {
        install(HttpTimeout) {
            requestTimeoutMillis = 120_000L
            connectTimeoutMillis = 15_000L
            socketTimeoutMillis = 120_000L
        }
        install(ContentNegotiation) {
            json(json)
        }
        install(Logging) {
            level = LogLevel.INFO
        }
    }
    
    init {
        startMcpServer()
        
        // 종료 시 서버 프로세스 정리
        Runtime.getRuntime().addShutdownHook(Thread {
            stopMcpServer()
        })
    }

    override suspend fun summarize(commits: List<Commit>): String = withContext(Dispatchers.IO) {
        if (commits.isEmpty()) {
            return@withContext "변경 사항이 없습니다."
        }

        val commitsText = formatCommitsForPrompt(commits)
        val prompt = """
            아래 Git 커밋 목록을 간결하고 명확하게 요약해주세요:
            
            $commitsText
            
            다음 형식으로 요약해주세요:
            1. 주요 변경 사항 (3~5줄)
            2. 기술적 세부 사항 (필요한 경우)
        """.trimIndent()

        callMcpFunction("completion", prompt, "summarize")
    }

    override suspend fun generatePRDraft(commits: List<Commit>, title: String?): String = withContext(Dispatchers.IO) {
        if (commits.isEmpty()) {
            return@withContext "변경 사항이 없습니다."
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

        callMcpFunction("completion", prompt, "generate_pr_draft")
    }

    override suspend fun generateChangelog(commits: List<Commit>, groupByType: Boolean): String = withContext(Dispatchers.IO) {
        if (commits.isEmpty()) {
            return@withContext "변경 사항이 없습니다."
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

        callMcpFunction("completion", prompt, "generate_changelog")
    }
    
    /**
     * MCP 서버를 통해 텍스트 완성 요청을 수행합니다.
     *
     * @param functionType 함수 유형 ("completion" 또는 다른 MCP 함수)
     * @param prompt 프롬프트 텍스트
     * @param operation 작업 유형 (로깅용)
     * @return 생성된 텍스트
     */
    private suspend fun callMcpFunction(functionType: String, prompt: String, operation: String): String {
        return try {
            val response = httpClient.post("$mcpServerEndpoint/api/v1/chat") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("model", JsonPrimitive("gpt-4"))
                    put("messages", buildJsonArray {
                        add(buildJsonObject {
                            put("role", JsonPrimitive("system"))
                            put("content", JsonPrimitive("당신은 개발자를 위한 유용한 요약과 문서를 제공하는 AI 조수입니다."))
                        })
                        add(buildJsonObject {
                            put("role", JsonPrimitive("user"))
                            put("content", JsonPrimitive(prompt))
                        })
                    })
                    put("temperature", JsonPrimitive(0.7))
                    put("max_tokens", JsonPrimitive(1500))
                })
            }
            
            // 응답 처리
            val responseText = response.bodyAsText()
            val jsonResponse = json.parseToJsonElement(responseText).jsonObject
            
            if (jsonResponse.containsKey("error")) {
                val error = jsonResponse["error"]?.jsonObject
                val message = error?.get("message")?.jsonPrimitive?.content ?: "알 수 없는 오류"
                throw RuntimeException("MCP 서버 오류: $message")
            }
            
            // choices[0].message.content에서 실제 응답 추출
            val choices = jsonResponse["choices"]?.jsonArray
            if (choices == null || choices.isEmpty()) {
                throw RuntimeException("MCP 서버 응답에 choices가 없습니다.")
            }
            
            val message = choices[0].jsonObject["message"]?.jsonObject
            val content = message?.get("content")?.jsonPrimitive?.content
            
            content ?: throw RuntimeException("MCP 서버 응답에 content가 없습니다.")
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
    
    /**
     * MCP 서버를 통해 Pull Request를 생성합니다.
     * GitHub MCP 서버의 create_pull_request 함수를 활용합니다.
     * 
     * @param owner 저장소 소유자
     * @param repo 저장소 이름
     * @param title PR 제목
     * @param body PR 내용
     * @param head 변경 사항이 있는 브랜치
     * @param base 대상 브랜치 (일반적으로 main 또는 master)
     * @return PR 생성 결과
     */
    suspend fun createPullRequest(
        owner: String,
        repo: String,
        title: String,
        body: String,
        head: String,
        base: String
    ): String = withContext(Dispatchers.IO) {
        try {
            val response = httpClient.post("$mcpServerEndpoint/api/v1/functions/create_pull_request") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("owner", JsonPrimitive(owner))
                    put("repo", JsonPrimitive(repo))
                    put("title", JsonPrimitive(title))
                    put("body", JsonPrimitive(body))
                    put("head", JsonPrimitive(head))
                    put("base", JsonPrimitive(base))
                })
            }
            
            return@withContext response.bodyAsText()
        } catch (e: Exception) {
            "PR 생성 중 오류 발생: ${e.localizedMessage}"
        }
    }
    
    /**
     * MCP 서버를 통해 저장소의 커밋 목록을 가져옵니다.
     * GitHub MCP 서버의 list_commits 함수를 활용합니다.
     * 
     * @param owner 저장소 소유자
     * @param repo 저장소 이름
     * @param branch 브랜치 이름 (옵션)
     * @param path 특정 파일 경로 (옵션)
     * @return 커밋 목록 JSON 문자열
     */
    suspend fun listCommits(
        owner: String,
        repo: String,
        branch: String? = null,
        path: String? = null
    ): String = withContext(Dispatchers.IO) {
        try {
            val response = httpClient.post("$mcpServerEndpoint/api/v1/functions/list_commits") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("owner", JsonPrimitive(owner))
                    put("repo", JsonPrimitive(repo))
                    if (branch != null) put("sha", JsonPrimitive(branch))
                    if (path != null) put("path", JsonPrimitive(path))
                })
            }
            
            return@withContext response.bodyAsText()
        } catch (e: Exception) {
            "커밋 목록 조회 중 오류 발생: ${e.localizedMessage}"
        }
    }
    
    /**
     * MCP 서버를 통해 커밋 세부 정보를 가져옵니다.
     * GitHub MCP 서버의 get_commit 함수를 활용합니다.
     * 
     * @param owner 저장소 소유자
     * @param repo 저장소 이름
     * @param sha 커밋 SHA
     * @return 커밋 세부 정보 JSON 문자열
     */
    suspend fun getCommit(
        owner: String,
        repo: String,
        sha: String
    ): String = withContext(Dispatchers.IO) {
        try {
            val response = httpClient.post("$mcpServerEndpoint/api/v1/functions/get_commit") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("owner", JsonPrimitive(owner))
                    put("repo", JsonPrimitive(repo))
                    put("sha", JsonPrimitive(sha))
                })
            }
            
            return@withContext response.bodyAsText()
        } catch (e: Exception) {
            "커밋 정보 조회 중 오류 발생: ${e.localizedMessage}"
        }
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
    
    /**
     * GitHub MCP 서버를 시작합니다.
     * 서버 바이너리가 없는 경우 자동으로 다운로드합니다.
     */
    private fun startMcpServer() {
        try {
            val serverExePath = mcpServerPath ?: downloadMcpServer()
            
            val processBuilder = ProcessBuilder(
                serverExePath,
                "--port", mcpServerPort.toString(),
                "--api-key", apiKey
            )
            processBuilder.redirectErrorStream(true)
            
            serverProcess = processBuilder.start()
            
            // 로그 출력을 위한 별도 스레드
            thread {
                serverProcess?.inputStream?.bufferedReader()?.use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        println("MCP Server: $line")
                    }
                }
            }
            
            // 서버 시작 대기
            waitForServerStart()
            
            println("GitHub MCP 서버가 http://localhost:$mcpServerPort 에서 실행 중입니다.")
        } catch (e: Exception) {
            println("MCP 서버 시작 중 오류 발생: ${e.message}")
            throw e  // 예외를 전파하여 AISummarizerFactory에서 폴백 처리되도록 함
        }
    }
    
    /**
     * GitHub MCP 서버를 중지합니다.
     */
    private fun stopMcpServer() {
        serverProcess?.let { process ->
            try {
                process.destroy()
                if (process.isAlive) {
                    process.destroyForcibly()
                }
                println("GitHub MCP 서버가 중지되었습니다.")
            } catch (e: Exception) {
                println("MCP 서버 중지 중 오류 발생: ${e.message}")
            }
        }
    }
    
    /**
     * GitHub MCP 서버 바이너리를 다운로드합니다.
     * 
     * @return 다운로드된 서버 바이너리 경로
     */
    private fun downloadMcpServer(): String {
        val osName = System.getProperty("os.name").lowercase()
        val arch = System.getProperty("os.arch").lowercase()
        
        val isMac = osName.contains("mac")
        val isWindows = osName.contains("windows")
        val is64bit = arch.contains("64")
        
        val releaseTag = "v0.2.0" // 최신 버전으로 업데이트 필요
        val fileNameBase = "github-mcp-server"
        
        val fileName = when {
            isWindows -> "$fileNameBase-$releaseTag-windows-amd64.exe"
            isMac && arch.contains("aarch64") -> "$fileNameBase-$releaseTag-darwin-arm64"
            isMac -> "$fileNameBase-$releaseTag-darwin-amd64"
            else -> "$fileNameBase-$releaseTag-linux-amd64"
        }
        
        val downloadUrl = "https://github.com/github/github-mcp-server/releases/download/$releaseTag/$fileName"
        val targetDir = Paths.get(System.getProperty("user.home"), ".commitchronicle", "bin")
        val targetPath = targetDir.resolve(fileName)
        
        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir)
        }
        
        if (!Files.exists(targetPath)) {
            println("GitHub MCP 서버 다운로드 중: $downloadUrl")
            
            httpClient.use { client ->
                runBlocking {
                    val response = client.get(downloadUrl)
                    if (response.status.isSuccess()) {
                        Files.write(targetPath, response.readBytes())
                        
                        // 실행 권한 설정 (Windows 외)
                        if (!isWindows) {
                            val file = targetPath.toFile()
                            file.setExecutable(true)
                        }
                        
                        println("GitHub MCP 서버 다운로드 완료: $targetPath")
                    } else {
                        throw RuntimeException("GitHub MCP 서버 다운로드 실패: ${response.status}")
                    }
                }
            }
        }
        
        return targetPath.toString()
    }
    
    /**
     * MCP 서버가 시작되기를 기다립니다.
     */
    private fun waitForServerStart() {
        // 최대 30초 동안 시도
        for (i in 1..30) {
            try {
                val connection = java.net.URL("http://localhost:$mcpServerPort/api/v1/health").openConnection()
                connection.connectTimeout = 1000
                connection.readTimeout = 1000
                connection.connect()
                
                // 응답이 있으면 서버가 시작된 것으로 간주
                return
            } catch (e: Exception) {
                Thread.sleep(1000)
            }
        }
        
        throw RuntimeException("MCP 서버 시작 타임아웃")
    }
    
    companion object {
        /**
         * 사용 가능한 랜덤 포트를 찾습니다.
         * 
         * @return 사용 가능한 포트 번호
         */
        private fun findAvailablePort(): Int {
            return ServerSocket(0).use { it.localPort }
        }
    }
}