plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":core"))
    implementation("io.modelcontextprotocol:kotlin-sdk:0.4.0")
    
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    testImplementation("io.mockk:mockk:1.13.5")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

// Java 17 기능 활용을 위한 추가 설정
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "17"
        apiVersion = "1.8"
        languageVersion = "1.8"
        freeCompilerArgs = listOf("-Xjsr305=strict")
    }
}