package icu.cykuta.beaconshield.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Storage {
    private final ArrayList<Integer> storageSlots = new ArrayList<>();

    /**
     * Populate the storage.
     */
    public void populateStorage(Map<Integer, ItemStack> storedItems, Inventory inventory) {
        for (Map.Entry<Integer, ItemStack> entry : storedItems.entrySet()) {
            if (!this.storageSlots.contains(entry.getKey())) {
                continue;
            }

            inventory.setItem(entry.getKey(), entry.getValue());
        }
    }

    public void addStorageSlot(int slot) {
        this.storageSlots.add(slot);
    }

    public List<Integer> getStorageSlots() {
        return this.storageSlots;
    }
}
