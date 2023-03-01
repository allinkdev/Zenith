package me.allinkdev.zenith.authclient.thread;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import me.allinkdev.zenith.authclient.ActualSession;
import me.allinkdev.zenith.authclient.AuthClient;
import me.allinkdev.zenith.authclient.response.Entitlements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Set;

@RequiredArgsConstructor
public class MySessionCheckThread extends Thread {
    private static final Logger logger = LoggerFactory.getLogger("Session Check Thread");
    private final Gson gson = new Gson();
    private final ActualSession session;

    @Override
    public void run() {
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

        AuthClient.setAuthorization(connection, session);

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
            logger.warn("Failed to authenticate with Microsoft!");
            session.setState(ActualSession.AuthenticationState.NOT_AUTHENTICATED);
            return;
        }

        logger.info("Authenticated with Microsoft!");
        session.setState(ActualSession.AuthenticationState.AUTHENTICATED);
    }
}
