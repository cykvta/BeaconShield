package icu.cykuta.beaconshieldraid.events;

import icu.cykuta.beaconshield.beacon.BeaconShieldBlock;
import icu.cykuta.beaconshield.beacon.protection.ProtectedChunk;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Fired when attackers finish capturing a chunk of a protection during a
 * raid. The chunk's protection is already suppressed when this fires.
 */
public class ChunkCapturedEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final BeaconShieldBlock beacon;
    private final ProtectedChunk chunk;

    public ChunkCapturedEvent(BeaconShieldBlock beacon, ProtectedChunk chunk) {
        this.beacon = beacon;
        this.chunk = chunk;
    }

    public BeaconShieldBlock getBeacon() {
        return beacon;
    }

    public ProtectedChunk getChunk() {
        return chunk;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
