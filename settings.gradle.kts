rootProject.name = "Zenith"
include("auth-server")
include("auth-client")
include("whitelist")

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        maven("https://jitpack.io/")
        maven("https://maven.glass-launcher.net/babric")
    }
}