package icu.cykuta.beaconshield.data;

import icu.cykuta.beaconshield.upgrade.Upgrade;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpgradeHandler {
    private static final Map<Upgrade, ItemStack> upgrades = new HashMap<>();

    /**
     * Put an upgrade into the handler.
     * @param upgrade The upgrade
     * @param itemStack The item stack
     */
    public static void put(Upgrade upgrade, ItemStack itemStack) {
        upgrades.put(upgrade, itemStack);
    }

    /**
     * Get the item stack of an upgrade.
     * @param upgrade The upgrade
     * @return The item stack
     */
    public static ItemStack getItemstack(Upgrade upgrade) {
        return upgrades.get(upgrade);
    }

    /**
     * Get all upgrades.
     * @return A list of all upgrades
     */
    public static List<Upgrade> getUpgrades() {
        return new ArrayList<>(upgrades.keySet());
    }

    /**
     * Rebuild the item of every registered upgrade from the config.
     * Used on reload so upgrade.yml changes take effect.
     */
    public static void refreshItems() {
        upgrades.replaceAll((upgrade, item) -> upgrade.getItemStack());
    }

    /**
     * Check if an item is the item of a registered upgrade.
     * @param item The item to check
     * @return true if the item is an upgrade item
     */
    public static boolean isUpgradeItem(ItemStack item) {
        for (ItemStack upgradeItem : upgrades.values()) {
            if (upgradeItem != null && upgradeItem.isSimilar(item)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get an upgrade by its name.
     * @param name The name of the upgrade
     * @return The upgrade
     */
    public static Upgrade getUpgrade(String name) {
        for(Upgrade upgrade : upgrades.keySet()) {
            if(upgrade.getName().equalsIgnoreCase(name)) {
                return upgrade;
            }
        }
        return null;
    }
}
