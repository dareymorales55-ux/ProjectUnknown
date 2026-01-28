package com.deejay.projectunknown.reveal;

import com.deejay.projectunknown.ProjectUnknown;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;
// FIXED: Scoreboard classes MUST be imported from the .scoreboard package
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class RevealManager implements Listener {

    private final ProjectUnknown plugin;
    private final Set<UUID> revealedPlayers = new HashSet<>();

    private final Scoreboard scoreboard;
    private final Team revealedTeam;

    public RevealManager(ProjectUnknown plugin) {
        this.plugin = plugin;

        ScoreboardManager manager = Bukkit.getScoreboardManager();
        this.scoreboard = manager.getMainScoreboard();

        Team team = scoreboard.getTeam("revealed");
        if (team == null) {
            team = scoreboard.registerNewTeam("revealed");
            team.setColor(ChatColor.RED);
            team.setPrefix(ChatColor.RED.toString());
        }
        this.revealedTeam = team;

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /* =========================
       REVEAL / UNREVEAL
       ========================= */

    public boolean isRevealed(Player player) {
        return revealedPlayers.contains(player.getUniqueId());
    }

    public void reveal(Player player, long durationTicks) {
        if (isRevealed(player)) return;

        revealedPlayers.add(player.getUniqueId());

        // Restore real name
        player.setDisplayName(player.getName());
        player.setPlayerListName(player.getName());

        // Visuals
        player.setGlowing(true);
        revealedTeam.addEntry(player.getName());

        // Auto-unreveal
        new BukkitRunnable() {
            @Override
            public void run() {
                unreveal(player);
            }
        }.runTaskLater(plugin, durationTicks);
    }

    public void unreveal(Player player) {
        if (!isRevealed(player)) return;

        revealedPlayers.remove(player.getUniqueId());

        // Remove visuals
        player.setGlowing(false);
        revealedTeam.removeEntry(player.getName());

        // Re-anonymize
        player.setDisplayName("Player");
        player.setPlayerListName("Player");
    }

    /* =========================
       CAUGHT LOGIC
       ========================= */

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();

        if (!isRevealed(victim)) return;

        // Make sure real name shows in messages
        victim.setDisplayName(victim.getName());
        victim.setPlayerListName(victim.getName());

        Bukkit.broadcastMessage(ChatColor.RED + victim.getName() + " has been caught.");

        // FIXED: Using NAME type and victim.getName() to avoid profile mismatch errors
        Bukkit.getBanList(org.bukkit.BanList.Type.NAME).addBan(
                victim.getName(),
                ChatColor.DARK_RED + "Your cover was blown.",
                (java.util.Date) null,
                null
        );

        Bukkit.getScheduler().runTask(plugin, () -> {
            victim.kick(net.kyori.adventure.text.Component.text(ChatColor.DARK_RED + "Your cover was blown."));
        });

        revealedPlayers.remove(victim.getUniqueId());
    }
}
