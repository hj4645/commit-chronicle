package com.commitchronicle.cli

import com.commitchronicle.core.ai.AISummarizerFactory
import com.commitchronicle.core.git.JGitAnalyzer
import com.commitchronicle.core.template.MarkdownTemplateEngine
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.default
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.int
import kotlinx.coroutines.runBlocking
import java.io.File

fun main(args: Array<String>) {
    CommitChronicle()
        .subcommands(
            Summarize(),
            GeneratePR(),
            GenerateChangelog()
        )
        .main(args)
}

class CommitChronicle : CliktCommand() {
    override fun run() {
        echo("CommitChronicle - AI 기반 Git 커밋 & PR 초안 생성기")
        echo("사용 가능한 명령을 보려면 --help 옵션을 사용하세요.")
    }
}

class Summarize : CliktCommand(
    help = "특정 기간/범위의 커밋을 요약합니다."
) {
    private val repoPath by option("-p", "--path", help = "Git 저장소 경로")
        .default(".")
    
    private val sinceDays by option("-d", "--days", help = "며칠 전부터의 커밋을 분석할지 지정")
        .int()
        .default(7)
        
    private val limit by option("-l", "--limit", help = "가져올 최대 커밋 수")
        .int()
        .default(50)
        
    private val fromCommit by option("--from", help = "시작 커밋 ID (범위 지정 시)")
        
    private val toCommit by option("--to", help = "종료 커밋 ID (범위 지정 시)")
        .default("HEAD")
        
    private val apiKey by option("-k", "--key", help = "OpenAI API 키")
        .required()
        
    private val useMcp by option("--mcp", help = "MCP를 사용할지 여부 (JDK 17+ 필요)")
        .flag(default = false)
        
    private val outputFile by option("-o", "--output", help = "출력 파일 경로 (지정하지 않으면 콘솔에 출력)")
        
    override fun run() {
        runBlocking {
            echo("커밋 요약 생성 중...")
            
            val gitAnalyzer = JGitAnalyzer(repoPath)
            val aiSummarizer = AISummarizerFactory.create(apiKey, useMcp)
            
            val commits = when {
                fromCommit != null -> gitAnalyzer.getCommitRange(fromCommit!!, toCommit)
                else -> gitAnalyzer.getCommits(sinceDays)
            }.take(limit)
            
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
        }
    }
}

class GeneratePR : CliktCommand(
    name = "pr",
    help = "커밋 정보를 기반으로 PR 초안을 생성합니다."
) {
    private val repoPath by option("-p", "--path", help = "Git 저장소 경로")
        .default(".")
    
    private val sinceDays by option("-d", "--days", help = "며칠 전부터의 커밋을 분석할지 지정")
        .int()
        .default(7)
        
    private val limit by option("-l", "--limit", help = "가져올 최대 커밋 수")
        .int()
        .default(50)
        
    private val fromCommit by option("--from", help = "시작 커밋 ID (범위 지정 시)")
        
    private val toCommit by option("--to", help = "종료 커밋 ID (범위 지정 시)")
        .default("HEAD")
        
    private val title by option("-t", "--title", help = "PR 제목 (지정하지 않으면 자동 생성)")
        
    private val apiKey by option("-k", "--key", help = "OpenAI API 키")
        .required()
        
    private val useMcp by option("--mcp", help = "MCP를 사용할지 여부 (JDK 17+ 필요)")
        .flag(default = false)
        
    private val outputFile by option("-o", "--output", help = "출력 파일 경로 (지정하지 않으면 콘솔에 출력)")
        
    private val templatePath by option("--template", help = "사용자 정의 템플릿 파일 경로")
        
    override fun run() {
        runBlocking {
            echo("PR 초안 생성 중...")
            
            val gitAnalyzer = JGitAnalyzer(repoPath)
            val aiSummarizer = AISummarizerFactory.create(apiKey, useMcp)
            
            val commits = when {
                fromCommit != null -> gitAnalyzer.getCommitRange(fromCommit!!, toCommit)
                else -> gitAnalyzer.getCommits(sinceDays)
            }.take(limit)
            
            if (commits.isEmpty()) {
                echo("해당 기간/범위에 커밋이 없습니다.")
                return@runBlocking
            }
            
            echo("${commits.size}개의 커밋을 분석 중...")
            val prDraft = if (templatePath != null) {
                val templateEngine = MarkdownTemplateEngine()
                val summary = aiSummarizer.summarize(commits)
                
                val data = mapOf(
                    "commits" to commits,
                    "summary" to summary,
                    "title" to (title ?: "자동 생성된 PR 제목")
                )
                
                templateEngine.render(templatePath!!, data)
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
        }
    }
}

class GenerateChangelog : CliktCommand(
    name = "changelog",
    help = "커밋 정보를 기반으로 변경 로그를 생성합니다."
) {
    private val repoPath by option("-p", "--path", help = "Git 저장소 경로")
        .default(".")
    
    private val sinceDays by option("-d", "--days", help = "며칠 전부터의 커밋을 분석할지 지정")
        .int()
        .default(30)
        
    private val limit by option("-l", "--limit", help = "가져올 최대 커밋 수")
        .int()
        .default(100)
        
    private val fromCommit by option("--from", help = "시작 커밋 ID (범위 지정 시)")
        
    private val toCommit by option("--to", help = "종료 커밋 ID (범위 지정 시)")
        .default("HEAD")
        
    private val apiKey by option("-k", "--key", help = "OpenAI API 키")
        .required()
        
    private val useMcp by option("--mcp", help = "MCP를 사용할지 여부 (JDK 17+ 필요)")
        .flag(default = false)
        
    private val outputFile by option("-o", "--output", help = "출력 파일 경로 (지정하지 않으면 콘솔에 출력)")
        
    private val templatePath by option("--template", help = "사용자 정의 템플릿 파일 경로")
        
    private val groupByType by option("-g", "--group", help = "변경 유형별로 그룹화")
        .flag(default = true)
        
    override fun run() {
        runBlocking {
            echo("변경 로그 생성 중...")
            
            val gitAnalyzer = JGitAnalyzer(repoPath)
            val aiSummarizer = AISummarizerFactory.create(apiKey, useMcp)
            
            val commits = when {
                fromCommit != null -> gitAnalyzer.getCommitRange(fromCommit!!, toCommit)
                else -> gitAnalyzer.getCommits(sinceDays)
            }.take(limit)
            
            if (commits.isEmpty()) {
                echo("해당 기간/범위에 커밋이 없습니다.")
                return@runBlocking
            }
            
            echo("${commits.size}개의 커밋을 분석 중...")
            val changelog = if (templatePath != null) {
                val templateEngine = MarkdownTemplateEngine()
                val summary = aiSummarizer.summarize(commits)
                
                val data = mapOf(
                    "commits" to commits,
                    "summary" to summary,
                    "groupByType" to groupByType
                )
                
                templateEngine.render(templatePath!!, data)
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
        }
    }
} 