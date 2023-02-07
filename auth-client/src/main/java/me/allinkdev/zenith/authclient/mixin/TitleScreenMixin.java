package me.allinkdev.zenith.authclient.mixin;

import me.allinkdev.zenith.authclient.AuthClient;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin extends Screen {
    @Inject(method = "render", at = @At("TAIL"))
    public void onRender(final int j, final int f, final float par3, final CallbackInfo ci) {
        final boolean authenticated = AuthClient.isAuthenticated();
        final String text = (authenticated ? "A" : "Not a") + "uthenticated with Microsoft";
        final int textWidth = this.textManager.getTextWidth(text);
        final int width = this.width;
        final int x = (width - textWidth) - 2;

        this.drawTextWithShadow(this.textManager, text, x, 2, 5263440);
    }
}
