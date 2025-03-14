package icu.cykuta.beaconshield.utils;

import icu.cykuta.beaconshield.BeaconShield;
import icu.cykuta.beaconshield.beacon.BeaconShieldBlock;
import icu.cykuta.beaconshield.gui.GUI;
import icu.cykuta.beaconshield.gui.GUIHolder;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

public class GUIHelper {

    /**
     * Create a GUI inventory for the player.
     * @param beacon The beacon shield block.
     * @return The inventory.
     */
    public static Inventory createInventory(GUI gui, BeaconShieldBlock beacon) {
        GUIHolder holder = new GUIHolder(beacon, gui);

        PluginConfiguration lang = BeaconShield.getPlugin().getFileHandler().getLang();
        String inventoryName = lang.getString(gui.getInventoryName());

        if (inventoryName == null) {
            inventoryName = gui.getInventoryName() + " (Missing)";
        }

        Inventory inv = Bukkit.createInventory(holder, gui.getChestSize(), Text.color(inventoryName));

        gui.setInventory(inv);
        gui.populateInventory();

        if (gui.getStorage() != null) {
            gui.getStorage().populateStorage(beacon.getStoredItemsFromPDC() ,inv);
        }

        Inventory inventory = gui.getInventory();
        assert inventory != null;
        return inventory;
    }
}
