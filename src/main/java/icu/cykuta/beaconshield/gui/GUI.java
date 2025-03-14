package icu.cykuta.beaconshield.gui;

import icu.cykuta.beaconshield.BeaconShield;
import icu.cykuta.beaconshield.beacon.BeaconShieldBlock;
import icu.cykuta.beaconshield.data.BeaconDataManager;
import icu.cykuta.beaconshield.gui.views.BeaconGUI;
import icu.cykuta.beaconshield.gui.views.ConfirmationGUI;
import icu.cykuta.beaconshield.utils.GUIHelper;
import icu.cykuta.beaconshield.config.PluginConfiguration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public abstract class GUI {
    protected Inventory inventory;
    private final int chestSize;
    private List<Integer> decorationSlots;
    private final Map<Integer, Consumer<GUIClick>> buttonActions = new HashMap<>();
    private final String inventoryName;
    private Storage storage;
    protected final PluginConfiguration guiConfig = BeaconShield.getPlugin().getFileHandler().getGui();

    public GUI(String inventoryName, int chestSize) {
        this.chestSize = chestSize;
        this.inventoryName = inventoryName;
    }

    public GUI(String inventoryName, int chestSize, Storage storage) {
        this(inventoryName, chestSize);
        this.storage = storage;
    }

    public enum SlotType {
        BUTTON,
        STORAGE,
        DECORATION,
        NONE
    }

    /**
     * Populate the inventory.
     */
    public abstract void populateInventory();

    /**
     * Decorate the inventory.
     */
    protected void decorateInventory() {
        for (int i = 0; i < this.chestSize; i++) {
            if (!this.decorationSlots.contains(i)) {
                continue;
            }

            ItemStack decorationItemStack = this.guiConfig.getItemStack("decoration");
            this.inventory.setItem(i, decorationItemStack);
        }
    }

    /**
     * Add a button to the inventory.
     * @param slot The slot to add the button.
     * @param path The path of the button.
     * @param consumer The action to run when the button is clicked.
     */
    protected void addInventoryButton(int slot, String path, Consumer<GUIClick> consumer) {
        ItemStack itemStack = this.guiConfig.getItemStack(path);
        addInventoryButton(slot, itemStack, consumer);
    }

    /**
     * Add a button to the inventory.
     * @param slot The slot to add the button.
     * @param itemStack The item stack of the button.
     * @param consumer The action to run when the button is clicked.
     */
    protected void addInventoryButton(int slot, ItemStack itemStack, Consumer<GUIClick> consumer) {
        this.inventory.setItem(slot, itemStack);
        this.buttonActions.put(slot, consumer);
    }

    /**
     * Add a non-functional button to the inventory.
     * @param slots The slots to add the button.
     */
    public void setDecorationSlots(int ...slots) {
        this.decorationSlots = Arrays.stream(slots).boxed().collect(Collectors.toList());
        this.decorateInventory();
    }

    /**
     * Add a decoration slot to the inventory.
     */
    public void addDecorationSlot(int slot) {
        this.decorationSlots.add(slot);
        this.decorateInventory();
    }
    /**
     * Get the action of a button slot.
     * @param slot The slot of the button.
     * @return The action of the button.
     */
    public Consumer<GUIClick> getButtonSlotAction(int slot) {
        return this.buttonActions.get(slot);
    }

    /**
     * Get the type of slot.
     * @param slot The slot to check.
     * @return The type of slot.
     */
    public SlotType getSlotType(int slot) {
        if (this.buttonActions.containsKey(slot)) {
            return SlotType.BUTTON;
        } else if (this.decorationSlots.contains(slot)) {
            return SlotType.DECORATION;
        } else if (this.storage != null && this.storage.getStorageSlots().contains(slot)) {
            return SlotType.STORAGE;
        }

        return SlotType.NONE;
    }

    /**
     * Open another GUI.
     * @param gui The GUI to open.
     */
    public void openGUI(Player player, GUI gui) {
        Inventory inventory;

        if (gui instanceof BeaconGUI) {
            BeaconDataManager beaconDataManager = BeaconShield.getPlugin().getBeaconDataManager();
            inventory = beaconDataManager.getInventory(this.getBeaconBlock());
        } else {
            inventory = GUIHelper.createInventory(gui, this.getBeaconBlock());
        }

        player.openInventory(inventory);
    }

    /**
     * Open a confirmation GUI.
     * @return
     */
    public void openConfirmationGUI(Player player, Consumer<GUIClick> consumer) {
        this.openGUI(player, new ConfirmationGUI(this, consumer));
    }

    @Nullable
    public Inventory getInventory() {
        return this.inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    private GUIHolder getHolder() {
        return (GUIHolder) this.inventory.getHolder();
    }

    public BeaconShieldBlock getBeaconBlock() {
        return this.getHolder().getBeaconBlock();
    }

    public String getInventoryName() {
        return this.inventoryName;
    }

    public int getChestSize() {
        return this.chestSize;
    }

    public Storage getStorage() {
        return this.storage;
    }
}
