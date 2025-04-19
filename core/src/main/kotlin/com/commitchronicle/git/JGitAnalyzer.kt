package com.commitchronicle.git

import com.commitchronicle.model.ChangeType
import com.commitchronicle.model.Commit
import com.commitchronicle.model.FileChange
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

/**
 * JGit 라이브러리를 사용한 GitAnalyzer 구현
 */
class JGitAnalyzer(private val repoPath: String) : GitAnalyzer {
    private val git: Git by lazy { Git.open(File(repoPath)) }
    private val repository: Repository by lazy { git.repository }

    override fun getCommits(sinceDays: Int): List<Commit> {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -sinceDays)

        try {
            return git.log()
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
            // Log the error or handle it appropriately
            println("Error getting commits: ${e.message}")
            return emptyList()
        }
    }

    override fun getCommitRange(fromCommit: String, toCommit: String): List<Commit> {
        try {
            val resolvedToCommit = if (toCommit == "HEAD") "HEAD" else toCommit
            return git.log()
                .addRange(
                    repository.resolve(fromCommit), 
                    repository.resolve(resolvedToCommit)
                )
                .call()
                .map { revCommit -> mapToCommit(revCommit) }
        } catch (e: Exception) {
            // Log the error or handle it appropriately
            println("Error getting commit range: ${e.message}")
            return emptyList()
        }
    }

    override fun getRecentCommits(limit: Int): List<Commit> {
        try {
            return git.log()
                .setMaxCount(limit)
                .call()
                .map { revCommit -> mapToCommit(revCommit) }
        } catch (e: Exception) {
            // Log the error or handle it appropriately
            println("Error getting recent commits: ${e.message}")
            return emptyList()
        }
    }

    private fun mapToCommit(revCommit: RevCommit): Commit {
        val id = revCommit.name
        val parentId = if (revCommit.parentCount > 0) revCommit.getParent(0).name else null
        val changes = if (parentId != null) {
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
            changes = changes
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
                return diffs.map { mapToFileChange(it, diffFormatter) }
            }
        } catch (e: Exception) {
            // Log the error or handle it appropriately
            println("Error getting changes for commit: ${e.message}")
            return emptyList()
        }
    }

    private fun mapToFileChange(diffEntry: DiffEntry, diffFormatter: DiffFormatter): FileChange {
        var diff = ""

        try {
            ByteArrayOutputStream().use { byteOut ->
                DiffFormatter(byteOut).use { formatter ->
                    formatter.setRepository(repository)
                    formatter.format(diffEntry)
                    diff = String(byteOut.toByteArray(), StandardCharsets.UTF_8)
                }
            }
        } catch (e: Exception) {
            // Log the error or handle it appropriately
            diff = "Error generating diff: ${e.message}"
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
