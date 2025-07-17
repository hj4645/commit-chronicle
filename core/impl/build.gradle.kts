plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    implementation(project(":core:api"))

    // Git
    implementation("org.eclipse.jgit:org.eclipse.jgit:6.7.0.202309050840-r")

    // Ktor - 최소 의존성만 사용
    implementation("io.ktor:ktor-client-core:2.3.8")
    implementation("io.ktor:ktor-client-cio:2.3.8") {
        exclude(group = "io.ktor", module = "ktor-websockets")
        exclude(group = "io.ktor", module = "ktor-network-tls")
    }
    implementation("io.ktor:ktor-client-content-negotiation:2.3.8")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.8")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")

    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
    
    // SLF4J NOP to suppress warnings
    implementation("org.slf4j:slf4j-nop:1.7.36")
}