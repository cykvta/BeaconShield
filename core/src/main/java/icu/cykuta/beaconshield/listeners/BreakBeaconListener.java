package icu.cykuta.beaconshield.listeners;

import icu.cykuta.beaconshield.events.internal.InternalBeaconShieldBreakEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class BreakBeaconListener implements Listener {

    /**
     * This event is called when a player breaks a beacon shield.
     * @param event The event.
     */
    @EventHandler
    public void onBeaconShieldBreak(InternalBeaconShieldBreakEvent event) {
        event.setCancelled(true);
    }
}
