package me.allinkdev.zenith.authclient.mixin;

import me.allinkdev.zenith.authclient.ActualSession;
import me.allinkdev.zenith.authclient.AuthClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin extends Screen {
    private String getMessage() {
        final ActualSession.AuthenticationState state = AuthClient.getCurrentAuthenticationState();

        switch (state) {
            case CHECKING:
                return "Checking authentication status...";
            case AUTHENTICATED:
                return "Authenticated with Microsoft";
            case NOT_AUTHENTICATED:
                return "Not authenticated with Microsoft";
        }

        throw new IllegalStateException("Session authentication state wasn't what we expected!");
    }

    @Inject(method = "render", at = @At("TAIL"))
    public void onRender(final int j, final int f, final float par3, final CallbackInfo ci) {
        final String text = this.getMessage();
        final int textWidth = textRenderer.getWidth(text);
        final int width = this.width;
        final int x = (width - textWidth) - 2;

        this.drawStringWithShadow(textRenderer, text, x, 2, 5263440);
    }
}
