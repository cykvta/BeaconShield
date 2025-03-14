package icu.cykuta.beaconshield.config;

import icu.cykuta.beaconshield.utils.Text;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
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
        if (!isSet(path)) {
            setItemStack(path, def);
            return def;
        }

        String itemId = getString(path + ".item", def.getType().name());
        String itemName = getString(path + ".name", def.getItemMeta() != null ? def.getItemMeta().getDisplayName() : "");
        List<String> lore = getStringList(path + ".lore");
        int customModelData = getInt(path + ".custom-model-data", 0);

        Material material = Material.matchMaterial(itemId);
        if (material == null) {
            material = Material.STONE;
        }

        ItemStack item = new ItemStack(material);
        ItemMeta itemMeta = item.getItemMeta();

        if (itemMeta != null) {
            if (!itemName.isEmpty()) {
                itemMeta.setDisplayName(Text.color(itemName));
            }
            if (!lore.isEmpty()) {
                itemMeta.setLore(lore.stream().map(Text::color).toList());
            }
            if (customModelData != 0) {
                itemMeta.setCustomModelData(customModelData);
            }
            item.setItemMeta(itemMeta);
        }

        return item;
    }

    /**
     * Set the {@link ItemStack} to the path.
     * @param path Path of the ItemStack to set.
     * @param item The ItemStack to set.
     */
    public void setItemStack(String path, ItemStack item) {
        set(path + ".item-id", item.getType().name());

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            set(path + ".item-name", meta.hasDisplayName() ? meta.getDisplayName() : "");
            set(path + ".lore", meta.hasLore() ? meta.getLore() : null);
            if (meta.hasCustomModelData()) {
                set(path + ".custom-model-data", meta.getCustomModelData());
            }
        }
        set(path + ".amount", item.getAmount());
    }


    /**
     * Adapt the {@link YamlConfiguration} to {@link PluginConfiguration}.
     * @param yamlConfig The YamlConfiguration to adapt.
     * @return The adapted CustomConfig.
     */
    public static PluginConfiguration adapt(YamlConfiguration yamlConfig) {
        PluginConfiguration customConfig = new PluginConfiguration();
        try {
            String serialized = yamlConfig.saveToString();
            customConfig.loadFromString(serialized);
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }
        return customConfig;
    }

}
