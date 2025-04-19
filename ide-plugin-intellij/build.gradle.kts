plugins {
    kotlin("jvm")
    id("org.jetbrains.intellij") version "1.17.4"
}

dependencies {
    implementation(project(":core"))

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
}

intellij {
    version.set("2023.3")
    type.set("IC")
    plugins.set(listOf("Git4Idea"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.patchPluginXml {
    version.set(project.version.toString())
    sinceBuild.set("231")
    untilBuild.set("241.*")
}

//kotlin {
//    jvmToolchain(11)
//}
