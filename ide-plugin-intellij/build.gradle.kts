plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.15.0"
    kotlin("jvm")
}

group = "com.commitchronicle"
version = "0.1.0"

// 자바 및 코틀린 버전 설정
val javaVersion = "11"
val coroutinesVersion = "1.5.2" // IntelliJ 2021.3과 호환되는 버전

sourceSets {
    main {
        java.srcDirs("src/main/kotlin")
        resources.srcDirs("src/main/resources")
    }
}

repositories {
    mavenCentral()
}

configurations {
    create("includeInJar")
}

dependencies {
    implementation(project(":core:api"))
    implementation(project(":core:impl"))
    
    // 코루틴 라이브러리는 전적으로 명시적 포함으로 처리
    "includeInJar"("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    "includeInJar"("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$coroutinesVersion")
    // Java 버전에 맞는 코루틴 라이브러리
    "includeInJar"("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:$coroutinesVersion")
    
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$coroutinesVersion")
}

intellij {
    version.set("2021.3")
    type.set("IC")
    plugins.set(listOf("Git4Idea", "com.intellij.java", "org.jetbrains.kotlin"))
    updateSinceUntilBuild.set(false)
    sameSinceUntilBuild.set(false)
    
    // 로컬 IDE 사용 시 localPath 설정 (선택사항)
    // localPath.set("/Applications/IntelliJ IDEA.app/Contents")
    
    // 플러그인 클래스로더 설정
    sandboxDir.set("${project.rootDir}/idea-sandbox")
}

tasks.withType<Copy> {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
        options.encoding = "UTF-8"
    }
    
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
            languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_1_5)
            apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_1_5)
            freeCompilerArgs.add("-Xskip-metadata-version-check")
        }
    }
    
    processResources {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
    
    patchPluginXml {
        sinceBuild.set("213")
        untilBuild.set("251.*")
        pluginDescription.set("""
            Git 커밋 히스토리와 AI를 활용한 Pull Request 초안 및 버전별 변경 사항 요약, 커밋 내용 등 자동 생성 
            플러그인입니다.

            <h3>주요 기능</h3>
            <ul>
              <li>특정 기간/커밋 범위 기반 변경 사항 분석</li>
              <li>OpenAI API를 활용한 자연어 기반 요약 생성</li>
              <li>PR 초안 자동 생성</li>
              <li>변경 로그 자동 생성</li>
            </ul>
        """.trimIndent())
    }

    buildSearchableOptions {
        enabled = false
    }

    // 플러그인 JAR 빌드 시 의존성 포함 설정
    jar {
        // JAR에 코루틴 라이브러리 포함
        from(configurations.getByName("includeInJar").map { if (it.isDirectory) it else zipTree(it) })
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
    
    buildPlugin {
        // 무조건 JAR를 새로 생성
        dependsOn(jar)
    }
    
    runIde {
        // IDE 실행 시 필요한 JVM 메모리 설정
        jvmArgs("-Xmx2g")
        // 코루틴 디버그 옵션
        jvmArgs("-Dkotlinx.coroutines.debug")
        // ASM 9 지원을 위한 옵션
        jvmArgs("-Dasm.all=9")
    }
    
    // 모든 빌드 캐시와 임시 파일 삭제를 위한 태스크
    register<Delete>("cleanAll") {
        group = "build"
        description = "모든 빌드 캐시와 임시 파일을 삭제합니다"
        delete(project.buildDir)
        delete("${rootDir}/idea-sandbox")
        delete("${rootDir}/.gradle")
    }
}

tasks.test {
    useJUnitPlatform()
}

