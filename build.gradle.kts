import java.util.Properties

plugins {
    kotlin("jvm") version "2.1.0" apply false
    kotlin("plugin.serialization") version "2.1.0" apply false
}

// Git 태그에서 버전 추출 또는 SNAPSHOT 사용
fun getProjectVersion(): String {
    return try {
        val process = ProcessBuilder("git", "describe", "--tags", "--exact-match", "HEAD")
            .directory(rootDir)
            .start()
        
        if (process.waitFor() == 0) {
            // 정확한 태그가 있는 경우 (v1.0.0 → 1.0.0)
            process.inputStream.bufferedReader().readText().trim().removePrefix("v")
        } else {
            // 태그가 없는 경우 SNAPSHOT
            "1.0.0-SNAPSHOT"
        }
    } catch (e: Exception) {
        "1.0.0-SNAPSHOT"
    }
}

allprojects {
    group = "com.commitchronicle"
    version = getProjectVersion()
    
    repositories {
        mavenCentral()
    }
}

subprojects {
    plugins.withId("org.jetbrains.kotlin.jvm") {
        configure<org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension> {
            jvmToolchain(8)
            sourceSets.all {
                languageSettings {
                    languageVersion = "1.6"
                    apiVersion = "1.6"
                }
            }
        }
        tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
            kotlinOptions {
                jvmTarget = "1.8"
                freeCompilerArgs = listOf("-Xjsr305=strict")
            }
        }
    }
}




