package me.allinkdev.zenith.whitelist;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPreLoginEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class Whitelist extends JavaPlugin implements Listener, CommandExecutor {
    private static final Logger logger = Logger.getLogger("Whitelist");
    private static final Set<String> WHITELISTED_PLAYERS = new HashSet<>();
    private static final String PERMISSION_PREFIX = "whitelist.command";
    private static final String COMMAND_SYNTAX = "Syntax: " + ChatColor.RED + " /whitelist (add|remove) <player>\n/whitelist (enable|disable)";
    private File whitelistFile;
    private Path whitelistFilePath;
    private File configFile;
    private Path configFilePath;
    private Configuration configuration;

    @Override
    public void onDisable() {
        // ...
    }

    @Override
    public void onLoad() {
        final File dataFolder = getDataFolder();
        final Path dataFolderPath = dataFolder.toPath();

        if (!dataFolder.exists()) {
            final boolean created = dataFolder.mkdirs();

            if (!created) {
                throw new IllegalStateException("created is false!");
            }
        }

        whitelistFilePath = dataFolderPath.resolve("white-list.txt");
        whitelistFile = whitelistFilePath.toFile();
        configFilePath = dataFolderPath.resolve("config.yml");
        configFile = configFilePath.toFile();
    }

    private void loadWhitelist() {
        if (!whitelistFile.exists()) {
            try {
                Files.createFile(whitelistFilePath);
            } catch (IOException e) {
                logger.warning("Failed to create whitelist file!");
                e.printStackTrace();
                return;
            }
        }

        final List<String> contents;

        try {
            contents = Files.readAllLines(whitelistFilePath);
        } catch (IOException e) {
            logger.warning("Failed to read whitelist!");
            e.printStackTrace();
            return;
        }

        WHITELISTED_PLAYERS.clear();
        WHITELISTED_PLAYERS.addAll(contents);
    }

    @Override
    public void onEnable() {
        loadWhitelist();

        if (!configFile.exists()) {
            try {
                saveDefaultConfig();
            } catch (IOException e) {
                logger.warning("Failed to save default config!");
                e.printStackTrace();
                return;
            }
        }

        configuration = getConfiguration();
        configuration.load();

        final PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(this, this);

        final PluginCommand whitelistCommand = Bukkit.getPluginCommand("whitelist");
        whitelistCommand.setExecutor(this);
    }

    private void save() throws IOException {
        final String asString = String.join("\n", WHITELISTED_PLAYERS);

        Files.writeString(whitelistFilePath, asString);
    }

    private void saveDefaultConfig() throws IOException {
        final Class<? extends Whitelist> clazz = getClass();
        final ClassLoader classLoader = clazz.getClassLoader();
        final InputStream resourceStream = classLoader.getResourceAsStream("config.yml");

        if (resourceStream == null) {
            throw new IOException("Could not find default configuration resource!");
        }

        Files.copy(resourceStream, configFilePath);
    }

    @EventHandler(priority = Event.Priority.Highest)
    public void onPreLogin(final PlayerPreLoginEvent event) {
        final boolean enabled = (Boolean) configuration.getProperty("enabled");

        if (!enabled) {
            return;
        }

        final String username = event.getName();

        if (WHITELISTED_PLAYERS.contains(username)) {
            logger.info(() -> username + " is whitelisted!");
            return;
        }

        logger.warning(() -> username + " is not whitelisted, denying!");
        event.setResult(PlayerPreLoginEvent.Result.KICK_WHITELIST);
        event.setKickMessage("Action not allowed.");
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        if (!sender.hasPermission(PERMISSION_PREFIX)) {
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(COMMAND_SYNTAX);
            return false;
        }

        final String subCommand = args[0].toLowerCase();
        final String subCommandPerm = PERMISSION_PREFIX + "." + subCommand;

        if (!sender.hasPermission(subCommandPerm)) {
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "enable":
                configuration.setProperty("enabled", true);
                configuration.save();
                loadWhitelist();
                sender.sendMessage("Whitelist enabled.");
                break;
            case "disable":
                configuration.setProperty("disabled", false);
                configuration.save();
                sender.sendMessage("Whitelist disabled.");
                break;
            case "add":
                if (args.length < 2) {
                    sender.sendMessage(COMMAND_SYNTAX);
                    return true;
                }

                final String who = args[1];
                WHITELISTED_PLAYERS.add(who);

                try {
                    save();
                } catch (IOException e) {
                    e.printStackTrace();
                    sender.sendMessage(ChatColor.RED + "Failed to save whitelist. Check logs for more details.");
                    return true;
                }

                sender.sendMessage("Added " + who + " to the whitelist.");
                break;
            case "remove":
                if (args.length < 2) {
                    sender.sendMessage(COMMAND_SYNTAX);
                    return true;
                }

                final String whoRemove = args[1];
                final boolean contains = WHITELISTED_PLAYERS.remove(whoRemove);

                try {
                    save();
                } catch (IOException e) {
                    e.printStackTrace();
                    sender.sendMessage(ChatColor.RED + "Failed to save whitelist. Check logs for more details.");
                    return true;
                }

                if (contains) {
                    sender.sendMessage("Removed " + whoRemove + " from the whitelist.");
                    break;
                }

                sender.sendMessage(whoRemove + " was not in the whitelist!");
                break;
            default:
                sender.sendMessage(COMMAND_SYNTAX);
                return false;
        }

        return true;
    }
}
