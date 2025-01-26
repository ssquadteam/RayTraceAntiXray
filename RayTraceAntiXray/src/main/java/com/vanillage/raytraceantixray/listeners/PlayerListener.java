package com.vanillage.raytraceantixray.listeners;

import com.vanillage.raytraceantixray.RayTraceAntiXray;
import com.vanillage.raytraceantixray.data.PlayerData;
import com.vanillage.raytraceantixray.data.VectorialLocation;
import com.vanillage.raytraceantixray.net.DuplexHandlerImpl;
import com.vanillage.raytraceantixray.tasks.RayTraceCallable;
import com.vanillage.raytraceantixray.tasks.UpdateBukkitRunnable;
import com.vanillage.raytraceantixray.util.BukkitUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PlayerListener implements Listener {
    private final RayTraceAntiXray plugin;

    public PlayerListener(RayTraceAntiXray plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerLogin(PlayerLoginEvent e) {
        if (plugin.validatePlayer(e.getPlayer())) {
            new DuplexHandlerImpl(plugin, e.getPlayer())
                    .attach(e.getAddress());
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (!plugin.validatePlayer(player)) {
            return;
        }

        PlayerData playerData = new PlayerData(RayTraceAntiXray.getLocations(player, new VectorialLocation(player.getEyeLocation())));
        playerData.setCallable(new RayTraceCallable(plugin, playerData));
        plugin.getPlayerData().put(event.getPlayer().getUniqueId(), playerData);

        if (BukkitUtil.IS_FOLIA) {
            event.getPlayer().getScheduler().runAtFixedRate(plugin, new UpdateBukkitRunnable(plugin, event.getPlayer()), null, 1L, plugin.getUpdateTicks());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getPlayerData().remove(event.getPlayer().getUniqueId());
    }

}
