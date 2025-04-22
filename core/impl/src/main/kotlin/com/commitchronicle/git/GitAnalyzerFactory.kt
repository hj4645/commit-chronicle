package com.commitchronicle.git

/**
 * GitAnalyzer 구현체를 생성하는 팩토리 클래스
 */
object GitAnalyzerFactory {
    /**
     * 저장소 경로와 엔진 유형에 따라 적절한 GitAnalyzer 구현체를 생성합니다.
     *
     * @param repoPath Git 저장소 경로
     * @param engine 사용할 Git 엔진 (기본값: "jgit")
     * @return GitAnalyzer 구현체
     */
    fun create(repoPath: String, engine: String = "jgit"): GitAnalyzer {
        return when (engine) {
            // 향후 다른 Git 엔진 추가 가능성을 위해 when 구문 유지
            else -> JGitAnalyzer(repoPath)
        }
    }
} 