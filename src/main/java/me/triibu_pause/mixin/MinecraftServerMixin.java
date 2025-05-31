package me.triibu_pause.mixin;

import me.triibu_pause.PauseManager;
import me.triibu_pause.TriibuPauseConfig;
import me.triibu_pause.TriibuPause;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.ServerNetworkIo;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;

import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin implements PauseManager {
    
    @Shadow private PlayerManager playerManager;
    @Shadow public abstract boolean saveAll(boolean suppressLogs, boolean flush, boolean force);
    @Shadow @Nullable public abstract ServerNetworkIo getNetworkIo();

    @Unique
    private int idleTickCount = 0;
    @Unique
    private int pauseTicks = -1;
    @Unique
    private boolean isPauseEnabled = true;
    @Unique
    private int pauseWhenEmptySeconds = -1;

    @Inject(method = "<init>*", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        // Load config values when server starts
        refreshConfig();
    }
    @Unique
    public void refreshConfig() {
        pauseTicks = TriibuPauseConfig.getInstance().getPauseWhenEmptyTicks();
        isPauseEnabled = TriibuPauseConfig.getInstance().isEnablePauseWhenEmpty();
        pauseWhenEmptySeconds = TriibuPauseConfig.getInstance().getPauseWhenEmptySeconds();
    }


    /**
     * Inject into the tick method to implement pause when empty functionality
     * This mirrors how 1.21.4 implements the feature
     */
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onTick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        if (!isPauseEnabled) {
            return;
        }

        if (pauseTicks > 0) {
            if (this.playerManager.getCurrentPlayerCount() == 0) {
                this.idleTickCount++;
            } else {
                this.idleTickCount = 0;
            }

            // If we've been idle for the configured time, pause the server
            if (this.idleTickCount >= pauseTicks) {
                // Log a message only once when pausing
                if (this.idleTickCount == pauseTicks) {
                    TriibuPause.LOGGER.info("Server empty for {} seconds, pausing",
                            pauseWhenEmptySeconds);

                    // Run an autosave when pausing
                    this.saveAll(true, false, false);
                }
                
                // While paused, we still need to handle network connections
                this.getNetworkIo().tick();

                processDedicatedServerCommands();

                // Skip the rest of the tick
                ci.cancel();
            }
        }
    }

    /**
     * Process console commands on dedicated servers while paused
     */
    @Unique
    private void processDedicatedServerCommands() {
        if ((Object)this instanceof MinecraftDedicatedServer) {
            // Cast to MinecraftDedicatedServer and call executeQueuedCommands
            ((MinecraftDedicatedServer)(Object)this).executeQueuedCommands();
        }
    }
}