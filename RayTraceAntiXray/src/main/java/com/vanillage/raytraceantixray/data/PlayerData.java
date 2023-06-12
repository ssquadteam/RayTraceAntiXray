package com.vanillage.raytraceantixray.data;

import com.destroystokyo.paper.antixray.ChunkPacketBlockController;
import com.vanillage.raytraceantixray.antixray.ChunkPacketBlockControllerAntiXray;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class PlayerData {
    private volatile List<? extends Location> locations;
    private final Map<ChunkPos, ChunkBlocks> chunks = new ConcurrentHashMap<>();
    private final Queue<Result> resultQueue = new ConcurrentLinkedQueue<>();
    private Runnable runnable;

    public PlayerData(Player player) {
        this(resolveLocations(player, player.getLocation()));
    }

    public PlayerData(List<? extends Location> locations) {
        this.locations = locations;
    }

    public void updateLocations(Player player, Location to) {
        locations = resolveLocations(player, to);
    }

    public List<? extends Location> getLocations() {
        return locations;
    }

    public Map<ChunkPos, ChunkBlocks> getChunks() {
        return chunks;
    }

    public Queue<Result> getResultQueue() {
        return resultQueue;
    }

    public Runnable getRunnable() {
        return runnable;
    }

    public void setRunnable(Runnable runnable) {
        this.runnable = runnable;
    }

    public static List<Location> resolveLocations(Player player, Location location) {
        location = location.clone().clone().add(0, player.getEyeHeight(), 0);

        ChunkPacketBlockController chunkPacketBlockController = ((CraftWorld) location.getWorld()).getHandle().chunkPacketBlockController;
        if (chunkPacketBlockController instanceof ChunkPacketBlockControllerAntiXray && ((ChunkPacketBlockControllerAntiXray) chunkPacketBlockController).rayTraceThirdPerson) {
            Vector direction = location.getDirection();
            return Arrays.asList(location, move(player, location, direction), move(player, location, direction.multiply(-1.)).setDirection(direction));
        }

        return Collections.singletonList(location);
    }

    private static Location move(Player player, Location location, Vector direction) {
        return location.clone().subtract(direction.clone().multiply(getMaxZoom(player, location, direction, 4.)));
    }

    private static double getMaxZoom(Player player, Location location, Vector direction, double maxZoom) {
        Vec3 position = new Vec3(location.getX(), location.getY(), location.getZ());

        for (int i = 0; i < 8; i++) {
            float edgeX = (float) ((i & 1) * 2 - 1);
            float edgeY = (float) ((i >> 1 & 1) * 2 - 1);
            float edgeZ = (float) ((i >> 2 & 1) * 2 - 1);
            edgeX *= 0.1f;
            edgeY *= 0.1f;
            edgeZ *= 0.1f;
            Vec3 edge = position.add(edgeX, edgeY, edgeZ);
            Vec3 edgeMoved = new Vec3(position.x - direction.getX() * maxZoom + (double) edgeX + (double) edgeZ, position.y - direction.getY() * maxZoom + (double) edgeY, position.z - direction.getZ() * maxZoom + (double) edgeZ);
            BlockHitResult result = ((CraftWorld) location.getWorld()).getHandle().clip(new ClipContext(edge, edgeMoved, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, ((CraftEntity) player).getHandle()));

            if (result.getType() != HitResult.Type.MISS) {
                double zoom = result.getLocation().distanceTo(position);

                if (zoom < maxZoom) {
                    maxZoom = zoom;
                }
            }
        }

        return maxZoom;
    }

    public static float getPlayerEyeHeight(Pose pose) {
        switch (pose) {
            case SWIMMING:
            case FALL_FLYING:
            case SPIN_ATTACK:
                return 0.4F;
            case SNEAKING:
                return 1.27F;
            default:
                return 1.62F;
        }
    }

}
