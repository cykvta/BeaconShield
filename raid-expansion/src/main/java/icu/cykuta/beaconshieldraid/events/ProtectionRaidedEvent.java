package icu.cykuta.beaconshieldraid.events;

import icu.cykuta.beaconshield.beacon.BeaconShieldBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Fired right before a protection falls: every chunk has been captured
 * and the nexus (beacon) has been broken. The beacon is destroyed
 * immediately after this event.
 */
public class ProtectionRaidedEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final BeaconShieldBlock beacon;
    private final Player raider;

    public ProtectionRaidedEvent(BeaconShieldBlock beacon, @Nullable Player raider) {
        this.beacon = beacon;
        this.raider = raider;
    }

    public BeaconShieldBlock getBeacon() {
        return beacon;
    }

    /**
     * The player that landed the final nexus hit, or {@code null}.
     */
    @Nullable
    public Player getRaider() {
        return raider;
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
