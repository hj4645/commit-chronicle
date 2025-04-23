package com.commitchronicle.intellij

import com.intellij.ide.AppLifecycleListener
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.EditorFactory

/**
 * 애플리케이션 초기화 리스너
 * 이전의 ApplicationComponent 대신 사용되는 현대적인 방식
 */
class CommitChronicleAppInitializedListener : AppLifecycleListener {
    fun componentsInitialized() {
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