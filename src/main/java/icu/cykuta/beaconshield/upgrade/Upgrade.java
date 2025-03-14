package icu.cykuta.beaconshield.upgrade;

import icu.cykuta.beaconshield.BeaconShield;
import icu.cykuta.beaconshield.beacon.BeaconShieldBlock;
import icu.cykuta.beaconshield.data.ProtectionHandler;
import icu.cykuta.beaconshield.data.UpgradeHandler;
import icu.cykuta.beaconshield.gui.views.BeaconGUI;
import icu.cykuta.beaconshield.utils.PluginConfiguration;
import icu.cykuta.beaconshield.utils.Text;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;

public abstract class Upgrade implements Listener {
    private final String name;

    public Upgrade(String name) {
        this.name = name;
        Bukkit.getPluginManager().registerEvents(this, BeaconShield.getPlugin());
    }

    public boolean chunkHasUpgrade(Chunk chunk) {
        if (!ProtectionHandler.isChunkProtected(chunk)) {
            return false; // If chunk is not protected, return
        }

        BeaconShieldBlock beacon = ProtectionHandler.getBeacon(chunk);
        assert beacon != null;

        if (!beacon.canProtect()) {
            return false; // If beacon not have fuel, return
        }

        for (Map.Entry<Integer, ItemStack> entry : beacon.getStoredItemsFromPDC().entrySet()) {
            int slot = entry.getKey();
            ItemStack item = entry.getValue();

            if (!BeaconGUI.UPGRADE_SLOTS.contains(slot)) {
                continue; // If slot is not an upgrade slot, continue
            }

            if (!UpgradeHandler.getItemstack(this).isSimilar(item)) {
                continue; // If item is not the upgrade, continue
            }

            return true;
        }

        return false;
    }

    @NotNull
    public abstract ItemStack getItemStack();

    public String getName() {
        return name;
    }

    public static ItemStack itemMaker(Material material, String namePath, String lorePath) {
        PluginConfiguration lang = BeaconShield.getPlugin().getFileHandler().getLang();
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
}
