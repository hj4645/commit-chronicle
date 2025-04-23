plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    application
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
        archiveBaseName.set("commitchronicle-cli")
        archiveVersion.set("0.1.0")
        archiveClassifier.set("all")
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
