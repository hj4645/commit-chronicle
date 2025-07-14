package com.commitchronicle.template

import java.io.File
import java.util.regex.Pattern
import kotlin.collections.get

/**
 * 간단한 마크다운 기반 템플릿 엔진 구현
 */
class MarkdownTemplateEngine : TemplateEngine {
    private val variablePattern = Pattern.compile("\\{\\{\\s*([\\w.]+)\\s*\\}\\}")
    private val loopStartPattern = Pattern.compile("\\{\\{\\s*for\\s+([\\w]+)\\s+in\\s+([\\w.]+)\\s*\\}\\}")
    private val loopEndPattern = Pattern.compile("\\{\\{\\s*endfor\\s*\\}\\}")
    private val conditionalStartPattern = Pattern.compile("\\{\\{\\s*if\\s+([\\w.]+)\\s*\\}\\}")
    private val conditionalEndPattern = Pattern.compile("\\{\\{\\s*endif\\s*\\}\\}")
    
    override fun render(templatePath: String, data: Map<String, Any>): String {
        val templateContent = File(templatePath).readText()
        return renderString(templateContent, data)
    }
    
    override fun renderString(templateContent: String, data: Map<String, Any>): String {
        var result = templateContent
        
        // 간단한 변수 치환
        result = replaceVariables(result, data)
        
        // 조건문 처리
        result = processConditionals(result, data)
        
        // 반복문 처리
        result = processLoops(result, data)
        
        return result
    }
    
    private fun replaceVariables(content: String, data: Map<String, Any>): String {
        val matcher = variablePattern.matcher(content)
        val result = StringBuffer()
        
        while (matcher.find()) {
            val variableName = matcher.group(1)
            val value = getNestedValue(data, variableName) ?: ""
            matcher.appendReplacement(result, value.toString().replace("$", "\\$"))
        }
        
        matcher.appendTail(result)
        return result.toString()
    }
    
    private fun processConditionals(content: String, data: Map<String, Any>): String {
        var result = content
        val startMatcher = conditionalStartPattern.matcher(result)
        
        while (startMatcher.find()) {
            val conditionVar = startMatcher.group(1)
            val conditionValue = getNestedValue(data, conditionVar)
            
            // 조건문 블록 찾기
            val startPos = startMatcher.start()
            var endPos = -1
            var depth = 1
            
            val endMatcher = conditionalEndPattern.matcher(result)
            while (endMatcher.find(startMatcher.end())) {
                depth--
                if (depth == 0) {
                    endPos = endMatcher.end()
                    break
                }
            }
            
            if (endPos != -1) {
                val conditionalBlock = result.substring(startMatcher.end(), endMatcher.start())
                val replacement = if (isTruthy(conditionValue)) {
                    conditionalBlock
                } else {
                    ""
                }
                
                result = result.substring(0, startPos) + replacement + result.substring(endPos)
                // 매처 재설정
                startMatcher.reset(result)
            }
        }
        
        return result
    }
    
    private fun processLoops(content: String, data: Map<String, Any>): String {
        var result = content
        val startMatcher = loopStartPattern.matcher(result)
        
        while (startMatcher.find()) {
            val loopVar = startMatcher.group(1)
            val itemsVar = startMatcher.group(2)
            val items = getNestedValue(data, itemsVar) as? List<*> ?: continue
            
            // 루프 블록 찾기
            val startPos = startMatcher.start()
            var endPos = -1
            var depth = 1
            
            val endMatcher = loopEndPattern.matcher(result)
            while (endMatcher.find(startMatcher.end())) {
                depth--
                if (depth == 0) {
                    endPos = endMatcher.end()
                    break
                }
            }
            
            if (endPos != -1) {
                val loopBlock = result.substring(startMatcher.end(), endMatcher.start())
                val loopResult = StringBuilder()
                
                // 각 아이템에 대해 루프 블록 처리
                for (item in items) {
                    val itemData = HashMap(data)
                    itemData[loopVar] = item!!
                    loopResult.append(renderString(loopBlock, itemData))
                }
                
                result = result.substring(0, startPos) + loopResult.toString() + result.substring(endPos)
                // 매처 재설정
                startMatcher.reset(result)
            }
        }
        
        return result
    }
    
    private fun getNestedValue(data: Map<String, Any>, key: String): Any? {
        val parts = key.split(".")
        var current: Any? = data
        
        for (part in parts) {
            current = when (current) {
                is Map<*, *> -> current[part]
                else -> return null
            }
        }
        
        return current
    }
    
    private fun isTruthy(value: Any?): Boolean {
        return when (value) {
            null -> false
            is Boolean -> value
            is String -> value.isNotEmpty()
            is Number -> value.toDouble() != 0.0
            is Collection<*> -> value.isNotEmpty()
            is Map<*, *> -> value.isNotEmpty()
            else -> true
        }
    }
} 