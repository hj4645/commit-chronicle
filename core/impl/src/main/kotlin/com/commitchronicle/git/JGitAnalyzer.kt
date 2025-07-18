package com.commitchronicle.git

import com.commitchronicle.git.model.ChangeType
import com.commitchronicle.git.model.Commit
import com.commitchronicle.git.model.FileChange
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.diff.DiffFormatter
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.revwalk.filter.RevFilter
import org.eclipse.jgit.treewalk.CanonicalTreeParser
import org.eclipse.jgit.util.io.DisabledOutputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.charset.StandardCharsets
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * JGit 라이브러리를 사용한 GitAnalyzer 구현
 */
class JGitAnalyzer(private val repoPath: String) : GitAnalyzer {
    private val git: Git by lazy { Git.open(File(repoPath)) }
    private val repository: Repository by lazy { git.repository }

    override suspend fun getCommits(days: Int): List<Commit> = withContext(Dispatchers.IO) {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -days)

        try {
            return@withContext git.log()
                .all()
                .setRevFilter(object : RevFilter() {
                    override fun include(walker: RevWalk, commit: RevCommit): Boolean {
                        val commitTime = commit.committerIdent.`when`.time
                        return commitTime >= calendar.timeInMillis
                    }

                    override fun clone(): RevFilter {
                        return this
                    }
                })
                .call()
                .map { revCommit -> mapToCommit(revCommit) }
        } catch (e: Exception) {
            println("Error getting commits: ${e.message}")
            return@withContext emptyList()
        }
    }

    override suspend fun getCommitRange(from: String, to: String): List<Commit> = withContext(Dispatchers.IO) {
        try {
            val resolvedToCommit = if (to == "HEAD") "HEAD" else to
            return@withContext git.log()
                .addRange(
                    repository.resolve(from), 
                    repository.resolve(resolvedToCommit)
                )
                .call()
                .map { revCommit -> mapToCommit(revCommit) }
        } catch (e: Exception) {
            println("Error getting commit range: ${e.message}")
            return@withContext emptyList()
        }
    }

    override suspend fun getRecentCommits(limit: Int): List<Commit> = withContext(Dispatchers.IO) {
        try {
            return@withContext git.log()
                .setMaxCount(limit)
                .call()
                .map { revCommit -> mapToCommit(revCommit) }
        } catch (e: Exception) {
            println("Error getting recent commits: ${e.message}")
            return@withContext emptyList()
        }
    }

    override suspend fun getCurrentBranch(): String = withContext(Dispatchers.IO) {
        try {
            return@withContext repository.branch
        } catch (e: Exception) {
            println("Error getting current branch: ${e.message}")
            return@withContext "unknown"
        }
    }

    private fun mapToCommit(revCommit: RevCommit): Commit {
        val id = revCommit.name
        val parentId = if (revCommit.parentCount > 0) revCommit.getParent(0).name else null
        val isMergeCommit = revCommit.parentCount > 1
        val changes = if (parentId != null && !isMergeCommit) {
            getChangesForCommit(id, parentId)
        } else {
            emptyList()
        }

        return Commit(
            id = id,
            shortId = id.substring(0, 7),
            message = revCommit.fullMessage,
            author = revCommit.authorIdent.name,
            email = revCommit.authorIdent.emailAddress,
            date = ZonedDateTime.ofInstant(
                revCommit.authorIdent.`when`.toInstant(),
                ZoneId.systemDefault()
            ),
            changes = changes,
            isMergeCommit = isMergeCommit
        )
    }

    private fun getChangesForCommit(commitId: String, parentId: String): List<FileChange> {
        try {
            val oldTreeId = repository.resolve("$parentId^{tree}")
            val newTreeId = repository.resolve("$commitId^{tree}")

            val reader = repository.newObjectReader()
            val oldTreeParser = CanonicalTreeParser()
            oldTreeParser.reset(reader, oldTreeId)
            val newTreeParser = CanonicalTreeParser()
            newTreeParser.reset(reader, newTreeId)

            DiffFormatter(DisabledOutputStream.INSTANCE).use { diffFormatter ->
                diffFormatter.setRepository(repository)
                diffFormatter.setDetectRenames(true)

                val diffs = diffFormatter.scan(oldTreeParser, newTreeParser)
                return diffs.map { mapToFileChange(it) }
            }
        } catch (e: Exception) {
            println("Error getting changes for commit: ${e.message}")
            return emptyList()
        }
    }

    private fun mapToFileChange(diffEntry: DiffEntry): FileChange {
        val diff = try {
            ByteArrayOutputStream().use { byteOut ->
                DiffFormatter(byteOut).use { formatter ->
                    formatter.setRepository(repository)
                    formatter.format(diffEntry)
                    String(byteOut.toByteArray(), StandardCharsets.UTF_8)
                }
            }
        } catch (e: Exception) {
            "Error generating diff: ${e.message}"
        }

        // 간단한 방식으로 추가된 라인과 삭제된 라인 계산
        val additions = diff.lines().count { it.startsWith("+") && !it.startsWith("+++") }
        val deletions = diff.lines().count { it.startsWith("-") && !it.startsWith("---") }

        return FileChange(
            path = diffEntry.newPath.takeUnless { it == DiffEntry.DEV_NULL } ?: diffEntry.oldPath,
            changeType = mapChangeType(diffEntry.changeType),
            additions = additions,
            deletions = deletions,
            diff = diff
        )
    }

    private fun mapChangeType(changeType: DiffEntry.ChangeType): ChangeType {
        return when (changeType) {
            DiffEntry.ChangeType.ADD -> ChangeType.ADD
            DiffEntry.ChangeType.MODIFY -> ChangeType.MODIFY
            DiffEntry.ChangeType.DELETE -> ChangeType.DELETE
            DiffEntry.ChangeType.RENAME -> ChangeType.RENAME
            DiffEntry.ChangeType.COPY -> ChangeType.COPY
        }
    }
} 
