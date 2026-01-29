package com.deejay.projectunknown.mobs;

import com.deejay.projectunknown.ProjectUnknown;
import com.deejay.projectunknown.listeners.BellListener;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Listener;

public class UnknownChicken implements Listener {
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
        
        // Fixed Attribute name for 1.16+ and casted safely
        chicken.getAttribute(Attribute.MAX_HEALTH).setBaseValue(200);
        chicken.setHealth(200);
        chicken.setGravity(false);
        chicken.setSilent(true);

        chicken.getEquipment().setHelmet(BellListener.createBellOfTruth(plugin));
        chicken.getEquipment().setChestplate(BellListener.createBellOfTruth(plugin));

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!chicken.isDead()) return;
            // Use Bukkit.broadcastMessage instead of world
            Bukkit.broadcastMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "The Unknown Chicken has been defeated!");
            world.dropItemNaturally(loc, BellListener.createBellOfTruth(plugin));
        }, 1L);
    }
}
