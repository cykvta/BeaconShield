package icu.cykuta.beaconshield.listeners;

import com.jeff_media.customblockdata.CustomBlockData;
import icu.cykuta.beaconshield.BeaconShield;
import icu.cykuta.beaconshield.beacon.BeaconShieldBlock;
import icu.cykuta.beaconshield.beacon.protection.RolePermission;
import icu.cykuta.beaconshield.config.ConfigHandler;
import icu.cykuta.beaconshield.data.BeaconHandler;
import icu.cykuta.beaconshield.data.ProtectionHandler;
import icu.cykuta.beaconshield.events.BeaconShieldPlacedEvent;
import icu.cykuta.beaconshield.utils.Chat;
import icu.cykuta.beaconshield.utils.PermissionUtils;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import static icu.cykuta.beaconshield.data.DataKeys.IS_BEACONSHIELD;

/**
 * Handles the lifecycle of the beacon shield block itself:
 * placing a shield item, protecting it against breaking and
 * opening the menu on right click.
 */
public class BeaconBlockListener implements Listener {

    /**
     * Register a new beacon shield when a shield item is placed.
     */
    @EventHandler
    public void onBeaconShieldPlace(BlockPlaceEvent event) {
        if (!isBeaconShieldItem(event.getItemInHand())) {
            return;
        }

        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (ProtectionHandler.isChunkProtected(block.getChunk())) {
            Chat.send(player, "cant-place-shield");
            event.setCancelled(true);
            return;
        }

        BeaconHandler beaconHandler = BeaconHandler.getInstance();
        int maxBeacons = PermissionUtils.getLimit(player, "beaconshield.max-beacons.",
                ConfigHandler.getInstance().getConfig().getInt("max-beacons-per-player"));
        if (beaconHandler.getBeaconShieldBlocksByOwner(player).size() >= maxBeacons) {
            Chat.send(player, "max-beacons-reached");
            event.setCancelled(true);
            return;
        }

        // Mark the block and register the new beacon with its core chunk
        new CustomBlockData(block, BeaconShield.getPlugin()).set(IS_BEACONSHIELD, PersistentDataType.BOOLEAN, true);

        BeaconShieldBlock beacon = new BeaconShieldBlock(block, player);
        beaconHandler.addBeaconShieldBlock(beacon);
        beacon.addProtectedChunk(beacon.getCoreChunk());

        player.playSound(player.getLocation(), "block.beacon.activate", 1, 1);
        Bukkit.getPluginManager().callEvent(new BeaconShieldPlacedEvent(player, beacon));
    }

    /**
     * Beacon shields cannot be mined; they must be destroyed from the menu.
     */
    @EventHandler
    public void onBeaconShieldBreak(BlockBreakEvent event) {
        if (BeaconHandler.getInstance().getBeaconShieldBlock(event.getBlock()) != null) {
            event.setCancelled(true);
        }
    }

    /**
     * Open the beacon menu when a registered beacon shield is right-clicked.
     */
    @EventHandler
    public void onBeaconShieldInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null) {
            return;
        }

        BeaconShieldBlock beacon = BeaconHandler.getInstance().getBeaconShieldBlock(event.getClickedBlock());
        if (beacon == null) {
            return;
        }

        event.setCancelled(true);

        // The event fires once per hand; only act on the main hand
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Player player = event.getPlayer();
        if (!beacon.isAllowedPlayer(RolePermission.BEACON_USE, player)) {
            Chat.send(player, "no-permission-to-interact");
            return;
        }

        BeaconHandler.getInstance().getBeaconGUI(beacon).open(player);
    }

    /**
     * Check if an item is a beacon shield item.
     */
    private static boolean isBeaconShieldItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }

        return Boolean.TRUE.equals(
                meta.getPersistentDataContainer().get(IS_BEACONSHIELD, PersistentDataType.BOOLEAN));
    }
}
