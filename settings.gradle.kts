rootProject.name = "Zenith"
include("auth-server")
include("auth-client")

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        maven("https://jitpack.io/")
    }

    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "fabric-loom" && !requested.version?.endsWith("-SNAPSHOT")!!) {
                if (requested.id.id == "fabric-loom" && !requested.version?.contains("+")!!) {
                    useModule("com.github.Chocohead.Fabric-Loom:fabric-loom:${requested.version}")
                } else if (requested.id.id == "fabric-loom") {
                    useModule("net.fabricmc:fabric-loom:${requested.version}")
                }
            }
        }
    }
}
include("whitelist")
