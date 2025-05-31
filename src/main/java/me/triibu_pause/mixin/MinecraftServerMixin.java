package me.triibu_pause.mixin;

import me.triibu_pause.PauseConfig;
import me.triibu_pause.Triibu_pause;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;
import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    
    @Shadow private PlayerManager playerManager;
    
    @Unique
    private int idleTickCount = 0;
    
    @Unique
    private boolean hasLoggedPause = false;
    
    /**
     * Get the configured pause interval in ticks
     */
    @Unique
    private int getPauseWhenEmptyTicks() {
        return PauseConfig.getInstance().getPauseWhenEmptyTicks();
    }
    
    /**
     * Check if pause when empty is enabled
     */
    @Unique
    private boolean isPauseEnabled() {
        return PauseConfig.getInstance().isEnablePauseWhenEmpty();
    }
    
    /**
     * Inject into the tick method to implement pause when empty functionality
     * This mirrors how 1.21.4 implements the feature
     */
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onTick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        // Only apply pause logic if the feature is enabled
        if (!isPauseEnabled()) {
            return;
        }
        
        int pauseTicks = getPauseWhenEmptyTicks();
        if (pauseTicks > 0) {
            // Check if server is empty
            if (this.playerManager.getCurrentPlayerCount() == 0) {
                this.idleTickCount++;
            } else {
                this.idleTickCount = 0;
                this.hasLoggedPause = false;
            }

            // If we've been idle for the configured time, pause the server
            if (this.idleTickCount >= pauseTicks) {
                // Log a message only once when pausing
                if (!this.hasLoggedPause) {
                    Triibu_pause.LOGGER.info("Server empty for {} seconds, pausing", 
                            PauseConfig.getInstance().getPauseWhenEmptySeconds());
                    this.hasLoggedPause = true;
                    
                    // Run an autosave when pausing
                    runAutosave();
                }
                
                // While paused, we still need to handle network connections
                tickNetworkIo();
                
                // Skip the rest of the tick
                ci.cancel();
            }
        }
    }
    
    /**
     * Minimal network IO handling while paused
     */
    @Unique
    private void tickNetworkIo() {
        // Cast to MinecraftServer to call the method
        Objects.requireNonNull(((MinecraftServer) (Object) this).getNetworkIo()).tick();
    }
    
    /**
     * Run an autosave when pausing
     */
    @Unique
    private void runAutosave() {
        MinecraftServer server = (MinecraftServer)(Object)this;
        server.save(true, false, false);
    }
}