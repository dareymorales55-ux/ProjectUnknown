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

        // Store real name (same logic as before)
        plugin.getPlayerConfig().set("players." + uuid + ".name", player.getName());
        plugin.savePlayerConfig();

        // Create anonymized profile
        PlayerProfile profile = Bukkit.createProfileExact(uuid, "Player");

        profile.setProperties(List.of(
            new ProfileProperty(
                "textures",
                "ewogICJ0aW1lc3RhbXAiIDogMTY5MTIxODk2MDIxMiwKICAicHJvZmlsZUlkIiA6ICJiNGJmZDZhNmRiZGQ0MDg2ODRhYmIzYzlmNDQyNmRiYSIsCiAgInByb2ZpbGVOYW1lIiA6ICJWZXJzYWNlNjciLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmYxZWZhN2YxODE3MTE3ZDZlMjQyOGY1YjM4OGEzNWI5MzcwMWEwN2ViOGUzNWEzZmFkY2ZhZmVjMmRjNzdlIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=",
                "lBwv7s/JkrSLa5sHzjx6JUBz7ALWa2qgW/2ugvktSxY8fD4gPYuMgvrkhJ3I9YB+87H2PMPwsW8hjysz42cSvTl8G0MbnH4PpEZIiiw7be25wSaVSeTuN2E1WQeKGwkJ/O1CXniED628ElBY1WyGkC7Dtea5pKHVkud3p+3LG6ZiTOGAS1QkMV9Iu7EZ9ZvRnhf7F8bd5EolTEWDtlAuxNuS1E53xtlIRPaHnMZszA31HhoeeRRPudS1w6uLMwcaO8hGnxYxQKVauZ/yv8km/Bi48cUGD2j7TLQTw66UzniiDkLYfiDHNx3HtPCVB7ICrPqFVa82zcA4RnJiqyeKEuyoRiwcXv9HDSTdPG8PrTLcPdDNvOqN0SmXHgGZhUIMDKv3QXEHOh6SpSBF5u54bkHr/MEXxhO6WachRDv+/hA8OklOI99V2fElMl+3A5v5v+TqgVsbJTL7fQG2nWsGAqhf7JddnxEnMxkCh1At9CiEnEwzUq4/XHGO+l6iVxZMIHyTzZlQbZfc1kOS1xIBSqR+TU4dNPdQ39hKqbQGh+eDjL86Ql7Chf3YLX957DPAft6Pccj9oGMsSsJ86/y7xMcqar2QVocsAgK2hipyuQ34mMd2JHYvkp/3siUy3pXDscN4c06l3FtmNQ/WgvRqRyulBoc9Ym3R9+ZZzQglUnE="
            )
        ));

        // Apply anonymization
        player.setPlayerProfile(profile);
        player.setPlayerListName("Player");
        player.setDisplayName("Player");

        event.setJoinMessage(ChatColor.YELLOW + "Player has joined");
    }
}
