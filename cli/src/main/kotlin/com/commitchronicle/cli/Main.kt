package com.commitchronicle.cli

import com.commitchronicle.ai.AIProviderType
import com.commitchronicle.ai.AISummarizerFactory
import com.commitchronicle.ai.DefaultAIProviderConfig
import com.commitchronicle.git.GitAnalyzerFactory
import com.commitchronicle.template.TemplateEngineFactory
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.path
import kotlinx.coroutines.runBlocking
import java.io.File
import java.nio.file.Path
import kotlin.io.path.absolutePathString

// AIProvider 옵션 그룹
class AIOptions : OptionGroup() {
    val apiKey by option("-k", "--key", help = "OpenAI API 키")
        .required()
    
    val providerType by option("-p", "--provider", help = "AI 프로바이더 (openai, claude, perplexity)")
        .choice("openai", "claude", "perplexity", ignoreCase = true)
        .default("openai")
        
    val model by option("-m", "--model", help = "사용할 모델 (기본: 자동 선택)")
}

// Git 옵션 그룹
class GitOptions : OptionGroup() {
    val path by option("--path", help = "Git 저장소 경로")
        .path(mustExist = true, canBeFile = false)
        .required()
        
    val branch by option("-b", "--branch", help = "분석할 브랜치 (기본: 현재 브랜치)")
        
    val days by option("-d", "--days", help = "분석할 일수 (기본: 7)")
        .int()
        .default(7)
        
    val limit by option("-l", "--limit", help = "분석할 최대 커밋 수 (기본: 50)")
        .int()
        .default(50)
        
    val fromCommit by option("--from", help = "시작 커밋 (커밋 ID)")
    
    val toCommit by option("--to", help = "종료 커밋 (기본: HEAD)")
        .default("HEAD")
}

// 메인 커맨드
class CommitChronicle : CliktCommand(
    help = "CommitChronicle - AI 기반 Git 커밋 분석 도구",
    printHelpOnEmptyArgs = true,
    invokeWithoutSubcommand = true
) {
    override fun run() {
        if (currentContext.invokedSubcommand == null) {
            echo("""
                CommitChronicle - Git 커밋 분석 도구
                
                사용 가능한 명령어를 확인하려면 --help 옵션을 사용하세요.
            """.trimIndent())
        }
    }
}

// 요약 커맨드
class Summarize : CliktCommand(
    name = "summarize",
    help = "Git 커밋 요약 생성",
    printHelpOnEmptyArgs = true
) {
    // 옵션 정의
    private val gitOptions by GitOptions()
    private val aiOptions by AIOptions()
    
    private val outputFile by option("-o", "--output", help = "출력 파일 경로")
    
    override fun run() = runBlocking {
        try {
            // Git 저장소 초기화
            val gitAnalyzer = GitAnalyzerFactory.create(gitOptions.path.absolutePathString())
            
            // AI 요약기 설정
            val aiProviderConfig = DefaultAIProviderConfig(
                apiKey = aiOptions.apiKey,
                modelName = aiOptions.model
            )
            val aiProvider = when (aiOptions.providerType.lowercase()) {
                "claude" -> AIProviderType.CLAUDE
                "perplexity" -> AIProviderType.PERPLEXITY
                else -> AIProviderType.OPENAI
            }
            val aiSummarizer = AISummarizerFactory.create(aiProviderConfig, aiProvider)
            
            // 커밋 가져오기
            val commits = if (gitOptions.fromCommit != null) {
                gitAnalyzer.getCommitRange(gitOptions.fromCommit!!, gitOptions.toCommit)
            } else {
                gitAnalyzer.getCommits(gitOptions.days).take(gitOptions.limit)
            }
            
            if (commits.isEmpty()) {
                echo("해당 기간/범위에 커밋이 없습니다.")
                return@runBlocking
            }
            
            echo("${commits.size}개의 커밋을 분석 중...")
            val summary = aiSummarizer.summarize(commits)
            
            if (outputFile != null) {
                File(outputFile!!).writeText(summary)
                echo("요약이 '$outputFile'에 저장되었습니다.")
            } else {
                echo("\n=== 커밋 요약 ===\n")
                echo(summary)
            }
        } catch (e: Exception) {
            echo("오류 발생: ${e.message}", err = true)
            e.printStackTrace()
        }
    }
}

// PR 초안 생성 커맨드
class GeneratePR : CliktCommand(
    name = "pr",
    help = "Pull Request 초안 생성",
    printHelpOnEmptyArgs = true
) {
    // 옵션 정의
    private val gitOptions by GitOptions()
    private val aiOptions by AIOptions()
    
    private val title by option("-t", "--title", help = "PR 제목 (기본: 자동 생성)")
    private val outputFile by option("-o", "--output", help = "출력 파일 경로")
    private val templatePath by option("--template", help = "템플릿 파일 경로").path(mustExist = true, canBeFile = true)
    
    override fun run() = runBlocking {
        try {
            // Git 저장소 초기화
            val gitAnalyzer = GitAnalyzerFactory.create(gitOptions.path.absolutePathString())
            
            // AI 요약기 설정
            val aiProviderConfig = DefaultAIProviderConfig(
                apiKey = aiOptions.apiKey,
                modelName = aiOptions.model
            )
            val aiProvider = when (aiOptions.providerType.lowercase()) {
                "claude" -> AIProviderType.CLAUDE
                "perplexity" -> AIProviderType.PERPLEXITY
                else -> AIProviderType.OPENAI
            }
            val aiSummarizer = AISummarizerFactory.create(aiProviderConfig, aiProvider)
            
            // 커밋 가져오기
            val commits = if (gitOptions.fromCommit != null) {
                gitAnalyzer.getCommitRange(gitOptions.fromCommit!!, gitOptions.toCommit)
            } else {
                gitAnalyzer.getCommits(gitOptions.days).take(gitOptions.limit)
            }
            
            if (commits.isEmpty()) {
                echo("해당 기간/범위에 커밋이 없습니다.")
                return@runBlocking
            }
            
            echo("${commits.size}개의 커밋을 분석 중...")
            val prDraft = if (templatePath != null) {
                val templateEngine = TemplateEngineFactory.create()
                val summary = aiSummarizer.summarize(commits)
                
                val data = mapOf(
                    "commits" to commits,
                    "summary" to summary,
                    "title" to (title ?: "PR 제목")
                )
                
                templateEngine.render(templatePath!!.toString(), data)
            } else {
                aiSummarizer.generatePRDraft(commits, title)
            }
            
            if (outputFile != null) {
                File(outputFile!!).writeText(prDraft)
                echo("PR 초안이 '$outputFile'에 저장되었습니다.")
            } else {
                echo("\n=== PR 초안 ===\n")
                echo(prDraft)
            }
        } catch (e: Exception) {
            echo("오류 발생: ${e.message}", err = true)
            e.printStackTrace()
        }
    }
}

// 변경 로그 생성 커맨드
class GenerateChangelog : CliktCommand(
    name = "changelog",
    help = "변경 로그 생성",
    printHelpOnEmptyArgs = true
) {
    // 옵션 정의
    private val gitOptions by GitOptions()
    private val aiOptions by AIOptions()
    
    private val groupByType by option("--group", help = "파일 타입별로 그룹화").flag()
    private val outputFile by option("-o", "--output", help = "출력 파일 경로")
    private val templatePath by option("--template", help = "템플릿 파일 경로").path(mustExist = true, canBeFile = true)
    
    override fun run() = runBlocking {
        try {
            // Git 저장소 초기화
            val gitAnalyzer = GitAnalyzerFactory.create(gitOptions.path.absolutePathString())
            
            // AI 요약기 설정
            val aiProviderConfig = DefaultAIProviderConfig(
                apiKey = aiOptions.apiKey,
                modelName = aiOptions.model
            )
            val aiProvider = when (aiOptions.providerType.lowercase()) {
                "claude" -> AIProviderType.CLAUDE
                "perplexity" -> AIProviderType.PERPLEXITY
                else -> AIProviderType.OPENAI
            }
            val aiSummarizer = AISummarizerFactory.create(aiProviderConfig, aiProvider)
            
            // 커밋 가져오기
            val commits = if (gitOptions.fromCommit != null) {
                gitAnalyzer.getCommitRange(gitOptions.fromCommit!!, gitOptions.toCommit)
            } else {
                gitAnalyzer.getCommits(gitOptions.days).take(gitOptions.limit)
            }
            
            if (commits.isEmpty()) {
                echo("해당 기간/범위에 커밋이 없습니다.")
                return@runBlocking
            }

            echo("${commits.size}개의 커밋을 분석 중...")
            val changelog = if (templatePath != null) {
                val templateEngine = TemplateEngineFactory.create()
                val summary = aiSummarizer.summarize(commits)

                val data = mapOf(
                    "commits" to commits,
                    "summary" to summary,
                    "groupByType" to groupByType
                )

                templateEngine.render(templatePath!!.toString(), data)
            } else {
                aiSummarizer.generateChangelog(commits, groupByType)
            }

            if (outputFile != null) {
                File(outputFile!!).writeText(changelog)
                echo("변경 로그가 '$outputFile'에 저장되었습니다.")
            } else {
                echo("\n=== 변경 로그 ===\n")
                echo(changelog)
            }
        } catch (e: Exception) {
            echo("오류 발생: ${e.message}", err = true)
            e.printStackTrace()
        }
    }
}

// 메인 함수
fun main(args: Array<String>) {
    try {
        val command = CommitChronicle()
            .subcommands(Summarize(), GeneratePR(), GenerateChangelog())
        command.main(args)
    } catch (e: Exception) {
        System.err.println("오류 발생: ${e.message}")
        e.printStackTrace()
    }
} 
