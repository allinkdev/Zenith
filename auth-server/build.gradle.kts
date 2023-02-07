plugins {
    id("net.minecrell.plugin-yml.bukkit") version "0.5.2"
}

dependencies {
    compileOnly("org.bukkit:project-poseidon:1.1.8-login")
}

bukkit {
    main = "me.allinkdev.zenith.authserver.Authentication"
    description = "Authentication component of Zenith (server-side)."
    author = "Allink"
}