package icu.cykuta.beaconshield.utils;

import icu.cykuta.beaconshield.beacon.BeaconShieldBlock;
import icu.cykuta.beaconshield.config.ConfigHandler;
import icu.cykuta.beaconshield.config.PluginConfiguration;
import icu.cykuta.beaconshield.data.ProtectionHandler;
import icu.cykuta.beaconshield.data.UpgradeHandler;
import icu.cykuta.beaconshield.gui.views.BeaconGUI;
import icu.cykuta.beaconshield.upgrade.Upgrade;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.Map;

public class UpgradeHelper {

    /**
     * Create an item stack prepared to be an {@Upgrade} item.
     * @param material Material of the item
     * @param namePath Path of the name in the lang file
     * @param lorePath Path of the lore in the lang file
     * @return The item stack
     */
    public static ItemStack itemMaker(Material material, String namePath, String lorePath) {
        PluginConfiguration lang = ConfigHandler.getInstance().getLang();
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;

        // Set display name and lore
        meta.setDisplayName(Text.color(lang.getString(namePath)));
        meta.setLore(Collections.singletonList(Text.color(lang.getString(lorePath))));

        meta.addItemFlags(ItemFlag.HIDE_PLACED_ON);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        meta.addItemFlags(ItemFlag.HIDE_DESTROYS);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.addItemFlags(ItemFlag.HIDE_DYE);

        item.setItemMeta(meta);
        return item;
    }

    /**
     * Verify if a chunk has an upgrade.
     * @param upgrade Upgrade to verify
     * @param chunk Chunk to verify
     * @return If chunk has upgrade
     */
    public static boolean chunkHasUpgrade(Upgrade upgrade, Chunk chunk) {
        if (!ProtectionHandler.isChunkProtected(chunk)) {
            return false; // If chunk is not protected, return
        }

        BeaconShieldBlock beacon = ProtectionHandler.getBeacon(chunk);
        assert beacon != null;

        if (!beacon.canProtect()) {
            return false; // If beacon not have fuel, return
        }

        for (Map.Entry<Integer, ItemStack> entry : beacon.getPdcManager().getStoredItems().entrySet()) {
            int slot = entry.getKey();
            ItemStack item = entry.getValue();

            if (!BeaconGUI.UPGRADE_SLOTS.contains(slot)) {
                continue; // If slot is not an upgrade slot, continue
            }

            if (!UpgradeHandler.getItemstack(upgrade).isSimilar(item)) {
                continue; // If item is not the upgrade, continue
            }

            return true;
        }

        return false;
    }
}
