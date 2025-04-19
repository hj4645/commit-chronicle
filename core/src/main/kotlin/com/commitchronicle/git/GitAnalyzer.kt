package com.commitchronicle.git

import com.commitchronicle.model.Commit

/**
 * Git 저장소의 커밋 정보를 분석하는 인터페이스
 */
interface GitAnalyzer {
    /**
     * 지정된 기간 내의 커밋 목록을 가져옵니다.
     * 
     * @param sinceDays 며칠 전부터의 커밋을 가져올지 지정
     * @return 커밋 목록
     */
    fun getCommits(sinceDays: Int): List<Commit>
    
    /**
     * 특정 범위의 커밋 목록을 가져옵니다.
     *
     * @param fromCommit 시작 커밋 ID
     * @param toCommit 종료 커밋 ID (기본값은 HEAD)
     * @return 커밋 목록
     */
    fun getCommitRange(fromCommit: String, toCommit: String = "HEAD"): List<Commit>
    
    /**
     * 최근 N개의 커밋을 가져옵니다.
     *
     * @param limit 가져올 커밋 수
     * @return 커밋 목록
     */
    fun getRecentCommits(limit: Int): List<Commit>
} 