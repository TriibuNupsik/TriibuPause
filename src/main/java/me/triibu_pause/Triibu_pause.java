package me.triibu_pause;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Triibu_pause implements ModInitializer {
    public static final String MOD_ID = "triibu_pause";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Triibu Pause When Empty");
        // Load config
        PauseConfig.load();

        if (PauseConfig.getInstance().isEnablePauseWhenEmpty()) {
            LOGGER.info("Pause when empty is enabled. Server will tick once every {} seconds when no players are online.",
                    PauseConfig.getInstance().getPauseWhenEmptySeconds());
        } else {
            LOGGER.info("Pause when empty is disabled.");
        }

        // Register command
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                PauseCommand.register(dispatcher));
    }
}
