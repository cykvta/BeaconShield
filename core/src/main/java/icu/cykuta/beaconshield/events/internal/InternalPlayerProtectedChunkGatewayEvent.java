package icu.cykuta.beaconshield.events.internal;

import icu.cykuta.beaconshield.beacon.BeaconShieldBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Internal events is not supposed to be used by other plugins.
 * They are used to handle the internal logic of BeaconShield.
 */

@ApiStatus.Internal
public class InternalPlayerProtectedChunkGatewayEvent extends Event {
    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private final BeaconShieldBlock beacon;
    private final Player player;
    private final Action action;

    public enum Action {
        ENTER,
        LEAVE
    }

    public InternalPlayerProtectedChunkGatewayEvent(BeaconShieldBlock beacon, Player player, Action action) {
        this.beacon = beacon;
        this.player = player;
        this.action = action;
    }

    public BeaconShieldBlock getBeacon() {
        return beacon;
    }

    public Player getPlayer() {
        return player;
    }

    public Action getAction() {
        return action;
    }

    public @NotNull HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }
}
