plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    application
    `maven-publish`
}

dependencies {
    implementation(project(":core:api"))
    implementation(project(":core:impl"))
    implementation("com.github.ajalt.clikt:clikt:4.2.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
}

tasks {
    shadowJar {
        archiveBaseName.set("commitchronicle")
        archiveVersion.set("0.1.0")
        archiveClassifier.set("")
        manifest {
            attributes["Main-Class"] = "com.commitchronicle.cli.MainKt"
        }
        mergeServiceFiles()
    }

    build {
        dependsOn(shadowJar)
    }
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "1.8"
        apiVersion = "1.8"
        languageVersion = "1.8"
    }
}

application {
    mainClass.set("com.commitchronicle.cli.MainKt")
}

// Publishing configuration for JitPack
publishing {
    publications {
        create<MavenPublication>("maven") {
            artifact(tasks.shadowJar)

            artifacts.removeAll { it.classifier == null }
            artifact(tasks.shadowJar) {
                classifier = null
            }

            groupId = "com.github.hj4645"
            artifactId = "commit-chronicle"
            version = "0.1.0"

            pom {
                name.set("Commit Chronicle")
                description.set("🚀 AI-powered Git commit analysis and summarization tool with GitHub template support. Generate PR drafts, changelogs, and commit summaries using OpenAI, Claude, Gemini, and more.")
                url.set("https://github.com/hj4645/commit-chronicle")

                properties.set(mapOf(
                    "maven.compiler.source" to "8",
                    "maven.compiler.target" to "8",
                    "project.build.sourceEncoding" to "UTF-8",
                    "kotlin.version" to "1.8.0"
                ))

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                        distribution.set("repo")
                    }
                }

                developers {
                    developer {
                        id.set("hj4645")
                        name.set("HJPark")
                        email.set("hjpark4645@gmail.com")
                        url.set("https://github.com/hj4645")
                        roles.set(listOf("Developer", "Maintainer"))
                        timezone.set("Asia/Seoul")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/hj4645/commit-chronicle.git")
                    developerConnection.set("scm:git:ssh://git@github.com:hj4645/commit-chronicle.git")
                    url.set("https://github.com/hj4645/commit-chronicle")
                    tag.set("v0.1.0")
                }

                issueManagement {
                    system.set("GitHub Issues")
                    url.set("https://github.com/hj4645/commit-chronicle/issues")
                }

                ciManagement {
                    system.set("GitHub Actions")
                    url.set("https://github.com/hj4645/commit-chronicle/actions")
                }
            }
        }
    }
}
