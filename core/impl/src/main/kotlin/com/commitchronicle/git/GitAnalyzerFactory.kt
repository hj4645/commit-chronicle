package com.commitchronicle.git

/**
 * GitAnalyzer 구현체를 생성하는 팩토리 클래스
 */
object GitAnalyzerFactory {
    /**
     * 저장소 경로에 따라 적절한 GitAnalyzer 구현체를 생성합니다.
     *
     * @param repoPath Git 저장소 경로
     * @return GitAnalyzer 구현체
     */
    fun create(repoPath: String): GitAnalyzer {
        return JGitAnalyzer(repoPath)
    }
} 