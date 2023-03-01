package me.allinkdev.zenith.authclient.mixin;

import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(Minecraft.SessionCheckThread.class)
public class SessionCheckThreadMixin {
    private static final Logger logger = LoggerFactory.getLogger(SessionCheckThreadMixin.class);

    /**
     * @author Allink
     * @reason Prevent query to non-existent login server
     */
    @Overwrite
    public void run() {
        logger.info("Blocked run of Notchian session check thread!");
    }
}
