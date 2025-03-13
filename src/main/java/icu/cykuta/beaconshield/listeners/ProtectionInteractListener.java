package icu.cykuta.beaconshield.listeners;

import icu.cykuta.beaconshield.beacon.BeaconShieldBlock;
import icu.cykuta.beaconshield.data.ProtectionHandler;
import icu.cykuta.beaconshield.utils.Chat;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class ProtectionInteractListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) {
            return;
        }

        // If the chunk is not protected.
        if (!ProtectionHandler.isChunkProtected(event.getClickedBlock().getChunk())) {
            return;
        }

        BeaconShieldBlock beacon = ProtectionHandler.getBeacon(event.getClickedBlock().getChunk());
        assert beacon != null;

        // If the player is allowed to interact with the territory.
        if (beacon.isAllowedPlayer(event.getPlayer())) {
            return;
        }

        // If beacon has no fuel
        if (!beacon.canProtect()) {
            return;
        }

        event.setCancelled(true);
        Chat.send(event.getPlayer(), "no-permission-to-interact");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteractWithEntity(PlayerInteractEntityEvent event) {
        Bukkit.getLogger().info("PlayerInteractEntityEvent");
        // If the entity is a player.
        if (event.getRightClicked() instanceof Player) {
            return;
        }

        Entity target = event.getRightClicked();

        // If the chunk is not protected.
        if (!ProtectionHandler.isChunkProtected(target.getLocation().getChunk())) {
            return;
        }

        BeaconShieldBlock beacon = ProtectionHandler.getBeacon(target.getLocation().getChunk());
        assert beacon != null;

        // If the player is allowed to interact with the territory.
        if (beacon.isAllowedPlayer(event.getPlayer())) {
            return;
        }

        // If beacon has no fuel
        if (!beacon.canProtect()) {
            return;
        }

        event.setCancelled(true);
        Chat.send(event.getPlayer(), "no-permission-to-interact");

    }
}
