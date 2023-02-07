package me.allinkdev.zenith.authclient;

import com.google.gson.Gson;
import lombok.Getter;
import me.allinkdev.zenith.authclient.request.Join;
import me.allinkdev.zenith.authclient.response.Entitlements;
import net.fabricmc.api.ModInitializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.logging.Logger;

public class AuthClient implements ModInitializer {
    public static final Logger logger = Logger.getLogger("AuthClient");
    @Getter
    private static final Gson gson = new Gson();
    private static ActualSession session;
    @Getter
    private static boolean authenticated = false;

    public static void setSession(final ActualSession session) {
        AuthClient.session = session;
        authenticated = false;

        final URL url;

        try {
            url = new URL("https://api.minecraftservices.com/entitlements/mcstore");
        } catch (MalformedURLException exception) {
            return;
        }

        final HttpURLConnection connection;

        try {
            connection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            return;
        }

        setAuthorization(connection);

        final InputStream responseStream;

        try {
            responseStream = connection.getInputStream();
        } catch (IOException e) {
            return;
        }

        final Reader reader = new InputStreamReader(responseStream, StandardCharsets.UTF_8);
        final Entitlements entitlements = gson.fromJson(reader, Entitlements.class);
        final Set<Object> items = entitlements.getItems();

        if (items.isEmpty()) {
            authenticated = false;
            return;
        }

        logger.info("Authenticated with Microsoft!");
        authenticated = true;
    }

    public static String getAuthorization() {
        final String accessToken = session.getAccessToken();

        return "Bearer " + accessToken;
    }

    public static void setAuthorization(final HttpURLConnection connection) {
        connection.setRequestProperty("Authorization", getAuthorization());
    }

    public static String getUsername() {
        return session.getUsername();
    }

    public static Join constructJoin(final String serverId) {
        return session.constructJoin(serverId);
    }

    @Override
    public void onInitialize() {
        // We don't need to initialise anything, so we just don't do anything here
    }
}
