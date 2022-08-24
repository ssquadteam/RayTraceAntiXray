package com.vanillage.raytraceantixray.listeners;

import com.vanillage.raytraceantixray.RayTraceAntiXray;
import com.vanillage.raytraceantixray.data.PlayerData;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PlayerListener implements Listener {
    private final RayTraceAntiXray plugin;

    public PlayerListener(RayTraceAntiXray plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        handleJoin(plugin, event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getPlayerData().remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        PlayerData playerData = plugin.getPlayerData().get(event.getPlayer().getUniqueId());
        if (playerData != null) {
            Location location = event.getTo();

            if (location.getWorld().equals(playerData.getLocations().get(0).getWorld())) {
                location = location.clone();
                location.setY(location.getY() + event.getPlayer().getEyeHeight());
                playerData.setLocations(plugin.getLocations(event.getPlayer(), location));
            }
        }
    }

    public static void handleJoin(RayTraceAntiXray plugin, Player player) {
        plugin.getPlayerData().put(player.getUniqueId(), new PlayerData(plugin.getLocations(player, player.getEyeLocation())));
    }

}
