package com.vanillage.raytraceantixray.listeners;

import com.vanillage.raytraceantixray.RayTraceAntiXray;
import com.vanillage.raytraceantixray.data.PlayerData;
import com.vanillage.raytraceantixray.data.VectorialLocation;
import com.vanillage.raytraceantixray.net.OutboundHandler;
import com.vanillage.raytraceantixray.tasks.RayTraceCallable;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PlayerListener implements Listener {
    private final RayTraceAntiXray plugin;

    public PlayerListener(RayTraceAntiXray plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerLogin(PlayerLoginEvent e) {
        OutboundHandler.attach(plugin, e.getPlayer(), e.getAddress());
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
        Location to = event.getTo();

        if (to.getWorld().equals(playerData.getLocations()[0].getWorld())) {
            VectorialLocation location = new VectorialLocation(to);
            location.getVector().setY(location.getVector().getY() + event.getPlayer().getEyeHeight());
            playerData.setLocations(plugin.getLocations(event.getPlayer(), location));
        }
    }

    public static void handleJoin(RayTraceAntiXray plugin, Player player) {
        PlayerData playerData = new PlayerData(plugin.getLocations(player, new VectorialLocation(player.getEyeLocation())));
        playerData.setCallable(new RayTraceCallable(playerData));
        plugin.getPlayerData().put(player.getUniqueId(), playerData);
    }

}
