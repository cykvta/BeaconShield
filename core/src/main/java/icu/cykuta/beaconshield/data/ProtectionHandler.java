package icu.cykuta.beaconshield.data;

import icu.cykuta.beaconshield.beacon.BeaconShieldBlock;
import icu.cykuta.beaconshield.beacon.protection.ProtectedChunk;
import org.bukkit.Chunk;
import org.bukkit.World;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Global index of protected chunks. Chunks are keyed by coordinates
 * (world name, x, z) so lookups never load a chunk.
 */
public class ProtectionHandler {

    private record ChunkKey(String world, int x, int z) {
        static ChunkKey of(Chunk chunk) {
            return new ChunkKey(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
        }

        static ChunkKey of(ProtectedChunk chunk) {
            return new ChunkKey(chunk.getWorldName(), chunk.getX(), chunk.getZ());
        }
    }

    private static final Map<ChunkKey, BeaconShieldBlock> protectedChunks = new HashMap<>();

    public static void addChunk(ProtectedChunk chunk, BeaconShieldBlock beacon) {
        protectedChunks.put(ChunkKey.of(chunk), beacon);
    }

    public static void removeChunk(ProtectedChunk chunk) {
        protectedChunks.remove(ChunkKey.of(chunk));
    }

    public static boolean isChunkProtected(Chunk chunk) {
        return protectedChunks.containsKey(ChunkKey.of(chunk));
    }

    public static boolean isChunkProtected(ProtectedChunk chunk) {
        return protectedChunks.containsKey(ChunkKey.of(chunk));
    }

    @Nullable
    public static BeaconShieldBlock getBeacon(Chunk chunk) {
        return protectedChunks.get(ChunkKey.of(chunk));
    }

    @Nullable
    public static BeaconShieldBlock getBeacon(ProtectedChunk chunk) {
        return protectedChunks.get(ChunkKey.of(chunk));
    }

    @Nullable
    public static BeaconShieldBlock getBeacon(World world, int chunkX, int chunkZ) {
        return protectedChunks.get(new ChunkKey(world.getName(), chunkX, chunkZ));
    }

    /**
     * Register all protected chunks of a beacon.
     */
    public static void registerAllChunksForBeacon(BeaconShieldBlock beacon) {
        for (ProtectedChunk chunk : beacon.getProtectedChunks()) {
            addChunk(chunk, beacon);
        }
    }

    /**
     * Unregister all protected chunks of a beacon.
     */
    public static void unregisterAllChunksForBeacon(BeaconShieldBlock beacon) {
        for (ProtectedChunk chunk : beacon.getProtectedChunks()) {
            removeChunk(chunk);
        }
    }
}
