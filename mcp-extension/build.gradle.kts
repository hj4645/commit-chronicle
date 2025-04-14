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

//kotlin {
//    jvmToolchain(17)
//}