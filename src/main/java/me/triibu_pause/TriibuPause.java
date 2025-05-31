package me.triibu_pause;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TriibuPause implements ModInitializer {
    public static final String MOD_ID = "TriibuPause";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Triibu Pause When Empty");
        // Load config
        TriibuPauseConfig.load();

        if (TriibuPauseConfig.getInstance().getEnablePauseWhenEmpty()) {
            LOGGER.info("Pause when empty is enabled. Server will tick once every {} seconds when no players are online.",
                    TriibuPauseConfig.getInstance().getPauseWhenEmptySeconds());
        } else {
            LOGGER.info("Pause when empty is disabled.");
        }

        // Register command
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                TriibuPauseCommand.register(dispatcher));
    }
}
