package com.commitchronicle.intellij

import com.commitchronicle.ai.AISummarizerFactory
import com.commitchronicle.git.JGitAnalyzer
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.IconLoader
import git4idea.repo.GitRepositoryManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * CommitChronicle IntelliJ 플러그인 메인 클래스
 */
class CommitChroniclePlugin {
    companion object {
        const val PLUGIN_ID = "com.commitchronicle.intellij"
        val PLUGIN_ICON = IconLoader.getIcon("/icons/plugin_icon.svg", CommitChroniclePlugin::class.java)
    }
}

/**
 * 커밋 요약 액션
 */
class SummarizeCommitsAction : AnAction("커밋 요약 생성") {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val gitRepoManager = GitRepositoryManager.getInstance(project)
        val repos = gitRepoManager.repositories
        
        if (repos.isEmpty()) {
            Messages.showWarningDialog(project, "Git 저장소를 찾을 수 없습니다.", "CommitChronicle")
            return
        }
        
        val repo = repos.first()
        val repoPath = repo.root.path
        
        val apiKey = getApiKey(project)
        if (apiKey.isNullOrBlank()) {
            return
        }
        
        val scope = CoroutineScope(Dispatchers.Main)
        scope.launch {
            try {
                val gitAnalyzer = JGitAnalyzer(repoPath)
                val aiSummarizer = AISummarizerFactory.create(apiKey, false)
                
                withContext(Dispatchers.IO) {
                    val commits = gitAnalyzer.getCommits(7) // 최근 7일간의 커밋
                    if (commits.isEmpty()) {
                        withContext(Dispatchers.Main) {
                            Messages.showInfoMessage(project, "최근 7일간의 커밋이 없습니다.", "CommitChronicle")
                        }
                        return@withContext
                    }
                    
                    val summary = aiSummarizer.summarize(commits)
                    
                    withContext(Dispatchers.Main) {
                        showNotification(
                            project,
                            "커밋 요약",
                            summary,
                            NotificationType.INFORMATION
                        )
                    }
                }
            } catch (ex: Exception) {
                withContext(Dispatchers.Main) {
                    Messages.showErrorDialog(
                        project,
                        "요약 생성 중 오류가 발생했습니다: ${ex.message}",
                        "CommitChronicle"
                    )
                }
            }
        }
    }
}

/**
 * PR 초안 생성 액션
 */
class GeneratePRAction : AnAction("PR 초안 생성") {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val gitRepoManager = GitRepositoryManager.getInstance(project)
        val repos = gitRepoManager.repositories
        
        if (repos.isEmpty()) {
            Messages.showWarningDialog(project, "Git 저장소를 찾을 수 없습니다.", "CommitChronicle")
            return
        }
        
        val repo = repos.first()
        val repoPath = repo.root.path
        
        val title = Messages.showInputDialog(
            project,
            "PR 제목을 입력하세요 (선택 사항):",
            "CommitChronicle",
            null
        )
        
        val apiKey = getApiKey(project)
        if (apiKey.isNullOrBlank()) {
            return
        }
        
        val scope = CoroutineScope(Dispatchers.Main)
        scope.launch {
            try {
                val gitAnalyzer = JGitAnalyzer(repoPath)
                val aiSummarizer = AISummarizerFactory.create(apiKey, false)
                
                withContext(Dispatchers.IO) {
                    val commits = gitAnalyzer.getCommits(7) // 최근 7일간의 커밋
                    if (commits.isEmpty()) {
                        withContext(Dispatchers.Main) {
                            Messages.showInfoMessage(project, "최근 7일간의 커밋이 없습니다.", "CommitChronicle")
                        }
                        return@withContext
                    }
                    
                    val prDraft = aiSummarizer.generatePRDraft(commits, title)
                    
                    withContext(Dispatchers.Main) {
                        showNotification(
                            project,
                            "PR 초안",
                            prDraft,
                            NotificationType.INFORMATION
                        )
                    }
                }
            } catch (ex: Exception) {
                withContext(Dispatchers.Main) {
                    Messages.showErrorDialog(
                        project,
                        "PR 초안 생성 중 오류가 발생했습니다: ${ex.message}",
                        "CommitChronicle"
                    )
                }
            }
        }
    }
}

/**
 * 변경 로그 생성 액션
 */
class GenerateChangelogAction : AnAction("변경 로그 생성") {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val gitRepoManager = GitRepositoryManager.getInstance(project)
        val repos = gitRepoManager.repositories
        
        if (repos.isEmpty()) {
            Messages.showWarningDialog(project, "Git 저장소를 찾을 수 없습니다.", "CommitChronicle")
            return
        }
        
        val repo = repos.first()
        val repoPath = repo.root.path
        
        val apiKey = getApiKey(project)
        if (apiKey.isNullOrBlank()) {
            return
        }
        
        val scope = CoroutineScope(Dispatchers.Main)
        scope.launch {
            try {
                val gitAnalyzer = JGitAnalyzer(repoPath)
                val aiSummarizer = AISummarizerFactory.create(apiKey, false)
                
                withContext(Dispatchers.IO) {
                    val commits = gitAnalyzer.getCommits(30) // 최근 30일간의 커밋
                    if (commits.isEmpty()) {
                        withContext(Dispatchers.Main) {
                            Messages.showInfoMessage(project, "최근 30일간의 커밋이 없습니다.", "CommitChronicle")
                        }
                        return@withContext
                    }
                    
                    val changelog = aiSummarizer.generateChangelog(commits, true)
                    
                    withContext(Dispatchers.Main) {
                        showNotification(
                            project,
                            "변경 로그",
                            changelog,
                            NotificationType.INFORMATION
                        )
                    }
                }
            } catch (ex: Exception) {
                withContext(Dispatchers.Main) {
                    Messages.showErrorDialog(
                        project,
                        "변경 로그 생성 중 오류가 발생했습니다: ${ex.message}",
                        "CommitChronicle"
                    )
                }
            }
        }
    }
}

/**
 * 사용자로부터 API 키를 입력받습니다.
 */
private fun getApiKey(project: Project): String? {
    return Messages.showPasswordDialog(
        project,
        "OpenAI API 키를 입력하세요:",
        "CommitChronicle",
        null
    )
}

/**
 * 알림을 표시합니다.
 */
private fun showNotification(
    project: Project,
    title: String,
    content: String,
    type: NotificationType
) {
    val notificationGroup = NotificationGroupManager.getInstance()
        .getNotificationGroup("CommitChronicle.Notifications")
    
    notificationGroup.createNotification(content, type)
        .setTitle(title)
        .notify(project)
} 