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

public class TriibuPauseConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_FILE_NAME = "triibu-pause.json";
    private static TriibuPauseConfig INSTANCE;

    // Configuration options
    private int pauseWhenEmptySeconds = 60; // Default: 1 second (20 ticks)
    private boolean enablePauseWhenEmpty = true;

    public static TriibuPauseConfig getInstance() {
        if (INSTANCE == null) {
            load();
        }
        return INSTANCE;
    }

    public static void load() {
        Path configDir = FabricLoader.getInstance().getConfigDir();
        try {
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }

            File configFile = configDir.resolve(CONFIG_FILE_NAME).toFile();
            if (configFile.exists()) {
                try (FileReader reader = new FileReader(configFile)) {
                    INSTANCE = GSON.fromJson(reader, TriibuPauseConfig.class);
                    TriibuPause.LOGGER.info("Loaded config file for Triibu Pause");
                }
            } else {
                INSTANCE = new TriibuPauseConfig();
                save();
                TriibuPause.LOGGER.info("Created default config for Triibu Pause");
            }
        } catch (IOException e) {
            TriibuPause.LOGGER.error("Failed to load config for Triibu Pause", e);
            INSTANCE = new TriibuPauseConfig();
        }
    }

    public static void save() {
        Path configDir = FabricLoader.getInstance().getConfigDir();
        try {
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }

            File configFile = configDir.resolve(CONFIG_FILE_NAME).toFile();
            try (FileWriter writer = new FileWriter(configFile)) {
                GSON.toJson(INSTANCE, writer);
                TriibuPause.LOGGER.info("Saved config for Triibu Pause");
            }
        } catch (IOException e) {
            TriibuPause.LOGGER.error("Failed to save config for Triibu Pause", e);
        }
    }

    // Getters and Setters
    public  boolean getEnablePauseWhenEmpty() {
        return enablePauseWhenEmpty;
    }
    public  int getPauseWhenEmptySeconds() {
        return pauseWhenEmptySeconds;
    }
    public  int getPauseWhenEmptyTicks() {
        return pauseWhenEmptySeconds * 20; // Convert seconds to ticks
    }

    public  void setPauseWhenEmptySeconds(int pauseWhenEmptySeconds) {
        this.pauseWhenEmptySeconds = Math.max(1, pauseWhenEmptySeconds); // Minimum 1 second
        save();
    }
    public  void setEnablePauseWhenEmpty(boolean enablePauseWhenEmpty) {
        this.enablePauseWhenEmpty = enablePauseWhenEmpty;
        save();
    }
}