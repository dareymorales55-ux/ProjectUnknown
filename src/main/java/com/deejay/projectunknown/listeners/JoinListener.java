package com.deejay.projectunknown.listeners;

import com.deejay.projectunknown.ProjectUnknown;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.List;
import java.util.UUID;

public class JoinListener implements Listener {

    private final ProjectUnknown plugin;

    public JoinListener(ProjectUnknown plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Store real name
        plugin.getPlayerConfig().set("players." + uuid + ".name", player.getName());
        plugin.savePlayerConfig();

        // Create anonymized profile
        PlayerProfile profile = Bukkit.createProfileExact(uuid, "Player");

        profile.setProperties(List.of(
            new ProfileProperty(
                "textures",

                // ✅ TEXTURE VALUE (Base64 JSON)
                "ewogICJ0aW1lc3RhbXAiIDogMTc2OTczNjQ1NjAyMSwKICAicHJvZmlsZUlkIiA6ICJiZTQxM2Y4M2Y4ZWE0MjE0OGMwMjk0YTJiYzIyN2U2NSIsCiAgInByb2ZpbGVOYW1lIiA6ICJGaWdodGJveTEwMyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9jZjY4ZmFjODE0OTJmZDRiNzMxMWUzZDcxMDExMWU2MjljMTUwYTJjYTA3NzkwN2NjOTZmMTYxNzlmNWZkMTYyIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=",

                // ✅ MATCHING SIGNATURE
                "VJ7aIBR3MpxmTMbQc5CfZNp52nijjk5OogyRCl9nv4HAPFcObshYMpbM37bKlQkn0bVmzVdVhDPvAUhUCECZvt91Zetvc5iJtz1Yr884VSYhGUrJWUgf2bESHPre5bo+O7HK2q6P6u4nIOd4RZa7zHbIDvYLq7YuwzIakVd5mUw8mvS/AkN2PXi9mqCNYYkogwgP4bXbjmOTAP94wZcXjR4nvX9PDGtzGvMM3ElQ14ZRrJiiPkR+eBR7MpSMZ0EoKEv7nhALmsFCVZE1B0vlGCF7n2dYNnyCpBNnj1QBROEV3HxbSaA5ifAo63lq/tt7hMcJ47p8Y6fikWAARWaNRE7W1+F068WSAn5kd8RSIBEMC3QRIAXohca+tAGHWX8UC5Z6NgyXuLEcz/EaTrCqYsO9bASOZrc80erAxsE9KdDWAs7Uxjt4IE6PoAKXysmM0q8YOjOt0eUXShFI/r7hPuSuNOV/aWl2hNWLmFV+KbMQK3NhVYP7hg7dVf4F4tTEhcdfPaP9pt4ZTQa94vdyNaHEUriUorICC1E9d0/envfUCsdP2vj22eWUmLC/x+EpAUubP8dt4gsKNegK9wcIF6PihlNteV2z3kM0W61AWrO5ap0HC9wPcIdZa+zz9+bj4FFtReGCsnqeGT5eMEsU5OID/oLSAUcOzfP5YpnTz3E=8.155"
            )
        ));

        // Apply anonymization
        player.setPlayerProfile(profile);
        player.setPlayerListName("Player");
        player.setDisplayName("Player");

        event.setJoinMessage(ChatColor.YELLOW + "Player has joined");
    }
}
