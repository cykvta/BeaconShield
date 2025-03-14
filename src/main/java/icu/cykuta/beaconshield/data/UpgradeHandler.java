package icu.cykuta.beaconshield.data;

import icu.cykuta.beaconshield.BeaconShield;
import icu.cykuta.beaconshield.upgrade.Upgrade;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpgradeHandler {
    private static final Map<Upgrade, ItemStack> upgrades = new HashMap<>();

    public static void addUpgrade(Upgrade upgrade) {
        upgrades.put(upgrade, upgrade.getItemStack());
        Bukkit.getPluginManager().registerEvents(upgrade, BeaconShield.getPlugin());
    }

    public static ItemStack getItemstack(Upgrade upgrade) {
        return upgrades.get(upgrade);
    }

    public static List<Upgrade> getUpgrades() {
        return new ArrayList<>(upgrades.keySet());
    }

    public static Upgrade getUpgrade(String name) {
        for(Upgrade upgrade : upgrades.keySet()) {
            if(upgrade.getName().equalsIgnoreCase(name)) {
                return upgrade;
            }
        }
        return null;
    }
}
