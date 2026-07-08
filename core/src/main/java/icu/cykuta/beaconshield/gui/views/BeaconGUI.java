package icu.cykuta.beaconshield.gui.views;

import icu.cykuta.beaconshield.beacon.BeaconShieldBlock;
import icu.cykuta.beaconshield.beacon.protection.PlayerRole;
import icu.cykuta.beaconshield.config.BeaconFile;
import icu.cykuta.beaconshield.config.ConfigHandler;
import icu.cykuta.beaconshield.data.BeaconHandler;
import icu.cykuta.beaconshield.data.ProtectionHandler;
import icu.cykuta.beaconshield.data.UpgradeHandler;
import icu.cykuta.beaconshield.events.BeaconShieldDestroyedEvent;
import icu.cykuta.beaconshield.gui.GUI;
import icu.cykuta.beaconshield.gui.GUIClick;
import icu.cykuta.beaconshield.utils.Chat;
import icu.cykuta.beaconshield.utils.Text;
import icu.cykuta.beaconshield.utils.Time;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Main menu of a beacon. This GUI is cached per beacon (see
 * {@link BeaconHandler#getBeaconGUI}) so every viewer shares the same
 * inventory and the fuel/upgrade storage slots stay in sync.
 */
public class BeaconGUI extends GUI {
    public static final int FUEL_STORAGE_SLOT = 25;
    public static final List<Integer> INFO_SLOTS = List.of(16, 34);
    public static final List<Integer> UPGRADE_SLOTS = List.of(28, 29, 30, 31, 32);

    public BeaconGUI(BeaconShieldBlock beacon) {
        super(beacon, "inventory-title-beacon-shield", 45);
    }

    @Override
    protected void populate() {
        this.addDecoration(
                0,  1,  2,  3,  4,  5,  6,  7,  8,
                9,              13,     15,     17,
                18, 19, 20, 21, 22, 23, 24,     26,
                27,                     33,     35,
                36, 37, 38, 39, 40, 41, 42, 43, 44
        );

        this.addButton(10, "beacon-gui.territory", click -> new TerritoryGUI(this.beacon).open(click.clicker()));
        this.addButton(11, "beacon-gui.members", click -> new MembersGUI(this.beacon).open(click.clicker()));
        this.addButton(12, "beacon-gui.group-management", click -> new GroupManagementGUI(this.beacon).open(click.clicker()));
        this.addButton(14, "beacon-gui.destroy", click -> this.openConfirmation(click.clicker(), this::destroyBeaconShield));

        if (BeaconShieldBlock.isFuelSystemEnabled()) {
            this.addStorageSlot(FUEL_STORAGE_SLOT);
        } else {
            this.addDecoration(FUEL_STORAGE_SLOT);
        }
        UPGRADE_SLOTS.forEach(this::addStorageSlot);

        this.restoreStoredItems();
        this.renderInfoSlot();
    }

    /**
     * Put the items persisted in the beacon PDC back into the storage slots.
     */
    private void restoreStoredItems() {
        this.beacon.getPdcManager().getStoredItems().forEach((slot, item) -> {
            if (this.getSlotType(slot) == SlotType.STORAGE) {
                this.inventory.setItem(slot, item);
            }
        });
    }

    @Override
    protected void onStorageChanged() {
        this.renderInfoSlot();
    }

    /**
     * Try to store an item into the matching storage slots, furnace-style:
     * fuel goes to the fuel slot and upgrade items to the upgrade slots.
     * Similar stacks are merged first (respecting the max stack size) and
     * remaining items fill empty slots.
     *
     * @param item The item to store (not modified).
     * @return The items that did not fit, or null if everything was stored.
     */
    @Nullable
    public ItemStack storeItem(ItemStack item) {
        List<Integer> targetSlots = this.getTargetSlots(item);
        if (targetSlots == null) {
            return item;
        }

        ItemStack remaining = this.storeInSlots(item.clone(), targetSlots);

        if (remaining.getAmount() != item.getAmount()) {
            this.persistStorage();
        }

        return remaining.getAmount() <= 0 ? null : remaining;
    }

    /**
     * Get the storage slots where an item belongs, or null if the item
     * is neither fuel nor an upgrade.
     */
    @Nullable
    private List<Integer> getTargetSlots(ItemStack item) {
        if (BeaconShieldBlock.isFuelSystemEnabled() && this.beacon.getBurnTime(item) > 0) {
            return List.of(FUEL_STORAGE_SLOT);
        }
        if (UpgradeHandler.isUpgradeItem(item)) {
            return UPGRADE_SLOTS;
        }
        return null;
    }

    /**
     * Distribute an item stack over the given slots and return what
     * did not fit (possibly with amount 0).
     */
    private ItemStack storeInSlots(ItemStack item, List<Integer> slots) {
        Inventory inventory = this.getInventory();

        // First merge into similar stacks
        for (int slot : slots) {
            if (item.getAmount() <= 0) {
                return item;
            }

            ItemStack existing = inventory.getItem(slot);
            if (existing == null || existing.getType().isAir() || !existing.isSimilar(item)) {
                continue;
            }

            int space = existing.getMaxStackSize() - existing.getAmount();
            if (space <= 0) {
                continue;
            }

            int moved = Math.min(space, item.getAmount());
            existing.setAmount(existing.getAmount() + moved);
            inventory.setItem(slot, existing);
            item.setAmount(item.getAmount() - moved);
        }

        // Then fill empty slots
        for (int slot : slots) {
            if (item.getAmount() <= 0) {
                return item;
            }

            ItemStack existing = inventory.getItem(slot);
            if (existing != null && !existing.getType().isAir()) {
                continue;
            }

            inventory.setItem(slot, item.clone());
            item.setAmount(0);
        }

        return item;
    }

    /**
     * Render the beacon information item (protection state and remaining fuel).
     */
    public void renderInfoSlot() {
        if (!this.isBuilt()) {
            return;
        }

        ItemStack item = this.beacon.canProtect() ?
                this.guiConfig.getItemStack("beacon-gui.info-protected") :
                this.guiConfig.getItemStack("beacon-gui.info-unprotected");

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setLore(Text.replace(meta.getLore(), this.getFuelExpireTime()));
            item.setItemMeta(meta);
        }

        for (int slot : INFO_SLOTS) {
            this.inventory.setItem(slot, item);
        }
    }

    /**
     * Get the remaining protection time (current fuel plus queued fuel)
     * in a readable format. When the fuel system is disabled the
     * protection is permanent.
     */
    private String getFuelExpireTime() {
        if (!BeaconShieldBlock.isFuelSystemEnabled()) {
            return Text.color(ConfigHandler.getInstance().getLang().getString("fuel-infinite"));
        }

        int currentFuel = this.beacon.getFuelLevel();
        ItemStack fuel = this.inventory.getItem(FUEL_STORAGE_SLOT);

        if (fuel == null || fuel.getType().isAir()) {
            return Time.secondsToTime(currentFuel);
        }

        int queuedFuel = this.beacon.getBurnTime(fuel) * fuel.getAmount();
        return Time.secondsToTime(currentFuel + queuedFuel);
    }

    /**
     * Destroy the beacon shield: drop the beacon item and the stored items,
     * unregister the protection and delete the persisted data.
     */
    private void destroyBeaconShield(GUIClick click) {
        Player player = click.clicker();

        if (!this.beacon.hasPermissionLevel(player, PlayerRole.OWNER)) {
            Chat.send(player, "no-permission-action");
            return;
        }

        World world = this.beacon.getWorld();
        Location dropLocation = this.beacon.getBlock().getLocation();

        // Drop the beacon item and the stored items
        world.dropItem(dropLocation, BeaconShieldBlock.createBeaconItem());
        for (ItemStack stored : this.beacon.getPdcManager().getStoredItems().values()) {
            if (stored != null && !stored.getType().isAir()) {
                world.dropItem(dropLocation, stored);
            }
        }

        // Remove the persisted block data before removing the block itself
        this.beacon.getPdcManager().clear();
        this.beacon.getBlock().setType(Material.AIR);

        // Unregister the beacon and delete its data file
        ProtectionHandler.unregisterAllChunksForBeacon(this.beacon);
        BeaconHandler.getInstance().removeBeaconShieldBlock(this.beacon);
        BeaconFile.deleteBeaconFile(this.beacon);

        // Close the GUI for everyone (copy the list to avoid concurrent modification)
        new ArrayList<>(this.inventory.getViewers()).forEach(HumanEntity::closeInventory);
        player.closeInventory();
        player.playSound(player.getLocation(), "block.beacon.deactivate", 1, 1);

        Bukkit.getPluginManager().callEvent(new BeaconShieldDestroyedEvent(player, this.beacon));
    }
}
