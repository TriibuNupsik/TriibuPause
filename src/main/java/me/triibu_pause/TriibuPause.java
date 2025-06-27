/*
 * Copyright (c) 2025. Triibunupsik
 * SPDX-License-Identifier: Apache-2.0
 */

package me.triibu_pause;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TriibuPause implements ModInitializer {
    public static final String MOD_ID = "AutoPause";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.debug("Initializing Auto Pause");

        TriibuPauseConfig.load();

        if (TriibuPauseConfig.getInstance().getEnablePauseWhenEmpty()) {
            LOGGER.info("Auto Pause is enabled. Server will pause after {} seconds when no players are online.",
                    TriibuPauseConfig.getInstance().getPauseWhenEmptySeconds());
        } else {
            LOGGER.info("Auto Pause is disabled.");
        }

        // Register command
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                TriibuPauseCommand.register(dispatcher));
    }
}
