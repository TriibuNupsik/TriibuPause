package me.triibu_pause;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class PauseConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_FILE_NAME = "triibu_pause.json";
    private static PauseConfig INSTANCE;

    // Configuration options
    private int pauseWhenEmptySeconds = 1; // Default: 1 second (20 ticks)
    private boolean enablePauseWhenEmpty = true;

    public static PauseConfig getInstance() {
        if (INSTANCE == null) {
            load();
        }
        return INSTANCE;
    }

    public static void load() {
        Path configDir = FabricLoader.getInstance().getConfigDir().resolve(Triibu_pause.MOD_ID);
        try {
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }

            File configFile = configDir.resolve(CONFIG_FILE_NAME).toFile();
            if (configFile.exists()) {
                try (FileReader reader = new FileReader(configFile)) {
                    INSTANCE = GSON.fromJson(reader, PauseConfig.class);
                    Triibu_pause.LOGGER.info("Loaded config file for Triibu Pause");
                }
            } else {
                INSTANCE = new PauseConfig();
                save();
                Triibu_pause.LOGGER.info("Created default config for Triibu Pause");
            }
        } catch (IOException e) {
            Triibu_pause.LOGGER.error("Failed to load config for Triibu Pause", e);
            INSTANCE = new PauseConfig();
        }
    }

    public static void save() {
        Path configDir = FabricLoader.getInstance().getConfigDir().resolve(Triibu_pause.MOD_ID);
        try {
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }

            File configFile = configDir.resolve(CONFIG_FILE_NAME).toFile();
            try (FileWriter writer = new FileWriter(configFile)) {
                GSON.toJson(INSTANCE, writer);
                Triibu_pause.LOGGER.info("Saved config for Triibu Pause");
            }
        } catch (IOException e) {
            Triibu_pause.LOGGER.error("Failed to save config for Triibu Pause", e);
        }
    }

    // Getters and Setters
    public int getPauseWhenEmptySeconds() {
        return pauseWhenEmptySeconds;
    }

    public void setPauseWhenEmptySeconds(int pauseWhenEmptySeconds) {
        this.pauseWhenEmptySeconds = Math.max(1, pauseWhenEmptySeconds); // Minimum 1 second
    }

    public boolean isEnablePauseWhenEmpty() {
        return enablePauseWhenEmpty;
    }

    public void setEnablePauseWhenEmpty(boolean enablePauseWhenEmpty) {
        this.enablePauseWhenEmpty = enablePauseWhenEmpty;
    }

    public int getPauseWhenEmptyTicks() {
        return pauseWhenEmptySeconds * 20; // Convert seconds to ticks
    }
}
