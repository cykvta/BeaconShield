package icu.cykuta.beaconshield.events;

import icu.cykuta.beaconshield.beacon.BeaconShieldBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class BeaconShieldDestroyedEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final BeaconShieldBlock beaconShieldBlock;
    private final Player player;

    public BeaconShieldDestroyedEvent(Player player, BeaconShieldBlock beaconShieldBlock) {
        this.beaconShieldBlock = beaconShieldBlock;
        this.player = player;
    }

    public BeaconShieldBlock getBeaconShieldBlock() {
        return beaconShieldBlock;
    }

    public Player getPlayer() {
        return player;
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
