package icu.cykuta.beaconshield.data;

import icu.cykuta.beaconshield.beacon.BeaconShieldBlock;
import icu.cykuta.beaconshield.beacon.protection.ProtectedChunk;
import org.bukkit.Chunk;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class ProtectionHandler {
    private static final Map<Chunk, BeaconShieldBlock> chunkBeaconMap = new HashMap<>();

    public static void addChunk(Chunk chunk, BeaconShieldBlock beaconShieldBlock) {
        chunkBeaconMap.put(chunk, beaconShieldBlock);
    }

    public static void removeChunk(Chunk chunk) {
        chunkBeaconMap.remove(chunk);
    }

    public static boolean isChunkProtected(Chunk chunk) {
        return chunkBeaconMap.containsKey(chunk);
    }

    @Nullable
    public static BeaconShieldBlock getBeacon(Chunk chunk) {
        return chunkBeaconMap.get(chunk);
    }

    /**
     * Register all chunks for a beacon
     * @param beacon Beacon to register
     */
    public static void registerAllChunksForBeacon(BeaconShieldBlock beacon) {
        for (ProtectedChunk protectedChunk : beacon.getProtectedChunks()) {
            Chunk chunk = protectedChunk.toChunk();
            ProtectionHandler.addChunk(chunk, beacon);
        }
    }

    /**
     * Unregister all chunks for a beacon
     * @param beacon Beacon to unregister
     */
    public static void unregisterAllChunksForBeacon(BeaconShieldBlock beacon) {
        for (ProtectedChunk protectedChunk : beacon.getProtectedChunks()) {
            Chunk chunk = protectedChunk.toChunk();
            ProtectionHandler.removeChunk(chunk);
        }
    }
}
