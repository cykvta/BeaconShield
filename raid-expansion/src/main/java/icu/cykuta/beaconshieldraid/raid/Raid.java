package icu.cykuta.beaconshieldraid.raid;

import icu.cykuta.beaconshield.beacon.BeaconShieldBlock;
import icu.cykuta.beaconshield.beacon.protection.ProtectedChunk;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * In-memory progress of a single protection being raided: which chunks
 * are captured, how far the contested ones have progressed and the
 * remaining nexus health. Raids are ephemeral — they live only while
 * raiding is active and are discarded on reset or server restart.
 */
public class Raid {

    private final String beaconId;

    /** Capture progress in seconds, keyed by chunk (only contested chunks). */
    private final Map<ProtectedChunk, Double> progress = new HashMap<>();

    /** Chunks that have been fully captured (protection suppressed). */
    private final Set<ProtectedChunk> captured = new HashSet<>();

    /** Nexus health; -1 means the nexus phase has not started/been touched. */
    private int nexusHealth = -1;

    /** Millis of the last nexus hit, for regeneration. */
    private long lastNexusDamage;

    /** The player who started the raid, or null for an admin-forced raid. */
    private UUID initiator;

    /** The attacking party. Only its members contribute to the raid. */
    private final Set<UUID> party = new LinkedHashSet<>();

    /** Players who requested to join, awaiting the leader's decision. */
    private final Set<UUID> joinRequests = new LinkedHashSet<>();

    /** True once the raid has been explicitly started (command or force). */
    private boolean active;

    /** Millis when the raid was created, for status/info. */
    private long startedAt;

    /** Millis a raider was last seen inside the protection (defenders-win timer). */
    private long lastRaiderSeen;

    /** Millis when the freeze (preparation) window ends; 0 = no freeze. */
    private long freezeEndsAt;

    /** Whether the FREEZE -> ACTIVE transition has been announced. */
    private boolean beganAnnounced;

    /** Where the raid was started, used for the join radius. */
    private String originWorld;
    private double originX, originY, originZ;

    public Raid(BeaconShieldBlock beacon) {
        this.beaconId = beacon.getId();
    }

    public String getBeaconId() {
        return beaconId;
    }

    // Party ------------------------------------------------------------

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public long getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(long startedAt) {
        this.startedAt = startedAt;
    }

    public long getLastRaiderSeen() {
        return lastRaiderSeen;
    }

    public void setLastRaiderSeen(long lastRaiderSeen) {
        this.lastRaiderSeen = lastRaiderSeen;
    }

    @org.jetbrains.annotations.Nullable
    public UUID getInitiator() {
        return initiator;
    }

    public void setInitiator(UUID initiator) {
        this.initiator = initiator;
    }

    public Set<UUID> getParty() {
        return party;
    }

    public boolean isInParty(UUID uuid) {
        return party.contains(uuid);
    }

    public void addToParty(UUID uuid) {
        party.add(uuid);
    }

    public void removeFromParty(UUID uuid) {
        party.remove(uuid);
    }

    public boolean isLeader(UUID uuid) {
        return uuid.equals(initiator);
    }

    // Join requests ----------------------------------------------------

    public void addRequest(UUID uuid) {
        joinRequests.add(uuid);
    }

    public boolean hasRequest(UUID uuid) {
        return joinRequests.contains(uuid);
    }

    public void removeRequest(UUID uuid) {
        joinRequests.remove(uuid);
    }

    public Set<UUID> getRequests() {
        return joinRequests;
    }

    // Freeze / preparation window --------------------------------------

    public long getFreezeEndsAt() {
        return freezeEndsAt;
    }

    public void setFreezeEndsAt(long freezeEndsAt) {
        this.freezeEndsAt = freezeEndsAt;
    }

    /**
     * Whether the raid is still in its freeze window (players may join,
     * capture has not started yet).
     */
    public boolean isInFreeze() {
        return System.currentTimeMillis() < freezeEndsAt;
    }

    public boolean isBeganAnnounced() {
        return beganAnnounced;
    }

    public void setBeganAnnounced(boolean beganAnnounced) {
        this.beganAnnounced = beganAnnounced;
    }

    // Origin (join radius) ---------------------------------------------

    public void setOrigin(Location location) {
        this.originWorld = location.getWorld().getName();
        this.originX = location.getX();
        this.originY = location.getY();
        this.originZ = location.getZ();
    }

    public void setOrigin(String world, double x, double y, double z) {
        this.originWorld = world;
        this.originX = x;
        this.originY = y;
        this.originZ = z;
    }

    /**
     * The location the raid was started from, or {@code null} if unknown or
     * its world is not loaded.
     */
    @Nullable
    public Location getOrigin() {
        if (originWorld == null || Bukkit.getWorld(originWorld) == null) {
            return null;
        }
        return new Location(Bukkit.getWorld(originWorld), originX, originY, originZ);
    }

    @Nullable
    public String getOriginWorld() {
        return originWorld;
    }

    public double getOriginX() {
        return originX;
    }

    public double getOriginY() {
        return originY;
    }

    public double getOriginZ() {
        return originZ;
    }

    public double getProgress(ProtectedChunk chunk) {
        return progress.getOrDefault(chunk, 0.0);
    }

    public void setProgress(ProtectedChunk chunk, double seconds) {
        if (seconds <= 0) {
            progress.remove(chunk);
        } else {
            progress.put(chunk, seconds);
        }
    }

    public boolean isCaptured(ProtectedChunk chunk) {
        return captured.contains(chunk);
    }

    public void markCaptured(ProtectedChunk chunk) {
        progress.remove(chunk);
        captured.add(chunk);
    }

    public Set<ProtectedChunk> getCapturedChunks() {
        return captured;
    }

    public int getCapturedCount() {
        return captured.size();
    }

    /**
     * Live view of the per-chunk capture progress, for persistence.
     */
    public Map<ProtectedChunk, Double> getProgressEntries() {
        return progress;
    }

    // Nexus ------------------------------------------------------------

    public boolean isNexusStarted() {
        return nexusHealth >= 0;
    }

    public int getNexusHealth() {
        return nexusHealth;
    }

    public void startNexus(int health) {
        this.nexusHealth = health;
        this.lastNexusDamage = System.currentTimeMillis();
    }

    /**
     * Restore a persisted nexus state (health and last-damage timestamp)
     * without touching the clock. Used when loading from disk.
     */
    public void restoreNexus(int health, long lastDamage) {
        this.nexusHealth = health;
        this.lastNexusDamage = lastDamage;
    }

    /**
     * Apply one hit to the nexus. Returns the remaining health.
     */
    public int damageNexus() {
        this.nexusHealth = Math.max(0, nexusHealth - 1);
        this.lastNexusDamage = System.currentTimeMillis();
        return nexusHealth;
    }

    /**
     * Fully regenerate the nexus after a stretch without damage.
     */
    public void regenNexus(int max) {
        if (nexusHealth >= 0 && nexusHealth < max) {
            nexusHealth = max;
            lastNexusDamage = System.currentTimeMillis();
        }
    }

    public long getLastNexusDamage() {
        return lastNexusDamage;
    }

    /**
     * A raid is idle (safe to drop) only when it was never activated and
     * holds no progress. Active raids (started or forced) always survive.
     */
    public boolean isIdle() {
        return !active && captured.isEmpty() && progress.isEmpty() && nexusHealth < 0;
    }
}
