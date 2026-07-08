package icu.cykuta.beaconshield.gui;

import icu.cykuta.beaconshield.beacon.BeaconShieldBlock;
import icu.cykuta.beaconshield.config.ConfigHandler;
import icu.cykuta.beaconshield.config.PluginConfiguration;
import icu.cykuta.beaconshield.data.BeaconHandler;
import icu.cykuta.beaconshield.gui.views.ConfirmationGUI;
import icu.cykuta.beaconshield.utils.Text;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Base class for every BeaconShield menu.
 *
 * <p>A GUI is always bound to a {@link BeaconShieldBlock}. The inventory is
 * created lazily the first time {@link #open(Player)} or {@link #getInventory()}
 * is called, and {@link #populate()} is responsible for filling it with
 * decorations, buttons and content.</p>
 */
public abstract class GUI {

    public enum SlotType {
        BUTTON,
        STORAGE,
        DECORATION,
        NONE
    }

    protected final BeaconShieldBlock beacon;
    protected final PluginConfiguration guiConfig = ConfigHandler.getInstance().getGui();
    protected Inventory inventory;

    private final String titleKey;
    private final int size;
    private final Map<Integer, Consumer<GUIClick>> buttons = new HashMap<>();
    private final Set<Integer> decorationSlots = new HashSet<>();
    private final Set<Integer> storageSlots = new HashSet<>();

    protected GUI(BeaconShieldBlock beacon, String titleKey, int size) {
        this.beacon = beacon;
        this.titleKey = titleKey;
        this.size = size;
    }

    /**
     * Fill the inventory with decorations, buttons and content.
     * Called every time the GUI is (re)built, see {@link #refresh()}.
     */
    protected abstract void populate();

    /**
     * Open this GUI for the given player, building the inventory if needed.
     */
    public void open(Player player) {
        player.openInventory(this.getInventory());
    }

    /**
     * Get the inventory of this GUI, building it on first use.
     */
    public Inventory getInventory() {
        if (this.inventory == null) {
            GUIHolder holder = new GUIHolder(this);
            String title = ConfigHandler.getInstance().getLang().getString(this.titleKey);
            this.inventory = Bukkit.createInventory(holder, this.size, Text.color(title));
            holder.setInventory(this.inventory);
            this.refresh();
        }
        return this.inventory;
    }

    /**
     * Check if the backing inventory has been created.
     */
    public boolean isBuilt() {
        return this.inventory != null;
    }

    /**
     * Clear the inventory and repopulate it from scratch.
     */
    public void refresh() {
        if (this.inventory == null) {
            return;
        }

        this.buttons.clear();
        this.decorationSlots.clear();
        this.storageSlots.clear();
        this.inventory.clear();
        this.populate();
    }

    /**
     * Add a clickable button whose item is read from gui.yml.
     */
    protected void addButton(int slot, String configPath, Consumer<GUIClick> action) {
        this.addButton(slot, this.guiConfig.getItemStack(configPath), action);
    }

    /**
     * Add a clickable button with a custom item.
     */
    protected void addButton(int slot, ItemStack item, Consumer<GUIClick> action) {
        this.inventory.setItem(slot, item);
        this.buttons.put(slot, action);
    }

    /**
     * Fill the given slots with the decoration item.
     */
    protected void addDecoration(int... slots) {
        ItemStack decoration = this.guiConfig.getItemStack("global.decoration");
        for (int slot : slots) {
            this.decorationSlots.add(slot);
            this.inventory.setItem(slot, decoration);
        }
    }

    /**
     * Mark a slot as free storage (players can put/take items in it).
     */
    protected void addStorageSlot(int slot) {
        this.storageSlots.add(slot);
    }

    /**
     * Persist the current content of every storage slot into the beacon
     * PDC. Does nothing if this GUI has no storage, is not built yet or
     * the beacon was destroyed.
     */
    public void persistStorage() {
        if (this.storageSlots.isEmpty() || this.inventory == null) {
            return;
        }
        if (!BeaconHandler.getInstance().isRegistered(this.beacon)) {
            return;
        }

        Map<Integer, ItemStack> storedItems = this.beacon.getPdcManager().getStoredItems();
        for (int slot : this.storageSlots) {
            ItemStack current = this.inventory.getItem(slot);

            if (current == null || current.getType().isAir()) {
                storedItems.remove(slot);
            } else {
                storedItems.put(slot, current);
            }
        }
        this.beacon.getPdcManager().setStoredItems(storedItems);

        this.onStorageChanged();
    }

    /**
     * Called after the storage content changed and was persisted.
     */
    protected void onStorageChanged() {
    }

    public SlotType getSlotType(int slot) {
        if (this.buttons.containsKey(slot)) {
            return SlotType.BUTTON;
        }
        if (this.storageSlots.contains(slot)) {
            return SlotType.STORAGE;
        }
        if (this.decorationSlots.contains(slot)) {
            return SlotType.DECORATION;
        }
        return SlotType.NONE;
    }

    public Consumer<GUIClick> getButtonAction(int slot) {
        return this.buttons.get(slot);
    }

    public BeaconShieldBlock getBeacon() {
        return this.beacon;
    }

    /**
     * Open the main (cached) beacon GUI of this beacon.
     */
    protected void openMainGUI(Player player) {
        BeaconHandler.getInstance().getBeaconGUI(this.beacon).open(player);
    }

    /**
     * Open a confirmation dialog. On deny, this GUI is reopened.
     */
    protected void openConfirmation(Player player, Consumer<GUIClick> onConfirm) {
        new ConfirmationGUI(this.beacon, this, onConfirm).open(player);
    }
}
