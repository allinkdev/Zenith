package me.allinkdev.zenith.authclient.mixin;

import com.google.gson.Gson;
import me.allinkdev.zenith.authclient.AuthClient;
import me.allinkdev.zenith.authclient.request.Join;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.Connection;
import net.minecraft.packet.handshake.HandshakePacket;
import net.minecraft.packet.login.LoginRequestPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {
    private static final String EXCEPTIONAL_DISCONNECT = "disconnect.loginFailedInfo";
    private static final int PROTOCOL_VERSION = 14;
    @Shadow
    private Connection connection;

    /**
     * @author Allink
     * @reason Implement Microsoft authentication
     */
    @Overwrite
    public void handleHandshake(final HandshakePacket packet) {
        final String username = AuthClient.getUsername();
        final String serverId = packet.str;

        if (!AuthClient.isAuthenticated() || serverId.equals("-")) {
            this.connection.sendPacket(new LoginRequestPacket(username, PROTOCOL_VERSION));
            return;
        }

        final URL url;

        try {
            url = new URL("https://sessionserver.mojang.com/session/minecraft/join");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            this.connection.disconnect(EXCEPTIONAL_DISCONNECT, "Malformed URL");
            return;
        }

        final HttpURLConnection urlConnection;

        try {
            urlConnection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
            this.connection.disconnect(EXCEPTIONAL_DISCONNECT, "Failed to open URL connection");
            return;
        }

        try {
            urlConnection.setRequestMethod("POST");
        } catch (ProtocolException e) {
            e.printStackTrace();
            this.connection.disconnect(EXCEPTIONAL_DISCONNECT, "Failed to set URL connection method to POST");
            return;
        }

        AuthClient.setAuthorization(urlConnection);
        urlConnection.setRequestProperty("Content-Type", "application/json");
        urlConnection.setDoOutput(true);
        urlConnection.setDoInput(true);

        final Join join = AuthClient.constructJoin(serverId);
        final Gson gson = AuthClient.getGson();
        final String json = gson.toJson(join);
        final OutputStream outputStream;

        try {
            urlConnection.connect();
        } catch (IOException e) {
            e.printStackTrace();
            this.connection.disconnect(EXCEPTIONAL_DISCONNECT, "Failed to connect to session server");
            return;
        }

        try {
            outputStream = urlConnection.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
            this.connection.disconnect(EXCEPTIONAL_DISCONNECT, "Failed to open output stream");
            return;
        }

        final OutputStreamWriter writer = new OutputStreamWriter(outputStream);

        try {
            writer.append(json);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            this.connection.disconnect(EXCEPTIONAL_DISCONNECT, "Failed to write to output stream");
            return;
        }

        final int responseCode;

        try {
            responseCode = urlConnection.getResponseCode();
        } catch (IOException e) {
            e.printStackTrace();
            this.connection.disconnect(EXCEPTIONAL_DISCONNECT, "Failed to get response code");
            return;
        }

        if (responseCode == 204) {
            this.connection.sendPacket(new LoginRequestPacket(username, PROTOCOL_VERSION));
            return;
        }

        final InputStream responseStream;
        try {
            responseStream = urlConnection.getErrorStream();
        } catch (Exception e) {
            e.printStackTrace();
            this.connection.disconnect(EXCEPTIONAL_DISCONNECT, "Failed to get response input stream");
            return;
        }

        final InputStreamReader inputStreamReader = new InputStreamReader(responseStream);
        final BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        final StringBuilder response = new StringBuilder();

        try {
            String inputLine;

            while ((inputLine = bufferedReader.readLine()) != null) {
                response.append(inputLine);
            }

            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
            this.connection.disconnect(EXCEPTIONAL_DISCONNECT, "Failed to read response input stream");
            return;
        }

        AuthClient.logger.warning(response.toString());

        this.connection.disconnect(EXCEPTIONAL_DISCONNECT, responseCode);
    }
}
