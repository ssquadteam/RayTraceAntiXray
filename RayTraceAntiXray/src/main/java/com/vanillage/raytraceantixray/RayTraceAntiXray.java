package com.vanillage.raytraceantixray;

import com.comphenix.protocol.ProtocolLibrary;
import com.google.common.collect.MapMaker;
import com.vanillage.raytraceantixray.antixray.ChunkPacketBlockControllerAntiXray;
import com.vanillage.raytraceantixray.commands.RayTraceAntiXrayTabExecutor;
import com.vanillage.raytraceantixray.data.ChunkBlocks;
import com.vanillage.raytraceantixray.data.PlayerData;
import com.vanillage.raytraceantixray.listeners.PacketListener;
import com.vanillage.raytraceantixray.listeners.PlayerListener;
import com.vanillage.raytraceantixray.listeners.WorldListener;
import com.vanillage.raytraceantixray.tasks.RayTraceTimerTask;
import com.vanillage.raytraceantixray.tasks.UpdateBukkitRunnable;
import io.papermc.paper.chunk.PlayerChunkLoader;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Map;
import java.util.Timer;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class RayTraceAntiXray extends JavaPlugin {
    private volatile boolean running = false;
    private volatile boolean timings = false;
    private final Map<ClientboundLevelChunkWithLightPacket, ChunkBlocks> packetChunkBlocksCache = new MapMaker().weakKeys().makeMap();
    private final Map<UUID, PlayerData> playerData = new ConcurrentHashMap<>();
    private ExecutorService executorService;
    private Timer timer;

    @Override
    public void onEnable() {
        if (!new File(getDataFolder(), "README.txt").exists()) {
            saveResource("README.txt", false);
        }

        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        reloadConfig();

        for (World w : Bukkit.getWorlds()) {
            WorldListener.handleLoad(this, w);
        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            PlayerListener.handleJoin(this, p);
        }

        // saveConfig();
        // Initialize stuff.
        running = true;
        executorService = Executors.newFixedThreadPool(Math.max(getConfig().getInt("settings.anti-xray.ray-trace-threads"), 1));
        timer = new Timer(true);
        timer.schedule(new RayTraceTimerTask(this), 0L, Math.max(getConfig().getLong("settings.anti-xray.ms-per-ray-trace-tick"), 1L));
        new UpdateBukkitRunnable(this).runTaskTimer(this, 0L, Math.max(getConfig().getLong("settings.anti-xray.update-ticks"), 1L));
        // Register events.
        getServer().getPluginManager().registerEvents(new WorldListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketListener(this));
        // registerCommands();
        getCommand("raytraceantixray").setExecutor(new RayTraceAntiXrayTabExecutor(this));
        getLogger().info(getDescription().getFullName() + " enabled");
    }

    @Override
    public void onDisable() {
        // unregisterCommands();
        // Cleanup stuff.
        ProtocolLibrary.getProtocolManager().removePacketListeners(this);
        HandlerList.unregisterAll(this);

        running = false;
        timer.cancel();
        executorService.shutdownNow();

        try {
            executorService.awaitTermination(1000L, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }

        for (World w : Bukkit.getWorlds()) {
            WorldListener.handleUnload(this, w);
        }

        packetChunkBlocksCache.clear();
        playerData.clear();
        getLogger().info(getDescription().getFullName() + " disabled");
    }

    public void reload() {
        onDisable();
        onEnable();
        getLogger().info(getDescription().getFullName() + " reloaded");
    }

    public void reloadChunks(Iterable<Player> players) {
        for (Player bp : players) {
            ServerPlayer p = ((CraftPlayer) bp).getHandle();
            PlayerChunkLoader playerChunkManager = ((ServerLevel) p.level).getChunkSource().chunkMap.playerChunkManager;
            playerChunkManager.removePlayer(p);
            playerChunkManager.addPlayer(p);
        }
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isTimings() {
        return timings;
    }

    public void setTimings(boolean timings) {
        this.timings = timings;
    }

    public Map<ClientboundLevelChunkWithLightPacket, ChunkBlocks> getPacketChunkBlocksCache() {
        return packetChunkBlocksCache;
    }

    public Map<UUID, PlayerData> getPlayerData() {
        return playerData;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public static boolean hasController(World world) {
        return ((CraftWorld) world).getHandle().chunkPacketBlockController instanceof ChunkPacketBlockControllerAntiXray;
    }

}
