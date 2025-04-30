package com.commitchronicle.git.model

import java.time.ZonedDateTime

/**
 * Git 커밋 정보를 담는 데이터 클래스
 */
data class Commit(
    val id: String,
    val shortId: String,
    val message: String,
    val author: String,
    val email: String,
    val date: ZonedDateTime,
    val changes: List<FileChange> = emptyList()
)

/**
 * 파일 변경 정보를 담는 데이터 클래스
 */
data class FileChange(
    val path: String,
    val changeType: ChangeType,
    val additions: Int,
    val deletions: Int,
    val diff: String
)

/**
 * 변경 유형
 */
enum class ChangeType {
    ADD, MODIFY, DELETE, RENAME, COPY
} 