plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.17.1"
}

group = "com.commitchronicle"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":core:api"))
    implementation(project(":core:impl"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
}

intellij {
    version.set("2023.2")
    type.set("IC")
    plugins.set(listOf("Git4Idea"))
}

tasks {
    patchPluginXml {
        sinceBuild.set("232")
        untilBuild.set("242.*")
    }
}

tasks.test {
    useJUnitPlatform()
}