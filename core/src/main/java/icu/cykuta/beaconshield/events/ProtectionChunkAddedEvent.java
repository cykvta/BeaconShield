package icu.cykuta.beaconshield.events;

import icu.cykuta.beaconshield.beacon.BeaconShieldBlock;
import icu.cykuta.beaconshield.beacon.protection.ProtectedChunk;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ProtectionChunkAddedEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final ProtectedChunk protectedChunk;
    private final BeaconShieldBlock beaconShieldBlock;

    public ProtectionChunkAddedEvent(ProtectedChunk protectedChunk, BeaconShieldBlock beaconShieldBlock) {
        this.protectedChunk = protectedChunk;
        this.beaconShieldBlock = beaconShieldBlock;
    }

    public ProtectedChunk getProtectedChunk() {
        return protectedChunk;
    }

    public BeaconShieldBlock getBeaconShieldBlock() {
        return beaconShieldBlock;
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
