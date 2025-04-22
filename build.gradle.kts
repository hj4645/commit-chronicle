plugins {
    kotlin("jvm") version "1.9.22" apply false
}

allprojects {
    group = "com.commitchronicle"
    version = "0.1.0"
    
    repositories {
        mavenCentral()
    }
}

subprojects {
    // 각 서브프로젝트는 자체 build.gradle.kts에서 필요한 플러그인을 적용
}
