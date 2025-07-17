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
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3") {
        exclude(group = "org.jetbrains.kotlin")  // Kotlin 의존성 중복 제거
    }

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
}

tasks {
    shadowJar {
        archiveBaseName.set("commitchronicle")
        archiveVersion.set(project.version.toString())
        archiveClassifier.set("")
        manifest {
            attributes["Main-Class"] = "com.commitchronicle.cli.MainKt"
        }
        
        // JAR 최적화 설정
        minimize()  // 사용하지 않는 클래스 제거
        mergeServiceFiles()  // 서비스 파일 병합
        
        // 중복 의존성 제거
        exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
        
        // 프로퍼티 파일 통합
        relocate("META-INF/services", "services")
        relocate("META-INF/spring.*", "spring")
        
        // 트랜지티브 의존성 최적화
        dependencies {
            exclude(dependency("org.jetbrains.kotlin:kotlin-stdlib"))
            exclude(dependency("org.jetbrains.kotlin:kotlin-stdlib-common"))
        }
    }
    
    // 버전 정보 자동 업데이트 태스크
    register("updateVersion") {
        doLast {
            val versionFile = file("${project.buildDir}/version.txt")
            versionFile.parentFile.mkdirs()
            versionFile.writeText(project.version.toString())
        }
    }
    
    // 빌드 시 버전 정보 자동 업데이트
    build {
        dependsOn("updateVersion")
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

// Configure publishing for JitPack
publishing {
    publications {
        create<MavenPublication>("maven") {
            artifact(tasks.shadowJar)

            groupId = "com.github.hj4645"
            artifactId = "commit-chronicle"
            version = project.version.toString()

            pom {
                name.set("Commit Chronicle")
                description.set("🚀 AI-powered Git commit analysis and summarization tool with GitHub template support. Generate PR drafts and commit summaries")
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
                    tag.set("v${project.version}")
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
