package icu.cykuta.beaconshield.events;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockPlaceEvent;
import org.jetbrains.annotations.NotNull;

public class BeaconShieldPlaceEvent extends Event implements Cancellable {
    private boolean isCancelled = false;
    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private final BlockPlaceEvent baseEvent;

    public BeaconShieldPlaceEvent(BlockPlaceEvent baseEvent) {
        this.baseEvent = baseEvent;
    }

    public Player getPlayer() {
        return this.baseEvent.getPlayer();
    }

    public Location getLocation() {
        return this.baseEvent.getBlock().getLocation();
    }

    public Block getBlock() {
        return this.baseEvent.getBlock();
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        isCancelled = b;
        this.baseEvent.setCancelled(b);
    }

    public @NotNull HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }
}
