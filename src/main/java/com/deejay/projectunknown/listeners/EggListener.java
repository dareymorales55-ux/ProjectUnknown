package com.deejay.projectunknown.listeners;

import com.deejay.projectunknown.ProjectUnknown;
import com.deejay.projectunknown.reveal.RevealManager;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EggListener implements Listener {

    private final ProjectUnknown plugin;
    private final RevealManager revealManager;

    private final Map<UUID, Long> cooldowns = new HashMap<>();

    private static final int COOLDOWN_SECONDS = 25;
    private static final int COUNTDOWN_SECONDS = 5;
    private static final int RING_RADIUS = 10;
    private static final int COUNTDOWN_RADIUS = 20;

    public EggListener(ProjectUnknown plugin) {
        this.plugin = plugin;
        this.revealManager = plugin.getRevealManager();
    }

    /* ======================================================
       ENCHANT + NAME ANY DRAGON EGG THAT ENTERS INVENTORY
       ====================================================== */

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        ItemStack item = event.getItem().getItemStack();
        if (item.getType() == Material.DRAGON_EGG) {
            applyEggMeta(item);
        }
    }

    @EventHandler
    public void onInventoryMove(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (event.getCurrentItem() == null) return;

        ItemStack item = event.getCurrentItem();
        if (item.getType() == Material.DRAGON_EGG) {
            applyEggMeta(item);
        }
    }

    private void applyEggMeta(ItemStack egg) {
        ItemMeta meta = egg.getItemMeta();
        if (meta == null) return;

        meta.setDisplayName(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Dragon Egg");
        meta.addEnchant(Enchantment.LUCK_OF_THE_SEA, 1, true); // glint only
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        egg.setItemMeta(meta);
    }

    /* ======================================================
       RIGHT CLICK ABILITY
       ====================================================== */

    @EventHandler
    public void onEggUse(PlayerInteractEvent event) {
        if (!event.getAction().toString().contains("RIGHT_CLICK")) return;
        if (event.getItem() == null) return;
        if (event.getItem().getType() != Material.DRAGON_EGG) return;

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        long now = System.currentTimeMillis();
        if (cooldowns.containsKey(uuid)) {
            long left = (cooldowns.get(uuid) - now) / 1000;
            if (left > 0) {
                player.sendMessage(ChatColor.RED + "Egg on cooldown (" + left + "s)");
                return;
            }
        }

        cooldowns.put(uuid, now + (COOLDOWN_SECONDS * 1000L));

        // Dragon roar
        player.getWorld().playSound(
                player.getLocation(),
                Sound.ENTITY_ENDER_DRAGON_GROWL,
                1.5f,
                1f
        );

        startEggSequence(player);
    }

    /* ======================================================
       MAIN SEQUENCE
       ====================================================== */

    private void startEggSequence(Player holder) {

        new BukkitRunnable() {

            int countdown = COUNTDOWN_SECONDS;

            @Override
            public void run() {

                if (!holder.isOnline()) {
                    cancel();
                    return;
                }

                Location center = holder.getLocation();

                // Spawn moving purple ring
                spawnRing(center);

                // Countdown display
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (!p.getWorld().equals(center.getWorld())) continue;
                    if (p.getLocation().distance(center) > COUNTDOWN_RADIUS) continue;

                    ChatColor color = (countdown == 1)
                            ? ChatColor.RED
                            : ChatColor.DARK_PURPLE;

                    p.sendTitle(
                            color + "" + ChatColor.BOLD + countdown,
                            "",
                            0,
                            20,
                            0
                    );
                }

                countdown--;

                if (countdown < 0) {
                    revealPlayers(center, holder);
                    cancel();
                }
            }

        }.runTaskTimer(plugin, 0L, 20L);
    }

    /* ======================================================
       PARTICLE RING
       ====================================================== */

    private void spawnRing(Location center) {
        World world = center.getWorld();
        if (world == null) return;

        int points = 36;

        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI * i / points;
            double x = center.getX() + RING_RADIUS * Math.cos(angle);
            double z = center.getZ() + RING_RADIUS * Math.sin(angle);

            Location loc = new Location(world, x, center.getY() + 0.2, z);

            world.spawnParticle(
                    Particle.DRAGON_BREATH,
                    loc,
                    1,
                    0, 0, 0,
                    0
            );
        }
    }

    /* ======================================================
       REVEAL LOGIC
       ====================================================== */

    private void revealPlayers(Location center, Player holder) {

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.equals(holder)) continue;
            if (!p.getWorld().equals(center.getWorld())) continue;
            if (p.getLocation().distance(center) > RING_RADIUS) continue;

            p.sendMessage(ChatColor.DARK_RED + "You have been revealed by the Dragon Egg.");
            revealManager.reveal(p, 15 * 20L);
        }
    }
}
