package com.deejay.projectunknown.mobs;

import com.deejay.projectunknown.ProjectUnknown;
import com.deejay.projectunknown.listeners.BellListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class UnknownChicken implements Listener {

    private static ProjectUnknown plugin;
    private static NamespacedKey chickenKey;
    private static BossBar bossBar;

    public UnknownChicken(ProjectUnknown plugin) {
        UnknownChicken.plugin = plugin;
        chickenKey = new NamespacedKey(plugin, "unknown_chicken");
    }

    /* =========================
       SPAWN
       ========================= */

    public static void spawn(org.bukkit.Location location) {
        Chicken chicken = (Chicken) location.getWorld().spawnEntity(location, EntityType.CHICKEN);

        PersistentDataContainer data = chicken.getPersistentDataContainer();
        data.set(chickenKey, PersistentDataType.BOOLEAN, true);

        chicken.setCustomName(ChatColor.YELLOW + "" + ChatColor.BOLD + "Unknown Chicken");
        chicken.setCustomNameVisible(true);

        chicken.setAdult();
        chicken.setMaxHealth(200.0);
        chicken.setHealth(200.0);
        chicken.setGlowing(true);

        bossBar = Bukkit.createBossBar(
                ChatColor.YELLOW + "" + ChatColor.BOLD + "Unknown Chicken",
                BarColor.YELLOW,
                BarStyle.SEGMENTED_20
        );

        Bukkit.getOnlinePlayers().forEach(bossBar::addPlayer);
        bossBar.setProgress(1.0);
    }

    /* =========================
       HIT → REVEAL ATTACKER
       ========================= */

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Chicken)) return;

        Chicken chicken = (Chicken) event.getEntity();
        if (!chicken.getPersistentDataContainer().has(chickenKey, PersistentDataType.BOOLEAN)) return;

        if (!(event.getDamager() instanceof Player)) return;

        Player attacker = (Player) event.getDamager();

        // Reveal attacker for 5 seconds
        plugin.getRevealManager().reveal(attacker, 5 * 20);
    }

    /* =========================
       DEATH
       ========================= */

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Chicken)) return;

        Chicken chicken = (Chicken) event.getEntity();
        if (!chicken.getPersistentDataContainer().has(chickenKey, PersistentDataType.BOOLEAN)) return;

        if (bossBar != null) {
            bossBar.removeAll();
        }

        Bukkit.broadcastMessage(
                ChatColor.YELLOW + "" + ChatColor.BOLD + "The Unknown Chicken has been defeated"
        );

        event.getDrops().clear();
        event.getDrops().add(BellListener.createBellOfTruth(plugin));
        event.getDrops().add(BellListener.createBellOfTruth(plugin));
    }
}
