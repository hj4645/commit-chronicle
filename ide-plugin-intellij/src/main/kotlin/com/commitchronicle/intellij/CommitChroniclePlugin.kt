package com.commitchronicle.intellij

import com.commitchronicle.ai.AISummarizerFactory
import com.commitchronicle.git.JGitAnalyzer
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.Disposable
import git4idea.repo.GitRepositoryManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext
import com.intellij.ide.AppLifecycleListener
import com.intellij.ide.ApplicationInitializedListener

/**
 * CommitChronicle IntelliJ 플러그인 메인 클래스
 */
class CommitChroniclePlugin : Disposable {
    companion object {
        const val PLUGIN_ID = "com.commitchronicle.intellij"
        val PLUGIN_ICON = IconLoader.getIcon("/icons/plugin_icon.svg", CommitChroniclePlugin::class.java)
        
        /**
         * UI 스레드에서 람다를 실행합니다.
         */
        fun invokeLater(project: Project, runnable: () -> Unit) {
            ApplicationManager.getApplication().invokeLater(
                runnable, 
                ModalityState.defaultModalityState(),
                project.disposed
            )
        }
        
        /**
         * 사용자로부터 API 키를 입력받습니다.
         */
        fun getApiKey(project: Project): String? {
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
        fun showNotification(
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
    }
    
    /**
     * 프로젝트 이벤트를 수신하는 리스너
     */
    class Listener : com.intellij.openapi.project.ProjectManagerListener {
        override fun projectOpened(project: Project) {
            // 프로젝트가 열릴 때 실행되는 코드
        }
        
        override fun projectClosed(project: Project) {
            // 프로젝트가 닫힐 때 실행되는 코드
            
            // 코루틴 서비스 작업 취소
            try {
                val coroutineService = ApplicationManager.getApplication().getService(CoroutineService::class.java)
                coroutineService?.cancelAllJobs()
            } catch (e: Exception) {
                // 예외 처리
            }
            
            // 열린 에디터 정리
            try {
                // 프로젝트의 모든 에디터 닫기
                val editorManager = FileEditorManager.getInstance(project)
                editorManager.openFiles.forEach { editorManager.closeFile(it) }
                
                // 에디터 리소스 명시적으로 해제
                ApplicationManager.getApplication().invokeLater {
                    val editorFactory = EditorFactory.getInstance()
                    val editors = editorFactory.getAllEditors()
                    for (editor in editors) {
                        if (!editor.isDisposed) {
                            try {
                                editorFactory.releaseEditor(editor)
                            } catch (e: Exception) {
                                // 예외 처리
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // 예외 처리
            }
        }
    }
    
    /**
     * 플러그인 리소스 해제
     */
    override fun dispose() {
        // 코루틴 작업 취소
        try {
            val coroutineService = ApplicationManager.getApplication().getService(CoroutineService::class.java)
            coroutineService?.cancelAllJobs()
        } catch (e: Exception) {
            // 예외 처리
        }
    }
}

/**
 * IntelliJ에서 사용할 코루틴 스코프를 제공하는 서비스
 * ApplicationManager에 의해 관리되며 플러그인의 생명주기 동안 유지됩니다.
 */
@Service
class CoroutineService : CoroutineScope {
    private val job = SupervisorJob()
    
    // 백그라운드 작업용 디스패처 (Default 사용으로 변경)
    override val coroutineContext: CoroutineContext = Dispatchers.Default + job
    
    // 스레드 안전을 위해 Default 디스패처에서만 작업 
    fun launchBackground(block: suspend CoroutineScope.() -> Unit): Job {
        return launch(coroutineContext, CoroutineStart.DEFAULT, block)
    }
    
    fun cancelAllJobs() {
        job.cancel()
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
        
        val apiKey = CommitChroniclePlugin.getApiKey(project)
        if (apiKey.isNullOrBlank()) {
            return
        }
        
        // 어플리케이션 서비스에서 코루틴 스코프 가져오기
        val coroutineService = ApplicationManager.getApplication().getService(CoroutineService::class.java)
        
        coroutineService.launchBackground {
            try {
                val gitAnalyzer = JGitAnalyzer(repoPath)
                val aiSummarizer = AISummarizerFactory.create(apiKey)
                
                // suspend 함수 호출
                val commits = gitAnalyzer.getCommits(7) // 최근 7일간의 커밋
                if (commits.isEmpty()) {
                    CommitChroniclePlugin.invokeLater(project) {
                        Messages.showInfoMessage(project, "최근 7일간의 커밋이 없습니다.", "CommitChronicle")
                    }
                    return@launchBackground
                }
                
                // suspend 함수 호출
                val summary = aiSummarizer.summarize(commits)
                
                CommitChroniclePlugin.invokeLater(project) {
                    CommitChroniclePlugin.showNotification(
                        project,
                        "커밋 요약",
                        summary,
                        NotificationType.INFORMATION
                    )
                }
            } catch (ex: Exception) {
                CommitChroniclePlugin.invokeLater(project) {
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
        
        val apiKey = CommitChroniclePlugin.getApiKey(project)
        if (apiKey.isNullOrBlank()) {
            return
        }
        
        // 어플리케이션 서비스에서 코루틴 스코프 가져오기
        val coroutineService = ApplicationManager.getApplication().getService(CoroutineService::class.java)
        
        coroutineService.launchBackground {
            try {
                val gitAnalyzer = JGitAnalyzer(repoPath)
                val aiSummarizer = AISummarizerFactory.create(apiKey)
                
                // suspend 함수 호출
                val commits = gitAnalyzer.getCommits(7) // 최근 7일간의 커밋
                if (commits.isEmpty()) {
                    CommitChroniclePlugin.invokeLater(project) {
                        Messages.showInfoMessage(project, "최근 7일간의 커밋이 없습니다.", "CommitChronicle")
                    }
                    return@launchBackground
                }
                
                // suspend 함수 호출
                val prDraft = aiSummarizer.generatePRDraft(commits, title)
                
                CommitChroniclePlugin.invokeLater(project) {
                    CommitChroniclePlugin.showNotification(
                        project,
                        "PR 초안",
                        prDraft,
                        NotificationType.INFORMATION
                    )
                }
            } catch (ex: Exception) {
                CommitChroniclePlugin.invokeLater(project) {
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
        
        val apiKey = CommitChroniclePlugin.getApiKey(project)
        if (apiKey.isNullOrBlank()) {
            return
        }
        
        // 어플리케이션 서비스에서 코루틴 스코프 가져오기
        val coroutineService = ApplicationManager.getApplication().getService(CoroutineService::class.java)
        
        coroutineService.launchBackground {
            try {
                val gitAnalyzer = JGitAnalyzer(repoPath)
                val aiSummarizer = AISummarizerFactory.create(apiKey)
                
                // suspend 함수 호출
                val commits = gitAnalyzer.getCommits(30) // 최근 30일간의 커밋
                if (commits.isEmpty()) {
                    CommitChroniclePlugin.invokeLater(project) {
                        Messages.showInfoMessage(project, "최근 30일간의 커밋이 없습니다.", "CommitChronicle")
                    }
                    return@launchBackground
                }
                
                // suspend 함수 호출
                val changelog = aiSummarizer.generateChangelog(commits, true)
                
                CommitChroniclePlugin.invokeLater(project) {
                    CommitChroniclePlugin.showNotification(
                        project,
                        "변경 로그",
                        changelog,
                        NotificationType.INFORMATION
                    )
                }
            } catch (ex: Exception) {
                CommitChroniclePlugin.invokeLater(project) {
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
 * 애플리케이션 초기화 리스너
 * 이전의 ApplicationComponent 대신 사용되는 현대적인 방식
 */
class CommitChronicleAppInitializedListener : ApplicationInitializedListener {
    override fun componentsInitialized() {
        // 애플리케이션 초기화 완료 시 호출
        
        // 애플리케이션 종료 리스너 등록
        ApplicationManager.getApplication().messageBus.connect()
            .subscribe(AppLifecycleListener.TOPIC, object : AppLifecycleListener {
                override fun appWillBeClosed(isRestart: Boolean) {
                    // IDE 종료 시 호출됨
                    cleanupResources()
                }
            })
    }
    
    private fun cleanupResources() {
        try {
            // 코루틴 서비스 작업 취소
            val coroutineService = ApplicationManager.getApplication().getService(CoroutineService::class.java)
            coroutineService?.cancelAllJobs()
            
            // 에디터 리소스 정리
            try {
                ApplicationManager.getApplication().invokeAndWait {
                    val editorFactory = EditorFactory.getInstance()
                    val editors = editorFactory.getAllEditors()
                    for (editor in editors) {
                        if (!editor.isDisposed) {
                            try {
                                editorFactory.releaseEditor(editor)
                            } catch (e: Exception) {
                                // 에디터 해제 중 예외는 무시
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // 에디터 해제 중 예외 발생 시 무시
            }
        } catch (e: Exception) {
            // 예외 처리
        }
    }
} 