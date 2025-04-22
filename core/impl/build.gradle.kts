plugins {
    kotlin("jvm")
}

val ktor_version = "2.3.7"

dependencies {
    implementation(project(":core:api"))
    implementation("org.eclipse.jgit:org.eclipse.jgit:6.7.0.202309050840-r")
    implementation("com.aallam.openai:openai-client:3.7.0")

    implementation("io.ktor:ktor-client-core:${ktor_version}")
    implementation("io.ktor:ktor-client-cio:${ktor_version}")
    implementation("io.ktor:ktor-serialization-jackson-jvm:${ktor_version}")
    implementation("io.ktor:ktor-client-content-negotiation-jvm:${ktor_version}")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
}

kotlin {
    jvmToolchain(8)
} 