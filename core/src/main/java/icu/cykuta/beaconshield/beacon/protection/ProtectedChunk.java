package icu.cykuta.beaconshield.beacon.protection;

import icu.cykuta.beaconshield.BeaconShield;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
        List<Location> edges = new ArrayList<>();
        Chunk chunk = toChunk();
        World world = chunk.getWorld();
        int chunkX = chunk.getX() << 4;
        int chunkZ = chunk.getZ() << 4;

        for (int x = 0; x < 16; x++) {
            addHeightMapBlock(world, chunkX + x, chunkZ, edges);
            addHeightMapBlock(world, chunkX + x, chunkZ + 15, edges);
        }

        for (int z = 1; z < 15; z++) {
            addHeightMapBlock(world, chunkX, chunkZ + z, edges);
            addHeightMapBlock(world, chunkX + 15, chunkZ + z, edges);
        }

        return edges;
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
    private static void addHeightMapBlock(World world, int x, int z, List<Location> edges) {
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

    public void preview(Material material, Player player) {
        List<Location> highestEdges = getChunkEdges();
        highestEdges.forEach(edge -> player.sendBlockChange(edge, material.createBlockData()));

        // Revert the changes after 5 seconds
        BeaconShield.getPlugin().getServer().getScheduler().runTaskLater(BeaconShield.getPlugin(), () -> {
            highestEdges.forEach(edge -> player.sendBlockChange(edge, edge.getBlock().getBlockData()));
        }, 5 * 20);
    }

}
