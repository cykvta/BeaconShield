package icu.cykuta.beaconshield.utils;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PluginConfiguration extends YamlConfiguration {

    /**
     * Get the string from the path, if the path not exist, set the path to the default value.
     * @param path Path of the String to get.
     * @param def The default value to set the path to.
     * @return The string value.
     */
    @Override
    public String getString(@NotNull String path, String def) {
        if (!isSet(path)) {
            set(path, def);
            return def;
        }
        return super.getString(path, def);
    }

    /**
     * Get the string from the path, if the path not exist, set the path to the default value.
     * @param path Path of the String to get.
     * @return The string value.
     */
    @Override
    public String getString(@NotNull String path) {
        return getString(path, "No value found ("+ this.getName() + ":" + path + ")");
    }

    /**
     * Get the int from the path, if the path not exist, set the path to the default value.
     * @param path Path of the int to get.
     * @param def The default value to set the path to.
     * @return The int value.
     */
    @Override
    public int getInt(@NotNull String path, int def) {
        if (!isSet(path)) {
            set(path, def);
            return def;
        }
        return super.getInt(path, def);
    }

    /**
     * Get the boolean from the path, if the path not exist, set the path to the default value.
     * @param path Path of the boolean to get.
     * @param def The default value to set the path to.
     * @return The boolean value.
     */
    @Override
    public boolean getBoolean(@NotNull String path, boolean def) {
        if (!isSet(path)) {
            set(path, def);
            return def;
        }
        return super.getBoolean(path, def);
    }

    /**
     * Get the double from the path, if the path not exist, set the path to the default value.
     * @param path Path of the double to get.
     * @param def The default value to set the path to.
     * @return The double value.
     */
    @Override
    public double getDouble(@NotNull String path, double def) {
        if (!isSet(path)) {
            set(path, def);
            return def;
        }
        return super.getDouble(path, def);
    }

    /**
     * Get the {@link ItemStack} from the path, if the path not exist, set the path to the default value.
     * @param path Path of the ItemStack to get.
     * @param def The default value to set the path to.
     * @return The ItemStack value.
     */
    @Override
    public org.bukkit.inventory.ItemStack getItemStack(@NotNull String path, ItemStack def) {
        String itemId = getString(path + ".item-id", def.getType().name());
        String itemName = getString(path + ".item-name", def.getItemMeta() != null ? def.getItemMeta().getDisplayName() : "");
        int amount = getInt(path + ".amount", def.getAmount());
        List<String> lore = getStringList(path + ".lore");
        int customModelData = getInt(path + ".custom-model-data", 0);

        Material material = Material.matchMaterial(itemId);
        if (material == null) {
            material = Material.STONE;
        }

        ItemStack item = new ItemStack(material, amount);
        ItemMeta itemMeta = item.getItemMeta();

        if (itemMeta != null) {
            if (!itemName.isEmpty()) {
                itemMeta.setDisplayName(itemName);
            }
            if (!lore.isEmpty()) {
                itemMeta.setLore(lore);
            }
            if (customModelData != 0) {
                itemMeta.setCustomModelData(customModelData);
            }
            item.setItemMeta(itemMeta);
        }

        return item;
    }

}
