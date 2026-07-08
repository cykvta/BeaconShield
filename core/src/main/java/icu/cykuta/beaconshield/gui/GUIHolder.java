package icu.cykuta.beaconshield.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

/**
 * InventoryHolder used to identify BeaconShield GUIs and get back
 * to the {@link GUI} instance from a Bukkit inventory event.
 */
public class GUIHolder implements InventoryHolder {
    private final GUI gui;
    private Inventory inventory;

    GUIHolder(GUI gui) {
        this.gui = gui;
    }

    void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public GUI getGUI() {
        return this.gui;
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return this.inventory;
    }
}
