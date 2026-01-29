package com.deejay.projectunknown.reveal;

import com.deejay.projectunknown.ProjectUnknown;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class HourlyRevealManager {

    private final ProjectUnknown plugin;
    private final RevealManager revealManager;

    private boolean started = false;

    // timings (ticks)
    private static final long INITIAL_DELAY = 10 * 60 * 20L; // 10 minutes
    private static final long WARNING_DELAY = 5 * 20L;       // 5 seconds
    private static final long REVEAL_DURATION = 10 * 60;     // seconds
    private static final long COOLDOWN_DELAY = 50 * 60 * 20L; // 50 minutes

    public HourlyRevealManager(ProjectUnknown plugin) {
        this.plugin = plugin;
        this.revealManager = plugin.getRevealManager();
    }

    public void startIfNeeded() {
        if (started) return;
        started = true;

        scheduleNextCycle();
    }

    private void scheduleNextCycle() {
        new BukkitRunnable() {
            @Override
            public void run() {
                sendWarning();
            }
        }.runTaskLater(plugin, INITIAL_DELAY);
    }

    private void sendWarning() {
        Bukkit.broadcastMessage("§c§lPlayer(s) will be revealed promptly");

        new BukkitRunnable() {
            @Override
            public void run() {
                startReveal();
            }
        }.runTaskLater(plugin, WARNING_DELAY);
    }

    private void startReveal() {
        // FIXED: Changed Sound.RESPAWN_ANCHOR_CHARGE to Sound.BLOCK_RESPAWN_ANCHOR_CHARGE
        Bukkit.getOnlinePlayers().forEach(p ->
                p.playSound(p.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1f, 1f)
        );

        List<Player> candidates = new ArrayList<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!revealManager.isRevealed(player)) {
                candidates.add(player);
            }
        }

        if (candidates.isEmpty()) {
            scheduleCooldown();
            return;
        }

        Collections.shuffle(candidates);
        Random random = new Random();

        int revealCount = random.nextBoolean() ? 1 : 2;
        revealCount = Math.min(revealCount, candidates.size());

        List<Player> revealed = candidates.subList(0, revealCount);

        for (Player player : revealed) {
            revealManager.reveal(player, REVEAL_DURATION);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                endReveal();
            }
        }.runTaskLater(plugin, REVEAL_DURATION * 20L);
    }

    private void endReveal() {
        // FIXED: Changed Sound.POTION_BREWING to Sound.BLOCK_BREWING_STAND_BREW
        Bukkit.getOnlinePlayers().forEach(p ->
                p.playSound(p.getLocation(), Sound.BLOCK_BREWING_STAND_BREW, 1f, 1f)
        );

        scheduleCooldown();
    }

    private void scheduleCooldown() {
        new BukkitRunnable() {
            @Override
            public void run() {
                scheduleNextCycle();
            }
        }.runTaskLater(plugin, COOLDOWN_DELAY);
    }
}
