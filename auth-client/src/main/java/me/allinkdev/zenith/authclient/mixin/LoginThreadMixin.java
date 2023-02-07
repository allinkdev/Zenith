package me.allinkdev.zenith.authclient.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(Minecraft.LoginThread.class)
public class LoginThreadMixin {
    /**
     * @author Allink
     * @reason Prevent query to non-existent login server
     */
    @Overwrite
    public void run() {
        // ...
    }
}
