package icu.cykuta.beaconshield.events;

import icu.cykuta.beaconshield.beacon.BeaconShieldBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

public class BeaconShieldInteractEvent extends Event {
    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private final PlayerInteractEvent baseEvent;

    public BeaconShieldInteractEvent(PlayerInteractEvent baseEvent) {
        this.baseEvent = baseEvent;
    }

    public BeaconShieldBlock getBeaconShieldBlock() {
        return BeaconShieldBlock.getBeaconShieldBlock(baseEvent.getClickedBlock());
    }

    public Player getPlayer() {
        return baseEvent.getPlayer();
    }

    public @NotNull HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }
}
