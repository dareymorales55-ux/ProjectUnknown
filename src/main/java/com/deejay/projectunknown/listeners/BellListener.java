package com.deejay.projectunknown.listeners;

import com.deejay.projectunknown.ProjectUnknown;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.*;

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
        if (item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(bellKey, PersistentDataType.BOOLEAN)) {
            Block block = event.getBlockPlaced();
            NamespacedKey blockKey = new NamespacedKey(plugin, "bell_" + block.getX() + "_" + block.getY() + "_" + block.getZ());
            block.getChunk().getPersistentDataContainer().set(blockKey, PersistentDataType.BOOLEAN, true);
        }
    }

    @EventHandler
    public void onBellBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        NamespacedKey blockKey = new NamespacedKey(plugin, "bell_" + block.getX() + "_" + block.getY() + "_" + block.getZ());
        if (block.getChunk().getPersistentDataContainer().has(blockKey, PersistentDataType.BOOLEAN)) {
            event.setDropItems(false);
            block.getWorld().dropItemNaturally(block.getLocation(), createBellOfTruth(plugin));
            block.getChunk().getPersistentDataContainer().remove(blockKey);
        }
    }

    @EventHandler
    public void onBellRing(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getType() == Material.BELL) {
            Block block = event.getClickedBlock();
            NamespacedKey blockKey = new NamespacedKey(plugin, "bell_" + block.getX() + "_" + block.getY() + "_" + block.getZ());
            if (block.getChunk().getPersistentDataContainer().has(blockKey, PersistentDataType.BOOLEAN)) {
                Player player = event.getPlayer();
                long expiry = cooldowns.getOrDefault(player, 0L);
                if (expiry > System.currentTimeMillis()) {
                    player.sendMessage(ChatColor.RED + "Cooldown!");
                    return;
                }
                cooldowns.put(player, System.currentTimeMillis() + (300 * 1000L));
                spawnBellCircle(block.getLocation().add(0.5, 0.5, 0.5));
                // NOTE: Ensure revealInRadius exists in RevealManager or change this call
                plugin.getRevealManager().reveal(player, 200L); 
            }
        }
    }

    private void spawnBellCircle(Location loc) {
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks++ > 100) cancel();
                for (int i = 0; i < 40; i++) {
                    double angle = 2 * Math.PI * i / 40;
                    Location pLoc = loc.clone().add(15 * Math.cos(angle), 1, 15 * Math.sin(angle));
                    loc.getWorld().spawnParticle(Particle.DUST, pLoc, 1, new Particle.DustOptions(Color.RED, 1f));
                }
            }
        }.runTaskTimer(plugin, 0, 2);
    }

    public static ItemStack createBellOfTruth(ProjectUnknown plugin) {
        ItemStack bell = new ItemStack(Material.BELL);
        ItemMeta meta = bell.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Bell Of Truth");
        meta.addEnchant(Enchantment.UNBREAKING, 1, true); // Fixed Enum
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "bell_of_truth"), PersistentDataType.BOOLEAN, true);
        bell.setItemMeta(meta);
        return bell;
    }
}
