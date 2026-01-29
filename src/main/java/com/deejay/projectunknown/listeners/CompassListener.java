package com.deejay.projectunknown.listeners;

import com.deejay.projectunknown.ProjectUnknown;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class CompassListener implements Listener {

    private final ProjectUnknown plugin;
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Map<UUID, TrackingData> tracking = new HashMap<>();

    // HARD-CODED VALUES (replacing config)
    private static final int COOLDOWN_SECONDS = 60;
    private static final int TRACK_DURATION_SECONDS = 30;

    private static final double PARTICLE_RADIUS = 2.5;
    private static final int PARTICLE_POINTS = 24;
    private static final double PARTICLE_OFFSET = 0.3;

    public CompassListener(ProjectUnknown plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCompassUse(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR &&
            event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || item.getType() != Material.COMPASS) return;

        event.setCancelled(true);

        if (player.getWorld().getEnvironment() != World.Environment.NORMAL) {
            player.sendMessage(ChatColor.RED + "Compass only works in the overworld.");
            return;
        }

        if (cooldowns.containsKey(player.getUniqueId())) {
            long timeLeft = (cooldowns.get(player.getUniqueId()) - System.currentTimeMillis()) / 1000;
            if (timeLeft > 0) {
                player.sendMessage(ChatColor.RED + "Cooldown: " + formatTime((int) timeLeft));
                return;
            }
            cooldowns.remove(player.getUniqueId());
        }

        List<Player> possibleTargets = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.equals(player) && p.getWorld().getEnvironment() == World.Environment.NORMAL) {
                possibleTargets.add(p);
            }
        }

        if (possibleTargets.isEmpty()) {
            player.sendMessage(ChatColor.GRAY + "No players to track.");
            return;
        }

        Player target = possibleTargets.get(new Random().nextInt(possibleTargets.size()));

        player.sendMessage(ChatColor.RED + "Hunting " + target.getName());
        target.sendMessage(ChatColor.DARK_RED + "You are being hunted.");

        playDetectionParticles(target);

        cooldowns.put(player.getUniqueId(),
                System.currentTimeMillis() + (COOLDOWN_SECONDS * 1000L));

        tracking.put(player.getUniqueId(),
                new TrackingData(target.getUniqueId(), TRACK_DURATION_SECONDS));

        applyCooldown(player);
        startTracking(player);
    }

    private void playDetectionParticles(Player target) {
        Location loc = target.getLocation();
        Random random = new Random();

        for (int i = 0; i < PARTICLE_POINTS; i++) {
            double angle = 2 * Math.PI * i / PARTICLE_POINTS;
            double x = loc.getX() + PARTICLE_RADIUS * Math.cos(angle);
            double z = loc.getZ() + PARTICLE_RADIUS * Math.sin(angle);

            double ox = (random.nextDouble() - 0.5) * PARTICLE_OFFSET;
            double oy = (random.nextDouble() - 0.5) * PARTICLE_OFFSET;
            double oz = (random.nextDouble() - 0.5) * PARTICLE_OFFSET;

            Location particleLoc = new Location(
                    loc.getWorld(),
                    x + ox,
                    loc.getY() + 1 + oy,
                    z + oz
            );

            loc.getWorld().spawnParticle(
                    Particle.TRIAL_SPAWNER_DETECTION_OMINOUS,
                    particleLoc,
                    1, 0, 0, 0, 0
            );
        }
    }

    private void applyCooldown(Player player) {
        player.setCooldown(Material.COMPASS, COOLDOWN_SECONDS * 20);
    }

    private void startTracking(final Player hunter) {
        new BukkitRunnable() {

            int ticks = 0;
            int timeLeft = TRACK_DURATION_SECONDS;

            @Override
            public void run() {
                if (!hunter.isOnline() || !tracking.containsKey(hunter.getUniqueId())) {
                    cancel();
                    return;
                }

                TrackingData data = tracking.get(hunter.getUniqueId());
                Player target = Bukkit.getPlayer(data.targetUUID);

                if (target == null || !target.isOnline()) {
                    sendActionBar(hunter, ChatColor.GRAY + "Target offline");
                    tracking.remove(hunter.getUniqueId());
                    cancel();
                    return;
                }

                if (!hunter.getWorld().equals(target.getWorld())) {
                    sendActionBar(hunter, ChatColor.GRAY + "Target in another dimension");
                    tracking.remove(hunter.getUniqueId());
                    cancel();
                    return;
                }

                double distance = hunter.getLocation().distance(target.getLocation());
                String arrow = getDirectionArrow(hunter, target);

                sendActionBar(
                        hunter,
                        "§c§l" + formatTime(timeLeft) + " §8| §7" + (int) distance + "m " + arrow
                );

                sendActionBar(target, "§c" + formatTime(timeLeft));

                ticks++;
                if (ticks >= 20) {
                    ticks = 0;
                    timeLeft--;
                    if (timeLeft <= 0) {
                        tracking.remove(hunter.getUniqueId());
                        hunter.sendMessage(ChatColor.GRAY + "Hunt ended.");
                        target.sendMessage(ChatColor.GRAY + "Hunt ended.");
                        cancel();
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    private String getDirectionArrow(Player hunter, Player target) {
        double dx = target.getLocation().getX() - hunter.getLocation().getX();
        double dz = target.getLocation().getZ() - hunter.getLocation().getZ();
        double angle = Math.toDegrees(Math.atan2(dz, dx)) - 90;
        double yaw = hunter.getLocation().getYaw();
        double relative = (angle - yaw + 360) % 360;

        if (relative < 22.5 || relative >= 337.5) return "⬆";
        if (relative < 67.5) return "⬈";
        if (relative < 112.5) return "➡";
        if (relative < 157.5) return "⬊";
        if (relative < 202.5) return "⬇";
        if (relative < 247.5) return "⬋";
        if (relative < 292.5) return "⬅";
        return "⬉";
    }

    private String formatTime(int seconds) {
        int m = seconds / 60;
        int s = seconds % 60;
        return String.format("%02d:%02d", m, s);
    }

    private void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(
                ChatMessageType.ACTION_BAR,
                new TextComponent(message)
        );
    }

    private static class TrackingData {
        UUID targetUUID;
        int duration;

        TrackingData(UUID targetUUID, int duration) {
            this.targetUUID = targetUUID;
            this.duration = duration;
        }
    }
}
