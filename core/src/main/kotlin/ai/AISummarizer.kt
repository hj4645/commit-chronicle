package com.commitchronicle.core.ai

import com.commitchronicle.core.model.Commit

/**
 * 커밋 정보를 기반으로 요약을 생성하는 인터페이스
 */
interface AISummarizer {
    /**
     * 커밋 목록을 요약합니다.
     *
     * @param commits 요약할 커밋 목록
     * @return 생성된 요약 텍스트
     */
    suspend fun summarize(commits: List<Commit>): String
    
    /**
     * 커밋 목록을 기반으로 PR 초안을 생성합니다.
     * 
     * @param commits 요약할 커밋 목록
     * @param title PR 제목 (null인 경우 자동 생성)
     * @return 생성된 PR 초안
     */
    suspend fun generatePRDraft(commits: List<Commit>, title: String? = null): String
    
    /**
     * 커밋 목록을 기반으로 변경 로그를 생성합니다.
     *
     * @param commits 요약할 커밋 목록
     * @param groupByType 파일 유형별로 그룹화할지 여부
     * @return 생성된 변경 로그
     */
    suspend fun generateChangelog(commits: List<Commit>, groupByType: Boolean = false): String
} 