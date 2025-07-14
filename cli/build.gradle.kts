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
            // Use shadowJar as the main artifact
            artifact(tasks.shadowJar)
            
            groupId = "com.commitchronicle"
            artifactId = "commit-chronicle"
            version = "0.1.0"
            
            pom {
                name.set("Commit Chronicle")
                description.set("AI-powered Git commit analysis and summarization library")
                url.set("https://github.com/eulji/commit-chronicle")
                
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                
                developers {
                    developer {
                        id.set("eulji")
                        name.set("Eulji")
                    }
                }
                
                scm {
                    connection.set("scm:git:git://github.com/eulji/commit-chronicle.git")
                    developerConnection.set("scm:git:ssh://github.com/eulji/commit-chronicle.git")
                    url.set("https://github.com/eulji/commit-chronicle")
                }
            }
        }
    }
}
