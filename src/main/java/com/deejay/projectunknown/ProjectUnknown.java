package com.deejay.projectunknown;

import com.deejay.projectunknown.listeners.JoinListener;
import com.deejay.projectunknown.listeners.CompassListener;
import com.deejay.projectunknown.reveal.RevealManager;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class ProjectUnknown extends JavaPlugin {

    private static ProjectUnknown instance;
    private RevealManager revealManager;
    
    // Config file fields
    private File playerConfigFile;
    private FileConfiguration playerConfig;

    @Override
    public void onEnable() {
        instance = this;

        // Initialize custom config first so listeners can use it
        createPlayerConfig();

        this.revealManager = new RevealManager(this);

        getServer().getPluginManager().registerEvents(
                new JoinListener(this),
                this
        );

        getServer().getPluginManager().registerEvents(
                new CompassListener(this),
                this
        );

        getLogger().info("ProjectUnknown enabled");
    }

    @Override
    public void onDisable() {
        savePlayerConfig(); // Auto-save on shutdown
        getLogger().info("ProjectUnknown disabled");
    }

    // This method fixes the error in JoinListener.java:[31,15]
    public FileConfiguration getPlayerConfig() {
        return this.playerConfig;
    }

    // This method fixes the error in JoinListener.java:[32,15]
    public void savePlayerConfig() {
        try {
            getPlayerConfig().save(playerConfigFile);
        } catch (IOException e) {
            getLogger().severe("Could not save players.yml!");
        }
    }

    // Sets up the physical players.yml file in the plugin folder
    private void createPlayerConfig() {
        playerConfigFile = new File(getDataFolder(), "players.yml");
        if (!playerConfigFile.exists()) {
            playerConfigFile.getParentFile().mkdirs();
            saveResource("players.yml", false);
        }

        playerConfig = YamlConfiguration.loadConfiguration(playerConfigFile);
    }

    public static ProjectUnknown getInstance() {
        return instance;
    }

    public RevealManager getRevealManager() {
        return revealManager;
    }
}
