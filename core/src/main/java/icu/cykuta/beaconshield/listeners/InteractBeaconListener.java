package icu.cykuta.beaconshield.listeners;

import icu.cykuta.beaconshield.beacon.protection.PlayerRole;
import icu.cykuta.beaconshield.data.BeaconHandler;
import icu.cykuta.beaconshield.events.internal.InternalBeaconShieldInteractEvent;
import icu.cykuta.beaconshield.utils.Chat;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;

public class InteractBeaconListener implements Listener {

    /**
     * This event is called when a player interacts with a beacon shield block.
     */
    @EventHandler
    public void onPlayerInteractBlock(InternalBeaconShieldInteractEvent event) {
        // Check if player has permission to interact with the beacon shield block
        if (!event.getBeaconShieldBlock().hasPermissionLevel(event.getPlayer(), PlayerRole.OFFICER)) {
            Chat.send(event.getPlayer(), "no-permission-to-interact");
            return;
        }

        BeaconHandler beaconHandler = BeaconHandler.getInstance();
        Inventory inv = beaconHandler.getInventory(event.getBeaconShieldBlock());

        event.getPlayer().openInventory(inv);
    }
}
