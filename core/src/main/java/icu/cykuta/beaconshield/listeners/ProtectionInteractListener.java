package icu.cykuta.beaconshield.listeners;

import icu.cykuta.beaconshield.beacon.BeaconShieldBlock;
import icu.cykuta.beaconshield.beacon.protection.RolePermission;
import icu.cykuta.beaconshield.data.ProtectionHandler;
import icu.cykuta.beaconshield.utils.Chat;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Objects;
import java.util.Set;

/**
 * Enforces the territory protection: blocks building, breaking,
 * interacting and grief mechanics inside protected chunks for
 * players without the required permission.
 */
public class ProtectionInteractListener implements Listener {
    private static final Set<Material> GENERATOR_SOURCES = Set.of(Material.LAVA, Material.WATER);

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) {
            return;
        }

        if (this.isActionDenied(event.getPlayer(), event.getClickedBlock().getChunk(), RolePermission.USE)) {
            event.setCancelled(true);
            Chat.send(event.getPlayer(), "no-permission-to-interact");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPlaceBlock(BlockPlaceEvent event) {
        if (this.isActionDenied(event.getPlayer(), event.getBlock().getChunk(), RolePermission.BUILD)) {
            event.setCancelled(true);
            Chat.send(event.getPlayer(), "no-permission-to-interact");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerBreakBlock(BlockBreakEvent event) {
        if (this.isActionDenied(event.getPlayer(), event.getBlock().getChunk(), RolePermission.BREAK)) {
            event.setCancelled(true);
            Chat.send(event.getPlayer(), "no-permission-to-interact");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteractWithEntity(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Player) {
            return;
        }

        Chunk chunk = event.getRightClicked().getLocation().getChunk();
        if (this.isActionDenied(event.getPlayer(), chunk, RolePermission.ENTITY)) {
            event.setCancelled(true);
            Chat.send(event.getPlayer(), "no-permission-to-interact");
        }
    }

    /**
     * Cancel piston extensions that push blocks from outside into a
     * protected territory.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        BeaconShieldBlock sourceProtection = ProtectionHandler.getBeacon(event.getBlock().getChunk());

        for (Block block : event.getBlocks()) {
            Block destination = block.getRelative(event.getDirection());
            BeaconShieldBlock targetProtection = ProtectionHandler.getBeacon(destination.getChunk());

            if (targetProtection == null || !targetProtection.canProtect()) {
                continue;
            }

            if (!Objects.equals(sourceProtection, targetProtection)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    /**
     * Cancel liquid flows (cobblestone generators) that cross into a
     * protected territory from outside.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLiquidFlow(BlockFromToEvent event) {
        if (!GENERATOR_SOURCES.contains(event.getBlock().getType())) {
            return;
        }

        Chunk fromChunk = event.getBlock().getChunk();
        Chunk toChunk = event.getToBlock().getChunk();
        if (fromChunk.equals(toChunk)) {
            return;
        }

        BeaconShieldBlock beacon = ProtectionHandler.getBeacon(toChunk);
        if (beacon != null && beacon.canProtect()) {
            event.setCancelled(true);
        }
    }

    /**
     * A player action in a chunk is denied when the chunk is protected by
     * a fueled beacon and the player lacks the required permission.
     */
    private boolean isActionDenied(Player player, Chunk chunk, RolePermission permission) {
        BeaconShieldBlock beacon = ProtectionHandler.getBeacon(chunk);
        return beacon != null
                && beacon.canProtect()
                && !beacon.isAllowedPlayer(permission, player);
    }
}
