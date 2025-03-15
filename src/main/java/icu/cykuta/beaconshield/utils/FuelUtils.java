package icu.cykuta.beaconshield.utils;

import icu.cykuta.beaconshield.BeaconShield;
import icu.cykuta.beaconshield.config.ConfigHandler;
import icu.cykuta.beaconshield.config.PluginConfiguration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public class FuelUtils {
    /**
     * Get the burning time of the fuel.
     * @param fuel The fuel to get the burning time
     * @return The burning time of the fuel
     */
    public static int getBurnTime(ItemStack fuel) {
        PluginConfiguration config = ConfigHandler.getInstance().getConfig();
        List<Map<?, ?>> fuelList = config.getMapList("fuel-items");

        for (Map<?, ?> fuelEntry : fuelList) {
            String itemName = (String) fuelEntry.get("item");
            int burnTime = (int) fuelEntry.get("burn-time");
            int customModelData = (int) fuelEntry.get("custom-model-data"); // 0 if not present

            // Add the namespace if it doesn't have one
            if (!itemName.startsWith("minecraft:")) {
                itemName = "minecraft:" + itemName;
            }

            // Compare the custom model data
            if (customModelData != 0) { // 0 if not present
                // fuel not have custom model data, continue
                // fuel have custom model data, but not equal to the custom model data in the config, continue
                if (fuel.getItemMeta() == null || fuel.getItemMeta().getCustomModelData() != customModelData) {
                    continue;
                }
            }

            // Get the material
            Material material = Material.matchMaterial(itemName);
            if (material == null) {
                continue;
            }

            // Check if the fuel is the same as the material
            if (fuel.getType() == material) {
                return burnTime;
            }
        }

        return 0;
    }
}
