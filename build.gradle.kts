plugins {
    kotlin("jvm") version "2.1.0" apply false
}

allprojects {
    group = "com.commitchronicle"
    version = "0.1.0"
    
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
