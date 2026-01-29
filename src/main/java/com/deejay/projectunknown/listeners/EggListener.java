package com.deejay.projectunknown.listeners;

import com.deejay.projectunknown.ProjectUnknown;
import com.deejay.projectunknown.reveal.RevealManager;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

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
        startPassiveParticles();
    }

    /* ======================================================
       APPLY META TO ANY DRAGON EGG
       ====================================================== */

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        ItemStack item = event.getItem().getItemStack();
        if (item.getType() == Material.DRAGON_EGG) applyEggMeta(item);
    }

    @EventHandler
    public void onInventoryMove(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        ItemStack item = event.getCurrentItem();
        if (item == null) return;
        if (item.getType() == Material.DRAGON_EGG) applyEggMeta(item);
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
        if (event.getItem() == null || event.getItem().getType() != Material.DRAGON_EGG) return;

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

        player.getWorld().playSound(
                player.getLocation(),
                Sound.ENTITY_ENDER_DRAGON_GROWL,
                1.5f,
                1f
        );

        startEggSequence(player);
    }

    /* ======================================================
       COUNTDOWN + MOVING RING
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
                spawnRing(center);

                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (!p.getWorld().equals(center.getWorld())) continue;
                    if (p.getLocation().distance(center) > COUNTDOWN_RADIUS) continue;

                    ChatColor color = (countdown == 1)
                            ? ChatColor.RED
                            : ChatColor.DARK_PURPLE;

                    p.sendTitle(color + "" + ChatColor.BOLD + countdown, "", 0, 20, 0);
                }

                countdown--;
                if (countdown < 0) {
                    revealPlayers(center, holder);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void spawnRing(Location center) {
        World world = center.getWorld();
        if (world == null) return;

        int points = 36;
        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI * i / points;
            double x = center.getX() + RING_RADIUS * Math.cos(angle);
            double z = center.getZ() + RING_RADIUS * Math.sin(angle);

            world.spawnParticle(
                    Particle.DRAGON_BREATH,
                    new Location(world, x, center.getY() + 0.2, z),
                    1, 0, 0, 0, 0
            );
        }
    }

    private void revealPlayers(Location center, Player holder) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.equals(holder)) continue;
            if (!p.getWorld().equals(center.getWorld())) continue;
            if (p.getLocation().distance(center) > RING_RADIUS) continue;

            p.sendMessage(ChatColor.DARK_RED + "You have been revealed by the Dragon Egg.");
            revealManager.reveal(p, 15 * 20L);
        }
    }

    /* ======================================================
       PASSIVE PARTICLES WHILE IN INVENTORY
       ====================================================== */

    private void startPassiveParticles() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!hasEgg(player)) continue;

                    player.getWorld().spawnParticle(
                            Particle.PORTAL,
                            player.getLocation().add(0, 1, 0),
                            6, 0.3, 0.5, 0.3, 0
                    );
                }
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }

    private boolean hasEgg(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.DRAGON_EGG) return true;
        }
        return false;
    }

    /* ======================================================
       PERMANENT HEART ON KILL (MAX 20 HEARTS)
       ====================================================== */

    @EventHandler
    public void onKill(EntityDeathEvent event) {
        if (!(event.getEntity().getKiller() instanceof Player killer)) return;
        if (!hasEgg(killer)) return;

        AttributeInstance attr = killer.getAttribute(Attribute.MAX_HEALTH);
        if (attr == null) return;

        double current = attr.getBaseValue();
        double max = 40.0;

        if (current < max) {
            attr.setBaseValue(Math.min(current + 2.0, max));
            killer.sendMessage(ChatColor.DARK_PURPLE + "You gained a permanent heart.");
        }
    }
}
