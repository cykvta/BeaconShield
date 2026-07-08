package icu.cykuta.beaconshield.listeners;

import icu.cykuta.beaconshield.beacon.BeaconShieldBlock;
import icu.cykuta.beaconshield.beacon.protection.RolePermission;
import icu.cykuta.beaconshield.config.ConfigHandler;
import icu.cykuta.beaconshield.data.ProtectionHandler;
import icu.cykuta.beaconshield.utils.Chat;
import org.bukkit.Chunk;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.Set;

/**
 * Protects the territory from non-player grief: explosions, fire
 * spreading/burning and block-changing mobs. Every check can be
 * toggled in the config.
 */
public class ProtectionGriefListener implements Listener {
    private static final Set<EntityType> GRIEFER_MOBS = Set.of(
            EntityType.ENDERMAN, EntityType.RAVAGER, EntityType.SILVERFISH);

    /**
     * Explosions caused by entities (creepers, TNT, withers, ...):
     * blocks inside a protected territory are not destroyed, the rest
     * of the explosion is untouched.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (isEnabled("protection-explosions")) {
            event.blockList().removeIf(block -> isChunkShielded(block.getChunk()));
        }
    }

    /**
     * Explosions caused by blocks (beds, respawn anchors, ...).
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockExplode(BlockExplodeEvent event) {
        if (isEnabled("protection-explosions")) {
            event.blockList().removeIf(block -> isChunkShielded(block.getChunk()));
        }
    }

    /**
     * Fire ignition inside the territory. Members with build permission
     * can still use flint and steel; natural causes (spread, lava,
     * lightning, fireballs) are always blocked.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockIgnite(BlockIgniteEvent event) {
        if (!isEnabled("protection-fire")) {
            return;
        }

        BeaconShieldBlock beacon = ProtectionHandler.getBeacon(event.getBlock().getChunk());
        if (beacon == null || !beacon.canProtect()) {
            return;
        }

        Player player = event.getPlayer();
        if (player != null) {
            if (!beacon.isAllowedPlayer(RolePermission.BUILD, player)) {
                event.setCancelled(true);
                Chat.send(player, "no-permission-to-interact");
            }
            return;
        }

        event.setCancelled(true);
    }

    /**
     * Blocks inside the territory never burn away.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBurn(BlockBurnEvent event) {
        if (isEnabled("protection-fire") && isChunkShielded(event.getBlock().getChunk())) {
            event.setCancelled(true);
        }
    }

    /**
     * Mobs that grief blocks (endermen stealing blocks, ravagers,
     * silverfish) cannot modify blocks inside the territory.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (!isEnabled("protection-mob-grief") || !GRIEFER_MOBS.contains(event.getEntityType())) {
            return;
        }

        if (isChunkShielded(event.getBlock().getChunk())) {
            event.setCancelled(true);
        }
    }

    /**
     * Check if a chunk is protected by a fueled beacon.
     */
    private static boolean isChunkShielded(Chunk chunk) {
        BeaconShieldBlock beacon = ProtectionHandler.getBeacon(chunk);
        return beacon != null && beacon.canProtect();
    }

    private static boolean isEnabled(String configKey) {
        return ConfigHandler.getInstance().getConfig().getBoolean(configKey, true);
    }
}
