package com.deejay.projectunknown.mobs;

import com.deejay.projectunknown.ProjectUnknown;
import com.deejay.projectunknown.listeners.BellListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class UnknownChicken {

    private final ProjectUnknown plugin;

    public UnknownChicken(ProjectUnknown plugin) {
        this.plugin = plugin;
    }

    public void spawn(Location loc) {
        World world = loc.getWorld();
        if (world == null) return;

        Chicken chicken = (Chicken) world.spawnEntity(loc, EntityType.CHICKEN);
        chicken.setCustomName(ChatColor.YELLOW + "" + ChatColor.BOLD + "Unknown Chicken");
        chicken.setCustomNameVisible(true);
        chicken.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(200);
        chicken.setHealth(200);
        chicken.setGravity(false);

        // Scale up 50x (hacky visual)
        chicken.setSilent(true); // prevent sounds for normal chicken
        // Give items (bells) on death
        chicken.getEquipment().setHelmet(BellListener.createBellOfTruth());
        chicken.getEquipment().setChestplate(BellListener.createBellOfTruth());

        // Bossbar & reveal on hit handled elsewhere

        // On death
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!chicken.isDead()) return;
            world.broadcastMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "The Unknown Chicken has been defeated!");
            world.dropItemNaturally(loc, BellListener.createBellOfTruth());
            world.dropItemNaturally(loc, BellListener.createBellOfTruth());
        }, 1L);
    }
}
