package me.triibu_pause.mixin;

import me.triibu_pause.PauseConfig;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    @Unique
    private int idleTickCount = 0;

    /**
     * Checks if the server should be paused (no players connected).
     */
    @Unique
    private boolean isPaused() {
        return PauseConfig.getInstance().isEnablePauseWhenEmpty() &&
                ((MinecraftServer)(Object)this).getCurrentPlayerCount() == 0;
    }

    /**
     * Injects into the shouldKeepTicking method to control server ticking when empty.
     */
    @Inject(method = "shouldKeepTicking", at = @At("HEAD"), cancellable = true)
    private void onShouldKeepTicking(CallbackInfoReturnable<Boolean> cir) {
        // If the server is paused (no players) and we've been idle for a while, skip ticks
        if (isPaused()) {
            idleTickCount++;
            // Only tick once every 20 ticks (1 second) when the server is empty
            if (idleTickCount >= 20) {
                idleTickCount = 0;
            } else {
                cir.setReturnValue(false);
            }
        } else {
            idleTickCount = 0;
        }
    }

    /**
     * Resets the idle tick count when players join the server.
     */
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if (!isPaused()) {
            idleTickCount = 0;
        }
    }
}
