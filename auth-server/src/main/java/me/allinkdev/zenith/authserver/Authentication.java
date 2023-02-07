package me.allinkdev.zenith.authserver;

import com.legacyminecraft.poseidon.event.PlayerSendPacketEvent;
import net.minecraft.server.Packet;
import net.minecraft.server.Packet2Handshake;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class Authentication extends JavaPlugin implements Listener {
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final String HAS_JOINED_URL = "https://sessionserver.mojang.com/session/minecraft/hasJoined?username=%s&serverId=%s&ip=%s";
    private static final Logger logger = Logger.getLogger("Authentication");
    private static final Map<InetAddress, String> serverIds = new HashMap<>();
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final String[] SERVER_ID_CHARSET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890".split("");
    private static final int SERVER_ID_CHARSET_LENGTH = SERVER_ID_CHARSET.length;

    @Override
    public void onDisable() {
        // We don't need to remove anything on disable
    }

    @Override
    public void onEnable() {
        final PluginManager pluginManager = Bukkit.getPluginManager();

        pluginManager.registerEvents(this, this);
    }

    @EventHandler(priority = Event.Priority.Highest)
    public void onLoginPacket(final PlayerSendPacketEvent event) {
        final Packet packet = event.getPacket();

        if (!(packet instanceof Packet2Handshake)) {
            return;
        }

        final InetAddress address = event.getAddress();
        final String serverId = getServerId(address);
        final Packet2Handshake newPacket = new Packet2Handshake(serverId);

        event.setPacket(newPacket);
    }

    private void onPlayerChangeConnectionState(final PlayerEvent event) {
        final Player player = event.getPlayer();
        final InetSocketAddress socketAddress = player.getAddress();
        final InetAddress address = socketAddress.getAddress();

        serverIds.remove(address);
    }

    @EventHandler(priority = Event.Priority.Highest)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        onPlayerChangeConnectionState(event);
    }

    @EventHandler(priority = Event.Priority.Highest)
    public void onPlayerQuit(final PlayerQuitEvent event) {
        onPlayerChangeConnectionState(event);
    }

    @EventHandler(priority = Event.Priority.Highest)
    public void onPreLogin(final PlayerPreLoginEvent event) {
        final String name = event.getName();
        final InetAddress address = event.getAddress();
        final String host = address.getHostAddress();
        final String serverId = getServerId(address);
        final String url = String.format(HAS_JOINED_URL, name, serverId, host);
        final URI uri = URI.create(url);
        final HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(uri)
                .build();
        final HttpResponse<Void> response;

        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
        } catch (Exception e) {
            event.setResult(PlayerPreLoginEvent.Result.KICK_OTHER);
            event.setKickMessage("Error communicating with Session Server!");
            logger.warning("Error communicating with Session Server!");
            e.printStackTrace();

            if (e instanceof InterruptedException) {
                final Thread currentThread = Thread.currentThread();

                currentThread.interrupt();
            }

            return;
        }

        if (response.statusCode() == 200) {
            logger.info(() -> name + " (" + host + ")" + " authenticated successfully!");
            return;
        }

        logger.warning(() -> name + " (" + host + ")" + " failed to authenticate!");
        event.setResult(PlayerPreLoginEvent.Result.KICK_OTHER);
        event.setKickMessage("Failed to authenticate with Session Server!");
    }

    private String generateServerId() {
        final StringBuilder builder = new StringBuilder();

        for (int i = 0; i < 32; i++) {
            final int choice = secureRandom.nextInt(0, SERVER_ID_CHARSET_LENGTH);
            final String character = SERVER_ID_CHARSET[choice];

            builder.append(character);
        }

        return builder.toString();
    }

    private String getServerId(final InetAddress inetAddress) {
        if (!serverIds.containsKey(inetAddress)) {
            final String generated = generateServerId();
            serverIds.put(inetAddress, generated);

            return generated;
        }

        return serverIds.get(inetAddress);
    }
}
