package me.allinkdev.zenith.authclient;

import lombok.Data;
import me.allinkdev.zenith.authclient.request.Join;

@Data
public class ActualSession {
    private final String username;
    private final String accessToken;
    private final String profileId;

    public Join constructJoin(final String serverId) {
        return new Join(accessToken, profileId, serverId);
    }
}
