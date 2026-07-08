package icu.cykuta.beaconshield.beacon.protection;

import icu.cykuta.beaconshield.BeaconShield;
import org.bukkit.*;
import org.bukkit.block.Block;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ProtectedChunk implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private final int x, z;
    private final String world;

    public ProtectedChunk(int x, int z, World world) {
        this.x = x;
        this.z = z;
        this.world = world.getName();
    }

    public ProtectedChunk(Chunk chunk) {
        this.x = chunk.getX();
        this.z = chunk.getZ();
        this.world = chunk.getWorld().getName();
    }

    /**
     * Get the edges of the chunk.
     * @return A list of locations representing the edges of the chunk.
     */
    public List<Location> getChunkEdges() {
        return getOuterEdges(List.of());
    }

    /**
     * Get the surface blocks of the chunk borders, skipping the sides
     * that touch another chunk of the given territory. Corner blocks
     * are included when at least one of their two sides is exposed.
     *
     * @param territory The chunks of the same protection.
     * @return A list of locations representing the exposed edges.
     */
    public List<Location> getOuterEdges(Collection<ProtectedChunk> territory) {
        World world = getWorld();
        boolean north = !territory.contains(new ProtectedChunk(x, z - 1, world));
        boolean south = !territory.contains(new ProtectedChunk(x, z + 1, world));
        boolean west = !territory.contains(new ProtectedChunk(x - 1, z, world));
        boolean east = !territory.contains(new ProtectedChunk(x + 1, z, world));

        // A set because the corner blocks belong to two sides at once
        Set<Location> edges = new LinkedHashSet<>();
        int minX = x << 4;
        int minZ = z << 4;

        for (int i = 0; i < 16; i++) {
            if (north) addHeightMapBlock(world, minX + i, minZ, edges);
            if (south) addHeightMapBlock(world, minX + i, minZ + 15, edges);
            if (west) addHeightMapBlock(world, minX, minZ + i, edges);
            if (east) addHeightMapBlock(world, minX + 15, minZ + i, edges);
        }

        return new ArrayList<>(edges);
    }

    /**
     * Get the x coordinate of the chunk.
     * @return The x coordinate of the chunk.
     */
    public int getX() {
        return x;
    }

    /**
     * Get the z coordinate of the chunk.
     * @return The z coordinate of the chunk.
     */
    public int getZ() {
        return z;
    }

    /**
     * Get the world of the chunk.
     * @return The world of the chunk.
     */
    public World getWorld() {
        return BeaconShield.getPlugin().getServer().getWorld(world);
    }

    /**
     * Get the name of the world of the chunk.
     * @return The world name.
     */
    public String getWorldName() {
        return world;
    }

    /**
     * Get the chunk.
     * @return The chunk.
     */
    public Chunk toChunk() {
        return getWorld().getChunkAt(x, z);
    }

    /**
     * Add a block to the list of edges.
     * @param world The world of the block.
     * @param x The x coordinate of the block.
     * @param z The z coordinate of the block.
     * @param edges The list of edges.
     */
    private static void addHeightMapBlock(World world, int x, int z, Collection<Location> edges) {
        int height = world.getHighestBlockYAt(x, z, HeightMap.MOTION_BLOCKING_NO_LEAVES);
        Block block = world.getBlockAt(x, height, z);
        edges.add(block.getLocation());
    }

    /**
     * Check if a location is within the bounds of a chunk.
     * @param location The location to check.
     * @return True if the location is within the chunk, false otherwise.
     */
    public boolean isLocationInChunk(Location location) {
        int chunkX = this.getX();
        int chunkZ = this.getZ();

        // Calculate the bounds of the chunk
        int minX = chunkX << 4; // chunkX * 16
        int maxX = minX + 15;
        int minZ = chunkZ << 4; // chunkZ * 16
        int maxZ = minZ + 15;

        // Check if the location is within the chunk bounds
        int locX = location.getBlockX();
        int locZ = location.getBlockZ();

        return locX >= minX && locX <= maxX && locZ >= minZ && locZ <= maxZ;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ProtectedChunk chunk)) {
            return false;
        }
        return x == chunk.x && z == chunk.z && world.equals(chunk.world);
    }

    @Override
    public int hashCode() {
        int result = world.hashCode();
        result = 31 * result + x;
        result = 31 * result + z;
        return result;
    }

}
