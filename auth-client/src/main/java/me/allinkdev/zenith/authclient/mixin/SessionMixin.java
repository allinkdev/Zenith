package me.allinkdev.zenith.authclient.mixin;

import me.allinkdev.zenith.authclient.ActualSession;
import me.allinkdev.zenith.authclient.AuthClient;
import net.minecraft.client.util.Session;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Session.class)
public class SessionMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    public void onSessionInit(final String username, final String token, final CallbackInfo ci) {
        final String tokenReplaced = token.replace("token:", "");
        final String[] parts = tokenReplaced.split(":");

        if (parts.length != 2) {
            AuthClient.setSession(new ActualSession(username, "", ""));
            return;
        }

        final ActualSession actualSession = new ActualSession(username, parts[0], parts[1]);

        AuthClient.setSession(actualSession);
    }
}
