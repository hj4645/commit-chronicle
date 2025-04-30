package com.commitchronicle.git

import com.commitchronicle.git.model.Commit

/**
 * Git 저장소를 분석하는 인터페이스
 */
interface GitAnalyzer {
    /**
     * 지정된 기간 동안의 커밋을 가져옵니다.
     *
     * @param days 가져올 커밋의 기간 (일)
     * @return 커밋 목록
     */
    suspend fun getCommits(days: Int): List<Commit>

    /**
     * 지정된 커밋 범위의 커밋을 가져옵니다.
     *
     * @param from 시작 커밋 ID
     * @param to 종료 커밋 ID (기본값: "HEAD")
     * @return 커밋 목록
     */
    suspend fun getCommitRange(from: String, to: String = "HEAD"): List<Commit>
    
    /**
     * 최근 커밋을 제한된 개수만큼 가져옵니다.
     *
     * @param limit 가져올 최대 커밋 수
     * @return 커밋 목록
     */
    suspend fun getRecentCommits(limit: Int): List<Commit>
} 