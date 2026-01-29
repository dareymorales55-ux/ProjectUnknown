package com.deejay.projectunknown.listeners;

import com.deejay.projectunknown.ProjectUnknown;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class BellListener implements Listener {

    private final ProjectUnknown plugin;
    private final NamespacedKey bellKey;
    private final Map<Player, Long> cooldowns = new HashMap<>();

    public BellListener(ProjectUnknown plugin) {
        this.plugin = plugin;
        this.bellKey = new NamespacedKey(plugin, "bell_of_truth");
    }

    @EventHandler
    public void onBellPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (item.getType() == Material.BELL && item.hasItemMeta() &&
                item.getItemMeta().getPersistentDataContainer().has(this.bellKey, PersistentDataType.BOOLEAN)) {

            Block block = event.getBlockPlaced();
            NamespacedKey blockKey = new NamespacedKey(plugin,
                    "bell_" + block.getX() + "_" + block.getY() + "_" + block.getZ());
            block.getChunk().getPersistentDataContainer().set(blockKey, PersistentDataType.BOOLEAN, true);
        }
    }

    @EventHandler
    public void onBellBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() == Material.BELL) {
            NamespacedKey blockKey = new NamespacedKey(plugin,
                    "bell_" + block.getX() + "_" + block.getY() + "_" + block.getZ());
            if (block.getChunk().getPersistentDataContainer().has(blockKey, PersistentDataType.BOOLEAN)) {
                event.setDropItems(false);
                block.getWorld().dropItemNaturally(block.getLocation(), createBellOfTruth());
                block.getChunk().getPersistentDataContainer().remove(blockKey);
            }
        }
    }

    @EventHandler
    public void onBellRing(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            if (block != null && block.getType() == Material.BELL) {
                NamespacedKey blockKey = new NamespacedKey(plugin,
                        "bell_" + block.getX() + "_" + block.getY() + "_" + block.getZ());
                if (block.getChunk().getPersistentDataContainer().has(blockKey, PersistentDataType.BOOLEAN)) {
                    Player player = event.getPlayer();
                    long remaining = getCooldownRemaining(player);
                    if (remaining > 0L) {
                        event.setCancelled(true);
                        player.sendMessage(ChatColor.RED + "Bell on cooldown: " + remaining + "s");
                    } else {
                        cooldowns.put(player, System.currentTimeMillis() + 300 * 1000L); // 5 min
                        spawnBellCircle(block.getLocation().add(0.5, 0.5, 0.5));
                        plugin.getRevealManager().revealInRadius(block.getLocation(), 15, 10 * 20L,
                                ChatColor.DARK_RED + "You have been revealed by the Bell of Truth!");
                    }
                }
            }
        }
    }

    private long getCooldownRemaining(Player player) {
        Long expiry = cooldowns.get(player);
        if (expiry == null) return 0;
        long remaining = (expiry - System.currentTimeMillis()) / 1000L;
        if (remaining <= 0) {
            cooldowns.remove(player);
            return 0;
        }
        return remaining;
    }

    private void spawnBellCircle(final Location loc) {
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= 100) { // example duration
                    cancel();
                    return;
                }
                double radius = 15; // example radius
                int points = 40;
                Random rand = new Random();

                for (int i = 0; i < points; i++) {
                    double angle = 2 * Math.PI * i / points;
                    double x = loc.getX() + radius * Math.cos(angle);
                    double z = loc.getZ() + radius * Math.sin(angle);
                    Location particleLoc = new Location(loc.getWorld(), x, loc.getY() + 1, z);
                    loc.getWorld().spawnParticle(Particle.REDSTONE, particleLoc, 1,
                            new DustOptions(Color.RED, 1f));
                }
                ticks++;
            }
        }.runTaskTimer(plugin, 0, 2);
    }

    public static ItemStack createBellOfTruth() {
        ItemStack bell = new ItemStack(Material.BELL);
        ItemMeta meta = bell.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Bell Of Truth");
        meta.addEnchant(org.bukkit.enchantments.Enchantment.DURABILITY, 1, true); // glint
        bell.setItemMeta(meta);
        return bell;
    }
}
