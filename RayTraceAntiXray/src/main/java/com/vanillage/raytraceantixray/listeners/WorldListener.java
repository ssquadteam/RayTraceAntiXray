package com.vanillage.raytraceantixray.listeners;

import com.destroystokyo.paper.antixray.ChunkPacketBlockController;
import com.google.common.base.Preconditions;
import com.vanillage.raytraceantixray.RayTraceAntiXray;
import com.vanillage.raytraceantixray.antixray.ChunkPacketBlockControllerAntiXray;
import io.papermc.paper.configuration.type.EngineMode;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import org.bukkit.World;
import org.bukkit.configuration.Configuration;
import org.bukkit.craftbukkit.v1_19_R2.CraftWorld;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldUnloadEvent;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

public final class WorldListener implements Listener {
    private final RayTraceAntiXray plugin;

    public WorldListener(RayTraceAntiXray plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onWorldInit(WorldInitEvent event) {
        handleLoad(plugin, event.getWorld());
    }

    @EventHandler
    public void onWorldUnload(WorldUnloadEvent e) {
        handleUnload(plugin, e.getWorld());
    }

    public static void handleLoad(RayTraceAntiXray plugin, World w) {
        Configuration conf = plugin.getConfig();
        if (((CraftWorld) w).getHandle().paperConfig().anticheat.antiXray.enabled
                && ((CraftWorld) w).getHandle().paperConfig().anticheat.antiXray.engineMode == EngineMode.HIDE
                && conf.getBoolean("world-settings." + w.getName() + ".anti-xray.ray-trace", conf.getBoolean("world-settings.default.anti-xray.ray-trace"))) {
            boolean rayTraceThirdPerson = conf.getBoolean("world-settings." + w.getName() + ".anti-xray.ray-trace-third-person", conf.getBoolean("world-settings.default.anti-xray.ray-trace-third-person"));
            double rayTraceDistance = Math.max(conf.getDouble("world-settings." + w.getName() + ".anti-xray.ray-trace-distance", conf.getDouble("world-settings.default.anti-xray.ray-trace-distance")), 0.);
            int maxRayTraceBlockCountPerChunk = Math.max(conf.getInt("world-settings." + w.getName() + ".anti-xray.max-ray-trace-block-count-per-chunk", conf.getInt("world-settings.default.anti-xray.max-ray-trace-block-count-per-chunk")), 0);
            boolean rehideBlocks = plugin.getConfig().getBoolean("world-settings." + w.getName() + ".anti-xray.rehide-blocks", plugin.getConfig().getBoolean("world-settings.default.anti-xray.rehide-blocks"));
            List<String> rayTraceBlocks = conf.getList("world-settings." + w.getName() + ".anti-xray.ray-trace-blocks", conf.getList("world-settings.default.anti-xray.ray-trace-blocks")).stream().filter(o -> o != null).map(String::valueOf).collect(Collectors.toList());

            try {
                Preconditions.checkArgument(!(((CraftWorld) w).getHandle().chunkPacketBlockController instanceof ChunkPacketBlockControllerAntiXray), "World already has ChunkPacketBlockControllerAntiXray");

                Field chunkPacketBlockController = Level.class.getDeclaredField("chunkPacketBlockController");
                chunkPacketBlockController.setAccessible(true);
                Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
                theUnsafe.setAccessible(true);
                Unsafe unsafe = (Unsafe) theUnsafe.get(null);
                unsafe.putObject(((CraftWorld) w).getHandle(), unsafe.objectFieldOffset(chunkPacketBlockController), new ChunkPacketBlockControllerAntiXray(plugin, ((CraftWorld) w).getHandle().chunkPacketBlockController, rayTraceThirdPerson, rayTraceDistance, maxRayTraceBlockCountPerChunk, rehideBlocks, rayTraceBlocks.isEmpty() ? null : rayTraceBlocks, ((CraftWorld) w).getHandle(), MinecraftServer.getServer().executor));
            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public static void handleUnload(RayTraceAntiXray plugin, World w) {
        if (((CraftWorld) w).getHandle().chunkPacketBlockController instanceof ChunkPacketBlockControllerAntiXray) {
            try {
                Field chunkPacketBlockController = Level.class.getDeclaredField("chunkPacketBlockController");
                chunkPacketBlockController.setAccessible(true);
                Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
                theUnsafe.setAccessible(true);
                Unsafe unsafe = (Unsafe) theUnsafe.get(null);
                ChunkPacketBlockController oldController = ((ChunkPacketBlockControllerAntiXray) ((CraftWorld) w).getHandle().chunkPacketBlockController).getOldController();

                unsafe.putObject(((CraftWorld) w).getHandle(), unsafe.objectFieldOffset(chunkPacketBlockController), oldController);
            } catch (ReflectiveOperationException | SecurityException ex) {
                ex.printStackTrace();
            }
        }
    }
}
