package com.deejay.projectunknown.commands;

import com.deejay.projectunknown.mobs.UnknownChicken;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SummonChickenCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        UnknownChicken.spawn(player.getLocation());

        player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "The Unknown Chicken has been summoned.");

        return true;
    }
}
