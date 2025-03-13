package icu.cykuta.beaconshield.listeners;

import icu.cykuta.beaconshield.BeaconShield;
import icu.cykuta.beaconshield.beacon.BeaconShieldBlock;
import icu.cykuta.beaconshield.data.BeaconDataManager;
import icu.cykuta.beaconshield.data.ProtectionHandler;
import icu.cykuta.beaconshield.events.BeaconShieldPlaceEvent;
import icu.cykuta.beaconshield.utils.Chat;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlaceBeaconListener implements Listener {
    @EventHandler
    public void onBeaconShieldPlace(BeaconShieldPlaceEvent event) {
        Block block = event.getBlock();
        Location location = block.getLocation();

        // Check if the current chunk is a protected chunk
        if(ProtectionHandler.isChunkProtected(location.getChunk())) {
            Chat.send(event.getPlayer(), "cant-place-shield");
            event.setCancelled(true);
            return;
        }
        BeaconDataManager beaconDataManager = BeaconShield.getPlugin().getBeaconDataManager();
        FileConfiguration config = BeaconShield.getPlugin().getConfig();
        int maxBeaconShieldBlocks = config.getInt("max-beacons-per-player");

        // Check has more than maxBeaconShieldBlocks
        if(beaconDataManager.getBeaconShieldBlocksByOwner(event.getPlayer()).size() >= maxBeaconShieldBlocks) {
            Chat.send(event.getPlayer(), "max-beacons-reached");
            event.setCancelled(true);
            return;
        }

        // Create a new BeaconShieldBlock
        BeaconShieldBlock beaconShieldBlock = new BeaconShieldBlock(block, event.getPlayer());

        // Save the BeaconShieldBlock
        beaconDataManager.addBeaconShieldBlock(beaconShieldBlock);

        // Start the BeaconShieldBlock
        beaconShieldBlock.place();

        // Play sound
        event.getPlayer().playSound(event.getPlayer().getLocation(), "block.beacon.activate", 1, 1);
    }
}
