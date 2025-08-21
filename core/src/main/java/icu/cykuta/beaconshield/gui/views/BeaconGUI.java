package icu.cykuta.beaconshield.gui.views;

import icu.cykuta.beaconshield.beacon.BeaconShieldBlock;
import icu.cykuta.beaconshield.beacon.protection.PlayerRole;
import icu.cykuta.beaconshield.config.BeaconFile;
import icu.cykuta.beaconshield.data.BeaconHandler;
import icu.cykuta.beaconshield.data.ProtectionHandler;
import icu.cykuta.beaconshield.events.BeaconShieldDestroyedEvent;
import icu.cykuta.beaconshield.gui.GUI;
import icu.cykuta.beaconshield.gui.GUIClick;
import icu.cykuta.beaconshield.gui.GUIHolder;
import icu.cykuta.beaconshield.gui.Storage;
import icu.cykuta.beaconshield.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ConcurrentModificationException;
import java.util.List;

public class BeaconGUI extends GUI {
    public static final int FUEL_STORAGE_SLOT = 25;
    public static final List<Integer> INFO_SLOTS = List.of(16, 34);
    public static final List<Integer> UPGRADE_SLOTS = List.of(28, 29, 30, 31, 32);

    public BeaconGUI() {
        super("inventory-title-beacon-shield", 45, new Storage());
    }

    @Override
    public void populateInventory() {
        this.setDecorationSlots(
                0,  1,  2,  3,  4,  5,  6,  7,  8,
                9,              13,     15,     17,
                18, 19, 20, 21, 22, 23, 24,     26,
                27,                     33,     35,
                36, 37, 38, 39, 40, 41, 42, 43, 44
        );

        this.addInventoryButton(10, "beacon-gui.territory", (guiClick) -> openGUI(guiClick.clicker(), new TerritoryGUI()));
        this.addInventoryButton(11, "beacon-gui.members", (guiClick) -> openGUI(guiClick.clicker(), new MembersGUI()));
        this.addInventoryButton(12, "beacon-gui.group-management", (guiClick) -> openGUI(guiClick.clicker(), new GroupManagementGUI()));
        this.addInventoryButton(14, "beacon-gui.destroy", (guiClick) -> openConfirmationGUI(guiClick.clicker(), this::destroyBeaconShield));

        // Add storage and render information
        this.getStorage().addStorageSlot(FUEL_STORAGE_SLOT);
        for (int slot : UPGRADE_SLOTS) {
            this.getStorage().addStorageSlot(slot);
        }

        this.renderInfoSlot();
    }

    /**
     * Render the beacon information slot.
     */
    public void renderInfoSlot() {
        ItemStack item = this.getBeaconBlock().canProtect() ?
                this.guiConfig.getItemStack("beacon-gui.info-protected") :
                this.guiConfig.getItemStack("beacon-gui.info-unprotected");

        ItemMeta meta = item.getItemMeta();
        List<String> lore = Text.replace(meta.getLore(), this.getFuelExpireTime());
        meta.setLore(lore);
        item.setItemMeta(meta);

        for (int slot : INFO_SLOTS) {
            this.inventory.setItem(slot, item);
        }
    }

    /**
     * Obtains the remaining time of the fuel in readable format.
     * @return Remaining time.
     */
    private String getFuelExpireTime() {
        int currentFuel = this.getBeaconBlock().getFuelLevel();
        ItemStack fuel = inventory.getItem(FUEL_STORAGE_SLOT);

        if (fuel == null || fuel.getType().isAir()) {
            return Time.secondsToTime(currentFuel);
        }

        int queueFuel = this.getBeaconBlock().getBurnTime(fuel) * fuel.getAmount();
        return Time.secondsToTime(currentFuel + queueFuel);
    }

    /**
     * Destroy the Beacon Shield and handle resource collection.
     */
    public void destroyBeaconShield(GUIClick guiClick) {
        Player player = guiClick.clicker();
        GUIHolder holder = (GUIHolder) this.inventory.getHolder();
        if (holder == null) return;

        BeaconShieldBlock beacon = holder.getBeaconBlock();
        if (!this.getBeaconBlock().hasPermissionLevel(player, PlayerRole.OWNER)) {
            Chat.send(player, "no-permission-action");
            return;
        }

        // Remove the beacon shield block and drop the item
        beacon.getBlock().getWorld().dropItem(beacon.getBlock().getLocation(), BeaconShieldBlock.createBeaconItem());
        beacon.getBlock().setType(Material.AIR);

        // Drop the stored items and clear the storage
        for (ItemStack itemStack : this.getBeaconBlock().getPdcManager().getStoredItems().values()) {
            if (itemStack == null || itemStack.getType().isAir()) continue;
            beacon.getBlock().getWorld().dropItem(beacon.getBlock().getLocation(), itemStack);
        }

        // Remove all protected chunks from the protection handler
        ProtectionHandler.unregisterAllChunksForBeacon(beacon);

        // Remove the beacon from the data manager
        BeaconHandler beaconHandler = BeaconHandler.getInstance();
        beaconHandler.removeBeaconShieldBlock(beacon);

        // Delete the file
        BeaconFile.deleteBeaconFile(beacon);

        // Remove the inventory from the PDC
        beacon.getPdcManager().removeStoredItems();

        try { this.inventory.getViewers().forEach(HumanEntity::closeInventory);
        } catch (ConcurrentModificationException ignore) { }
        player.closeInventory();
        player.playSound(player.getLocation(), "block.beacon.deactivate", 1, 1);

        // Call api event
        Bukkit.getPluginManager().callEvent(new BeaconShieldDestroyedEvent(player, beacon));
    }
}
