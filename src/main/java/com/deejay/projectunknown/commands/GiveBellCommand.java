package com.deejay.projectunknown.commands;

import com.deejay.projectunknown.ProjectUnknown;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class GiveBellCommand implements CommandExecutor {

    private final NamespacedKey bellItemKey;

    public GiveBellCommand(ProjectUnknown plugin) {
        this.bellItemKey = new NamespacedKey(plugin, "bell_of_truth_item");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        ItemStack bell = new ItemStack(Material.BELL);
        ItemMeta meta = bell.getItemMeta();

        meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Bell of Truth");
        meta.addEnchant(Enchantment.LUCK_OF_THE_SEA, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.getPersistentDataContainer().set(bellItemKey, PersistentDataType.BYTE, (byte) 1);

        bell.setItemMeta(meta);
        player.getInventory().addItem(bell);

        player.sendMessage(ChatColor.RED + "You received the " + ChatColor.BOLD + "Bell of Truth");
        return true;
    }
}
