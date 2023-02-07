import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    id("net.minecrell.plugin-yml.bukkit") version "0.5.2"
}

dependencies {
    compileOnly("org.bukkit:project-poseidon:1.1.8-login")
}

bukkit {
    main = "me.allinkdev.zenith.whitelist.Whitelist"
    description = "Whitelist component of Zenith."
    author = "Allink"

    val command = BukkitPluginDescription.Command("whitelist");
    command.description = "Allows modification of the Zenith whiteist."
    command.permission = "whitelist.command"
    command.usage = "/whitelist (add|remove) <player>\n/whitelist (enable|disable)"

    commands.add(command)

    val permissionPrefix = "whitelist.command"
    val permission = BukkitPluginDescription.Permission(permissionPrefix)
    permission.default = BukkitPluginDescription.Permission.Default.OP
    permission.description = "Permission to use the /whitelist command."
    permissions.add(permission)

    val subcommands = arrayOf("add", "remove", "enable", "disable")

    for (subcommand in subcommands) {
        val subcommandPermission = BukkitPluginDescription.Permission("$permissionPrefix.$subcommand")
        subcommandPermission.default = BukkitPluginDescription.Permission.Default.OP
        subcommandPermission.description = "Permission to use the /whitelist $subcommand subcommand. "
        permissions.add(subcommandPermission)
    }
}