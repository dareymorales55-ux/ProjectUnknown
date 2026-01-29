package com.deejay.projectunknown.listeners;

import com.deejay.projectunknown.ProjectUnknown;
import com.deejay.projectunknown.reveal.RevealManager;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BellListener implements Listener {

    private final ProjectUnknown plugin;
    private final RevealManager revealManager;

    private final NamespacedKey bellItemKey;
    private final NamespacedKey bellBlockKey;

    private final Map<UUID, Long> cooldowns = new HashMap<>();

    private static final int RADIUS = 15;
    private static final long REVEAL_TICKS = 10 * 60 * 20;
    private static final long COOLDOWN_MS = 5 * 60 * 1000;

    public BellListener(ProjectUnknown plugin) {
        this.plugin = plugin;
        this.revealManager = plugin.getRevealManager();
        this.bellItemKey = new NamespacedKey(plugin, "bell_of_truth_item");
        this.bellBlockKey = new NamespacedKey(plugin, "bell_of_truth_block");
    }

    @EventHandler
    public void onBellPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (item.getType() != Material.BELL || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        if (!meta.getPersistentDataContainer().has(bellItemKey, PersistentDataType.BYTE)) return;

        Block block = event.getBlockPlaced();
        // FIX: Cast to TileState to access PersistentDataContainer on a Block
        if (block.getState() instanceof TileState tileState) {
            tileState.getPersistentDataContainer().set(bellBlockKey, PersistentDataType.BYTE, (byte) 1);
            tileState.update(); // Important: must call update() to save changes
        }
    }

    @EventHandler
    public void onBellBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.BELL) return;

        // FIX: Cast to TileState to check data
        if (block.getState() instanceof TileState tileState) {
            if (!tileState.getPersistentDataContainer().has(bellBlockKey, PersistentDataType.BYTE)) return;

            event.setDropItems(false);
            block.getWorld().dropItemNaturally(block.getLocation(), createBellOfTruth());
            // No need to manually remove data if block is breaking, but good practice:
            tileState.getPersistentDataContainer().remove(bellBlockKey);
            tileState.update();
        }
    }

    @EventHandler
    public void onBellRing(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK &&
            event.getAction() != Action.RIGHT_CLICK_AIR) return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || item.getType() != Material.BELL || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        if (!meta.getPersistentDataContainer().has(bellItemKey, PersistentDataType.BYTE)) return;

        event.setCancelled(true);

        long now = System.currentTimeMillis();
        long expires = cooldowns.getOrDefault(player.getUniqueId(), 0L);

        if (expires > now) {
            long seconds = (expires - now) / 1000;
            player.sendMessage(ChatColor.RED + "Bell on cooldown: " + seconds + "s");
            return;
        }

        cooldowns.put(player.getUniqueId(), now + COOLDOWN_MS);

        Location center = player.getLocation().add(0, 0.1, 0);
        spawnRedstoneRing(center);

        for (Player target : player.getWorld().getPlayers()) {
            if (target.equals(player)) continue;
            if (target.getLocation().distance(center) > RADIUS) continue;

            revealManager.reveal(target, REVEAL_TICKS);
            target.sendMessage(ChatColor.DARK_RED + "You have been revealed by the Bell of Truth.");
        }
    }

    private void spawnRedstoneRing(Location center) {
        DustOptions dust = new DustOptions(Color.RED, 1.5f);
        int points = 120;

        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI * i / points;
            double x = center.getX() + RADIUS * Math.cos(angle);
            double z = center.getZ() + RADIUS * Math.sin(angle);

            // FIX: Changed Particle.REDSTONE to Particle.DUST for 1.21.4
            center.getWorld().spawnParticle(
                    Particle.DUST,
                    new Location(center.getWorld(), x, center.getY(), z),
                    1,
                    dust
            );
        }
    }

    private ItemStack createBellOfTruth() {
        ItemStack bell = new ItemStack(Material.BELL);
        ItemMeta meta = bell.getItemMeta();

        meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Bell of Truth");
        meta.addEnchant(org.bukkit.enchantments.Enchantment.LUCK_OF_THE_SEA, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.getPersistentDataContainer().set(bellItemKey, PersistentDataType.BYTE, (byte) 1);

        bell.setItemMeta(meta);
        return bell;
    }
}
