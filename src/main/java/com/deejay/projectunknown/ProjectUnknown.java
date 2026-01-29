package com.deejay.projectunknown;

import com.deejay.projectunknown.commands.GiveCompassCommand;
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

    // players.yml
    private File playerConfigFile;
    private FileConfiguration playerConfig;

    @Override
    public void onEnable() {
        instance = this;

        // Create players.yml FIRST
        createPlayerConfig();

        // Reveal system
        this.revealManager = new RevealManager(this);

        // Listeners
        getServer().getPluginManager().registerEvents(
                new JoinListener(this),
                this
        );

        getServer().getPluginManager().registerEvents(
                new CompassListener(this),
                this
        );

        // Commands
        getCommand("givecompass").setExecutor(new GiveCompassCommand());

        getLogger().info("ProjectUnknown enabled");
    }

    @Override
    public void onDisable() {
        savePlayerConfig();
        getLogger().info("ProjectUnknown disabled");
    }

    /* =========================
       players.yml access
       ========================= */

    public FileConfiguration getPlayerConfig() {
        return this.playerConfig;
    }

    public void savePlayerConfig() {
        try {
            playerConfig.save(playerConfigFile);
        } catch (IOException e) {
            getLogger().severe("Could not save players.yml!");
        }
    }

    private void createPlayerConfig() {
        playerConfigFile = new File(getDataFolder(), "players.yml");

        if (!playerConfigFile.exists()) {
            playerConfigFile.getParentFile().mkdirs();
            saveResource("players.yml", false);
        }

        playerConfig = YamlConfiguration.loadConfiguration(playerConfigFile);
    }

    /* =========================
       Getters
       ========================= */

    public static ProjectUnknown getInstance() {
        return instance;
    }

    public RevealManager getRevealManager() {
        return revealManager;
    }
}
