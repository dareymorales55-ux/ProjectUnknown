package com.deejay.projectunknown;

import com.deejay.projectunknown.listeners.JoinListener;
import com.deejay.projectunknown.listeners.CompassListener;
import com.deejay.projectunknown.reveal.RevealManager;

import org.bukkit.plugin.java.JavaPlugin;

public class ProjectUnknown extends JavaPlugin {

    private static ProjectUnknown instance;
    private RevealManager revealManager;

    @Override
    public void onEnable() {
        instance = this;

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
        getLogger().info("ProjectUnknown disabled");
    }

    public static ProjectUnknown getInstance() {
        return instance;
    }

    public RevealManager getRevealManager() {
        return revealManager;
    }
}
