package icu.cykuta.beaconshield.events;

import icu.cykuta.beaconshield.beacon.BeaconShieldBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerProtectedChunkGatewayEvent extends Event {
    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private final BeaconShieldBlock beacon;
    private final Player player;
    private final Action action;

    public enum Action {
        ENTER,
        LEAVE
    }

    public PlayerProtectedChunkGatewayEvent(BeaconShieldBlock beacon, Player player, Action action) {
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
