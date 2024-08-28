package com.vanillage.raytraceantixray.data;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Objects;

public final class VectorialLocation {
    private final Reference<World> world;
    private final Vector vector;
    private final Vector direction;

    public VectorialLocation(World world, Vector vector, Vector direction) {
        this.world = new WeakReference<>(world);
        this.vector = vector;
        this.direction = direction;
    }

    public VectorialLocation(VectorialLocation location) {
        world = location.world;
        vector = location.getVector().clone();
        direction = location.getDirection().clone();
    }

    public VectorialLocation(Location location) {
        this(location.getWorld(), location.toVector(), location.getDirection());
    }

    public World getWorld() {
        return world.get();
    }

    public Vector getVector() {
        return vector;
    }

    public Vector getDirection() {
        return direction;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        VectorialLocation that = (VectorialLocation) object;
        return Objects.equals(vector, that.vector) && Objects.equals(direction, that.direction) && Objects.equals(world, that.world);
    }

    @Override
    public int hashCode() {
        return Objects.hash(world, vector, direction);
    }

}
