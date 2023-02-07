package me.allinkdev.zenith.authclient.response;

import lombok.Data;

import java.util.Set;

@Data
public class Entitlements {
    private final Set<Object> items;
    private final Object signature;
    private final Object keyId;
}
