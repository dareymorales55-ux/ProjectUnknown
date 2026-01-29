package com.deejay.projectunknown.commands;

import com.deejay.projectunknown.ProjectUnknown;
import com.deejay.projectunknown.mobs.UnknownChicken;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class SummonChickenCommand implements CommandExecutor {
    private final ProjectUnknown plugin;

    public SummonChickenCommand(ProjectUnknown plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;

        // Create a new instance or use one from plugin to call non-static spawn
        new UnknownChicken(plugin).spawn(player.getLocation());

        player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "The Unknown Chicken has been summoned.");
        return true;
    }
}
