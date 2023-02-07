plugins {
    id("java")
    id("checkstyle")
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("io.freefair.lombok") version "6.6.1"
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "checkstyle")
    apply(plugin = "com.github.johnrengelman.shadow")
    apply(plugin = "io.freefair.lombok")

    java.sourceCompatibility = JavaVersion.VERSION_17

    tasks {
        assemble {
            dependsOn(checkstyleMain, shadowJar)
        }
    }

    repositories {
        mavenCentral()
        mavenLocal()
    }
}