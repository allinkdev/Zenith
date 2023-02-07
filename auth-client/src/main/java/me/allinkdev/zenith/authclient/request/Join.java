package me.allinkdev.zenith.authclient.request;

import lombok.Data;

@Data
public class Join {
    private final String accessToken;
    private final String selectedProfile;
    private final String serverId;
}
