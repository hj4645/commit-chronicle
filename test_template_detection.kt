import com.commitchronicle.template.TemplateEngineFactory
import java.io.File

fun main() {
    println("🔍 GitHub 템플릿 감지 테스트 시작...")
    
    val templateDetector = TemplateEngineFactory.createGitHubTemplateDetector()
    val currentPath = System.getProperty("user.dir")
    
    println("📁 현재 경로: $currentPath")
    
    // PR 템플릿 감지 테스트
    val prTemplate = templateDetector.findPRTemplate(currentPath)
    if (prTemplate != null) {
        println("✅ PR 템플릿 감지됨: $prTemplate")
        
        // 템플릿 내용 읽기
        val content = templateDetector.readTemplateContent(prTemplate)
        if (content != null) {
            println("📄 템플릿 내용:")
            println(content)
        } else {
            println("❌ 템플릿 내용을 읽을 수 없습니다")
        }
    } else {
        println("❌ PR 템플릿을 찾을 수 없습니다")
    }
    
    // Issue 템플릿 감지 테스트
    val issueTemplates = templateDetector.findIssueTemplates(currentPath)
    if (issueTemplates.isNotEmpty()) {
        println("✅ Issue 템플릿 감지됨: $issueTemplates")
    } else {
        println("ℹ️ Issue 템플릿을 찾을 수 없습니다")
    }
    
    // 모든 템플릿 감지 테스트
    val allTemplates = templateDetector.findAllTemplates(currentPath)
    println("📋 모든 템플릿: $allTemplates")
    
    println("🎉 테스트 완료!")
} 