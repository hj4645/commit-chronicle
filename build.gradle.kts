plugins {
    kotlin("jvm") version "1.9.22" apply false
}

allprojects {
    group = "com.commitchronicle"
    version = "0.1.0"
    
    repositories {
        mavenCentral()
    }

    apply(plugin = "org.jetbrains.kotlin.jvm")
    
}
