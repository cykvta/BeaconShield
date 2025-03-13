package icu.cykuta.beaconshield.data;

import icu.cykuta.beaconshield.BeaconShield;
import icu.cykuta.beaconshield.beacon.BeaconShieldBlock;
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
}
