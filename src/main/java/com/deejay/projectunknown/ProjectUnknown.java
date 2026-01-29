package com.deejay.projectunknown;

import com.deejay.projectunknown.commands.GiveCompassCommand;
import com.deejay.projectunknown.commands.SummonChickenCommand;
import com.deejay.projectunknown.listeners.*;
import com.deejay.projectunknown.mobs.UnknownChicken;
import com.deejay.projectunknown.reveal.RevealManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.io.IOException;

public class ProjectUnknown extends JavaPlugin {
    private static ProjectUnknown instance;
    private RevealManager revealManager;
    private File playerConfigFile;
    private FileConfiguration playerConfig;

    @Override
    public void onEnable() {
        instance = this;
        createPlayerConfig();
        this.revealManager = new RevealManager(this);

        // Register all listeners
        getServer().getPluginManager().registerEvents(new JoinListener(this), this);
        getServer().getPluginManager().registerEvents(new CompassListener(this), this);
        getServer().getPluginManager().registerEvents(new EggListener(this), this);
        getServer().getPluginManager().registerEvents(new BellListener(this), this);
        getServer().getPluginManager().registerEvents(new UnknownChicken(this), this);

        getCommand("givecompass").setExecutor(new GiveCompassCommand());
        getCommand("summonchicken").setExecutor(new SummonChickenCommand(this));

        getLogger().info("ProjectUnknown enabled");
    }

    @Override
    public void onDisable() {
        savePlayerConfig();
    }

    public FileConfiguration getPlayerConfig() { return this.playerConfig; }
    public RevealManager getRevealManager() { return revealManager; }
    public static ProjectUnknown getInstance() { return instance; }

    public void savePlayerConfig() {
        try { playerConfig.save(playerConfigFile); } catch (IOException e) { e.printStackTrace(); }
    }

    private void createPlayerConfig() {
        playerConfigFile = new File(getDataFolder(), "players.yml");
        if (!playerConfigFile.exists()) {
            playerConfigFile.getParentFile().mkdirs();
            saveResource("players.yml", false);
        }
        playerConfig = YamlConfiguration.loadConfiguration(playerConfigFile);
    }
}
