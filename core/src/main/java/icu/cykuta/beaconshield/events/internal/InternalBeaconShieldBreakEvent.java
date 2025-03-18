package icu.cykuta.beaconshield.events.internal;

import icu.cykuta.beaconshield.beacon.BeaconShieldBlock;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Internal events is not supposed to be used by other plugins.
 * They are used to handle the internal logic of BeaconShield.
 */

@ApiStatus.Internal
public class InternalBeaconShieldBreakEvent extends Event implements Cancellable {
    private boolean isCancelled = false;
    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private final BlockBreakEvent baseEvent;

    public InternalBeaconShieldBreakEvent(BlockBreakEvent baseEvent) {
        this.baseEvent = baseEvent;
    }

    public BeaconShieldBlock getBeaconShieldBlock() {
        return BeaconShieldBlock.getBeaconShieldBlock(baseEvent.getBlock());
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        isCancelled = cancel;
        baseEvent.setCancelled(cancel);
    }

    public @NotNull HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }
}
