package icu.cykuta.beaconshield.listeners.bukkit;

import com.jeff_media.customblockdata.CustomBlockData;
import icu.cykuta.beaconshield.BeaconShield;
import icu.cykuta.beaconshield.beacon.BeaconShieldBlock;
import icu.cykuta.beaconshield.events.internal.*;
import icu.cykuta.beaconshield.gui.GUIHolder;
import icu.cykuta.beaconshield.data.ProtectionHandler;
import icu.cykuta.beaconshield.data.BeaconHandler;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import static icu.cykuta.beaconshield.data.DataKeys.IS_BEACONSHIELD;

public class BukkitEventListener implements Listener {
    /**
     * This event is called when a player places a block.
     */
    @EventHandler
    public void onPlayerPlaceBlock(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();

        // Return if the item is null
        if (item.getItemMeta() == null) {
            return;
        }

        // Get the custom data of the item
        PersistentDataContainer dataContainer = item.getItemMeta().getPersistentDataContainer();
        boolean isBeaconShield = Boolean.TRUE.equals(dataContainer.get(IS_BEACONSHIELD, PersistentDataType.BOOLEAN));

        // Check if the item is a beacon
        if (isBeaconShield) {
            // Add custom data to the block
            CustomBlockData customBlockData = new CustomBlockData(event.getBlock(), BeaconShield.getPlugin());
            customBlockData.set(IS_BEACONSHIELD, PersistentDataType.BOOLEAN, true);

            // Call the internal event
            Bukkit.getPluginManager().callEvent(new InternalBeaconShieldPlaceEvent(event));
        }
    }

    /**
     * This event is called when a player breaks a block.
     */
    @EventHandler
    public void onPlayerBreakBlock(BlockBreakEvent event) {
        Block block = event.getBlock();

        PersistentDataContainer dataContainer = new CustomBlockData(block, BeaconShield.getPlugin());
        BeaconHandler dataManager = BeaconHandler.getInstance();

        // Check if the block is in the memory
        if (!(dataManager.isBeaconShieldBlockInMemory(block))) {
            dataContainer.set(IS_BEACONSHIELD, PersistentDataType.BOOLEAN, false);
            return;
        }

        boolean isBeaconShield = Boolean.TRUE.equals(dataContainer.get(IS_BEACONSHIELD, PersistentDataType.BOOLEAN));

        // Check if the item is a beacon
        if (isBeaconShield) {
            Bukkit.getPluginManager().callEvent(new InternalBeaconShieldBreakEvent(event));
        }
    }

    /**
     * This event is called when a player interacts with a block.
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null) {
            return;
        }

        Block block = event.getClickedBlock();

        PersistentDataContainer dataContainer = new CustomBlockData(block, BeaconShield.getPlugin());
        BeaconHandler dataManager = BeaconHandler.getInstance();

        // Check if the block is in the memory
        if (!(dataManager.isBeaconShieldBlockInMemory(block))) {
            dataContainer.set(IS_BEACONSHIELD, PersistentDataType.BOOLEAN, false);
            return;
        }

        boolean isBeaconShield = Boolean.TRUE.equals(dataContainer.get(IS_BEACONSHIELD, PersistentDataType.BOOLEAN));

        // Check if the item is a beacon
        if (isBeaconShield) {
            // disable the default behavior of the event
            event.setCancelled(true);

            Bukkit.getPluginManager().callEvent(new InternalBeaconShieldInteractEvent(event));
        }
    }

    /**
     * This event is called when a player interacts with a inventory.
     */
    @EventHandler
    public void onPlayerInteractInventory(InventoryClickEvent event) {
        InventoryView view = event.getView();
        Inventory clickedInventory = event.getClickedInventory();

        // Verify if the inventory is a GUI
        if (!(view.getTopInventory().getHolder() instanceof GUIHolder)) {
            return;
        }

        if (clickedInventory == null) {
            return;
        }

        if (event.getClick().isShiftClick()) {
            event.setCancelled(true);
            return;
        }

        if (clickedInventory.getType() == InventoryType.PLAYER) {
            return;
        }

        // Call the event
        Bukkit.getPluginManager().callEvent(new InternalGUIInteractEvent(
                (Player) event.getWhoClicked(),
                event.getAction(),
                (GUIHolder) view.getTopInventory().getHolder(),
                event.getClick(),
                event.getCursor(),
                event.getCurrentItem(),
                event.getSlot(),
                () -> event.setCancelled(true)
        ));
    }

    @EventHandler
    public void onPlayerDragInventory(InventoryDragEvent event) {
        InventoryView view = event.getView();

        // Verify if the inventory is a GUI
        if (view.getTopInventory().getHolder() instanceof GUIHolder) {
            event.setCancelled(true);
        }
    }

    /**
     * This event is called when a player change chunk.
     */
    @EventHandler
    public void onPlayerMoveChunk(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();

        if (to == null) {
            return;
        }

        // Check if the player change chunk
        if (from.getChunk() == to.getChunk()) {
            return;
        }

        Chunk fromChunk = from.getChunk();
        Chunk toChunk = to.getChunk();

        // Check if player enter a protected chunk
        BeaconShieldBlock fromBeacon = ProtectionHandler.getBeacon(fromChunk);
        BeaconShieldBlock toBeacon = ProtectionHandler.getBeacon(toChunk);

        // Check if the player stay in the same protected chunk
        if (fromBeacon == null && toBeacon == null) {
            return;
        }

        if (fromBeacon == toBeacon) {
            return;
        }

        if (fromBeacon == null) {
            // Call the event when the player enter a protected chunk
            Bukkit.getPluginManager().callEvent(
                    new InternalPlayerProtectedChunkGatewayEvent(toBeacon, event.getPlayer(),
                            InternalPlayerProtectedChunkGatewayEvent.Action.ENTER));

        } else if (toBeacon == null) {
            // Call the event when the player leave a protected chunk
            Bukkit.getPluginManager().callEvent(new InternalPlayerProtectedChunkGatewayEvent(fromBeacon, event.getPlayer(),
                    InternalPlayerProtectedChunkGatewayEvent.Action.LEAVE));
        } else {
            // Call 2 events when the player move between 2 protected chunks
            Bukkit.getPluginManager().callEvent(new InternalPlayerProtectedChunkGatewayEvent(fromBeacon, event.getPlayer(),
                            InternalPlayerProtectedChunkGatewayEvent.Action.LEAVE));

            Bukkit.getPluginManager().callEvent(new InternalPlayerProtectedChunkGatewayEvent(toBeacon, event.getPlayer(),
                        InternalPlayerProtectedChunkGatewayEvent.Action.ENTER));
        }
    }
}
