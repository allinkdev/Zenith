package me.allinkdev.zenith.authclient;

import com.google.gson.Gson;
import lombok.Getter;
import me.allinkdev.zenith.authclient.request.Join;
import me.allinkdev.zenith.authclient.thread.MySessionCheckThread;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuthClient implements ModInitializer {
    public static final Logger logger = LoggerFactory.getLogger("AuthClient");
    @Getter
    private static final Gson gson = new Gson();
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    @Getter
    private static ActualSession session;

    public static void setSession(final ActualSession session) {
        AuthClient.session = session;

        final MySessionCheckThread sessionCheckThread = new MySessionCheckThread(session);
        executor.submit(sessionCheckThread);
    }

    public static ActualSession.AuthenticationState getCurrentAuthenticationState() {
        return session.getState();
    }

    public static boolean isAuthenticated() {
        final ActualSession.AuthenticationState state = getCurrentAuthenticationState();

        return state == ActualSession.AuthenticationState.AUTHENTICATED;
    }

    private static String getAuthorization(final ActualSession session) {
        final String accessToken = session.getAccessToken();

        return "Bearer " + accessToken;
    }

    public static void setAuthorization(final HttpURLConnection connection, final ActualSession session) {
        connection.setRequestProperty("Authorization", getAuthorization(session));
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
