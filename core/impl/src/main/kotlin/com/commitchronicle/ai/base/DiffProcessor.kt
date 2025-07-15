package com.commitchronicle.ai.base

/**
 * Diff 내용을 가공하여 의미 있는 변경사항만 추출하는 클래스
 */
object DiffProcessor {
    
    /**
     * Raw diff를 처리하여 의미 있는 변경사항만 추출
     */
    fun processDiff(diff: String): String {
        if (diff.isBlank()) return ""
        
        val meaningfulLines = diff.lines()
            .filter { line -> isMeaningfulChange(line) }
            .take(20) // 토큰 절약을 위해 최대 20줄로 제한
        
        return if (meaningfulLines.isEmpty()) {
            ""
        } else {
            meaningfulLines.joinToString("\n")
        }
    }
    
    /**
     * 해당 라인이 의미 있는 변경사항인지 판단
     */
    private fun isMeaningfulChange(line: String): Boolean {
        val trimmedLine = line.trim()
        
        // 빈 줄은 제외
        if (trimmedLine.isEmpty()) return false
        
        // diff 헤더는 제외 (@@, +++, ---, index 등)
        if (trimmedLine.startsWith("@@") || 
            trimmedLine.startsWith("+++") || 
            trimmedLine.startsWith("---") ||
            trimmedLine.startsWith("index ") ||
            trimmedLine.startsWith("diff --git")) {
            return false
        }
        
        // 변경되지 않은 컨텍스트 라인은 제외 (+ 또는 -로 시작하지 않는 라인)
        if (!trimmedLine.startsWith("+") && !trimmedLine.startsWith("-")) {
            return false
        }
        
        // + 또는 - 제거 후 실제 내용 확인
        val actualContent = trimmedLine.substring(1).trim()
        
        // 빈 줄 변경은 제외
        if (actualContent.isEmpty()) return false
        
        // 단순 주석 변경은 제외
        if (isOnlyCommentChange(actualContent)) return false
        
        // 단순 들여쓰기/공백 변경은 제외
        if (isOnlyWhitespaceChange(line)) return false
        
        // 단순 import 정렬은 제외 (연속된 import 변경)
        if (isImportReordering(actualContent)) return false
        
        return true
    }
    
    /**
     * 주석만 변경된 라인인지 확인
     */
    private fun isOnlyCommentChange(content: String): Boolean {
        val trimmed = content.trim()
        return trimmed.startsWith("//") ||
               trimmed.startsWith("/*") ||
               trimmed.startsWith("*") ||
               trimmed.startsWith("*/") ||
               trimmed.startsWith("#") ||
               trimmed.startsWith("<!--") ||
               trimmed.startsWith("-->")
    }
    
    /**
     * 들여쓰기/공백만 변경된 라인인지 확인
     */
    private fun isOnlyWhitespaceChange(line: String): Boolean {
        if (!line.startsWith("+") && !line.startsWith("-")) return false
        
        val content = line.substring(1)
        // 공백만 있거나 탭만 있는 경우
        return content.isBlank() || content.all { it.isWhitespace() }
    }
    
    /**
     * import 재정렬인지 확인
     */
    private fun isImportReordering(content: String): Boolean {
        val trimmed = content.trim()
        return trimmed.startsWith("import ") && 
               !trimmed.contains("new ") && 
               !trimmed.contains("=") &&
               !trimmed.contains("{") &&
               !trimmed.contains("(")
    }
    
    /**
     * 변경사항 요약 생성
     */
    fun generateChangeSummary(diff: String): String {
        if (diff.isBlank()) return "No meaningful changes"
        
        val lines = diff.lines()
        val additions = lines.count { it.trim().startsWith("+") && isMeaningfulChange(it) }
        val deletions = lines.count { it.trim().startsWith("-") && isMeaningfulChange(it) }
        
        return when {
            additions > 0 && deletions > 0 -> "Modified (+$additions, -$deletions lines)"
            additions > 0 -> "Added (+$additions lines)"
            deletions > 0 -> "Removed (-$deletions lines)"
            else -> "No meaningful changes"
        }
    }
} 