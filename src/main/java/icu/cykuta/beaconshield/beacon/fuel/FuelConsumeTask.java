package icu.cykuta.beaconshield.beacon.fuel;

import com.jeff_media.customblockdata.CustomBlockData;
import icu.cykuta.beaconshield.BeaconShield;
import icu.cykuta.beaconshield.beacon.BeaconShieldBlock;
import icu.cykuta.beaconshield.config.ConfigHandler;
import icu.cykuta.beaconshield.data.BeaconHandler;
import icu.cykuta.beaconshield.gui.GUIHolder;
import icu.cykuta.beaconshield.gui.views.BeaconGUI;
import icu.cykuta.beaconshield.utils.FuelUtils;
import icu.cykuta.beaconshield.config.PluginConfiguration;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;

public class FuelConsumeTask extends BukkitRunnable {
    private final BeaconShield plugin = BeaconShield.getPlugin();
    private final PluginConfiguration config = ConfigHandler.getInstance().getConfig();
    private final BeaconHandler beaconHandler = BeaconHandler.getInstance();

    /**
     * This shits made with ChatGPT and fix the errors by myself.
     * I think is a good base, but I need to refactor this class.
     * Made at 5:00 AM after 24/h challenge. I'm so tired. I'm going to sleep. Bye.
     */

    @Override
    public void run() {
        for (Map.Entry<BeaconShieldBlock, Inventory> entry : beaconHandler.getBeaconShieldMap().entrySet()) {
            BeaconShieldBlock beacon = entry.getKey();
            Inventory inventory = entry.getValue();

            if (inventory == null) {
                handleNoInventory(beacon);
                continue;
            }

            if (beacon.getFuelLevel() > 0) {
                consumeFuel(beacon, inventory);
                continue;
            }

            refuelFromInventory(beacon, inventory);
        }
    }

    /**
     * Handles fuel consumption when the beacon has fuel.
     * @param beacon The beacon to handle
     * @param inventory The inventory of the beacon
     */
    private void consumeFuel(BeaconShieldBlock beacon, Inventory inventory) {
        beacon.consumeFuel();
        runEffect(beacon.getBlock());
        updateGUI(inventory);

        // If fuel is depleted after consumption, try to refuel
        if (beacon.getFuelLevel() <= 0) {
            refuelFromInventory(beacon, inventory);
        }
    }

    /**
     * Handles refueling the beacon from its inventory.
     * @param beacon The beacon to handle
     * @param inventory The inventory of the beacon
     */
    private void refuelFromInventory(BeaconShieldBlock beacon, Inventory inventory) {
        PersistentDataContainer pdc = new CustomBlockData(beacon.getBlock(), plugin);
        Map<Integer, ItemStack> storedItems = beacon.getStoredItemsFromPDC();
        ItemStack fuelItem = storedItems.get(BeaconGUI.FUEL_STORAGE_SLOT);

        if (fuelItem == null || fuelItem.getAmount() == 0 || FuelUtils.getBurnTime(fuelItem) <= 0) {
            beacon.setFuelLevel(-1); // No fuel available
            updateGUI(inventory); // Update GUI to reflect no fuel
            return;
        }

        int burnTime = FuelUtils.getBurnTime(fuelItem);
        if (burnTime > 0) {
            reduceFuel(beacon, pdc, fuelItem);
            updateFuelInGUI(inventory, fuelItem);
        }
    }

    /**
     * Reduces fuel from the beacon's PersistentDataContainer.
     * @param beacon The beacon to handle
     * @param pdc The PersistentDataContainer of the beacon
     * @param fuelItem The fuel item to reduce
     */
    private void reduceFuel(BeaconShieldBlock beacon, PersistentDataContainer pdc, ItemStack fuelItem) {
        int burnTime = FuelUtils.getBurnTime(fuelItem);
        beacon.setFuelLevel(burnTime);

        // Reduce the amount of fuel in the ItemStack
        fuelItem.setAmount(fuelItem.getAmount() - 1);

        // If the fuel item is depleted, remove it from the PDC
        if (fuelItem.getAmount() <= 0) {
            fuelItem = null; // Remove the fuel item
        }

        // Update the stored items in the PDC
        Map<Integer, ItemStack> storedItems = beacon.getStoredItemsFromPDC();
        storedItems.put(BeaconGUI.FUEL_STORAGE_SLOT, fuelItem);
        beacon.setStoredItemsToPDC(storedItems);
    }

    /**
     * Handles the case where the beacon has no inventory.
     * @param beacon The beacon to handle
     */
    private void handleNoInventory(BeaconShieldBlock beacon) {
        PersistentDataContainer pdc = new CustomBlockData(beacon.getBlock(), plugin);
        Map<Integer, ItemStack> storedItems = beacon.getStoredItemsFromPDC();
        ItemStack fuelItem = storedItems.get(BeaconGUI.FUEL_STORAGE_SLOT);

        if (fuelItem == null || fuelItem.getAmount() == 0) {
            beacon.setFuelLevel(-1); // No fuel available
        } else {
            reduceFuel(beacon, pdc, fuelItem);
            runEffect(beacon.getBlock());
        }
    }

    /**
     * Updates the fuel item in the GUI.
     * @param inventory The inventory of the beacon
     * @param fuelItem The fuel item to update
     */
    private void updateFuelInGUI(Inventory inventory, ItemStack fuelItem) {
        if (inventory.getHolder() instanceof GUIHolder holder && holder.getGUI() instanceof BeaconGUI gui) {
            gui.getInventory().setItem(BeaconGUI.FUEL_STORAGE_SLOT, fuelItem);
            gui.renderInfoSlot();
        }
    }

    /**
     * Updates the GUI of the beacon.
     * @param inventory The inventory of the beacon
     */
    private void updateGUI(Inventory inventory) {
        if (inventory.getHolder() instanceof GUIHolder holder && holder.getGUI() instanceof BeaconGUI gui) {
            gui.renderInfoSlot();
        }
    }

    /**
     * Generates a flame particle effect at the given block.
     * @param block The block to generate the effect at
     */
    private void runEffect(Block block) {
        if (!config.getBoolean("fuel-particles")) {
            return;
        }
        Location loc = block.getLocation();
        loc.getWorld().spawnParticle(
                Particle.FLAME,
                loc.getX() + 0.5, loc.getY() + 0.5, loc.getZ() + 0.5,
                5, 0.5, 0.5, 0.5, 0
        );
    }
}