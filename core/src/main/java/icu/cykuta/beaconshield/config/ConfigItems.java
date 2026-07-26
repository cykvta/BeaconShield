package icu.cykuta.beaconshield.config;

import icu.cykuta.api.config.PluginConfiguration;
import icu.cykuta.api.util.Text;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Reads and writes menu items ({@code gui.yml}) as {@link ItemStack}s.
 *
 * <p>The item layout is BeaconShield specific, so it lives here instead of in
 * {@link PluginConfiguration}, which the API provides:</p>
 *
 * <pre>
 * some-button:
 *   item: minecraft:beacon
 *   name: "&amp;bMy button"
 *   lore: ["&amp;7First line"]
 *   custom-model-data: 0
 * </pre>
 *
 * <p>Like the API getters, reading a missing path writes the default back to the
 * configuration, so the file heals itself on the next save.</p>
 */
public class ConfigItems {

    private ConfigItems() {
    }

    /**
     * Read the item at the path, writing {@code def} back when the path is missing.
     *
     * @param config The configuration to read from.
     * @param path   Path of the item section.
     * @param def    The default item.
     * @return The item described by the configuration.
     */
    public static ItemStack get(@NotNull PluginConfiguration config, @NotNull String path, ItemStack def) {
        if (!config.isSet(path)) {
            set(config, path, def);
            return def;
        }

        String itemId = config.getString(path + ".item", def.getType().name());
        String itemName = config.getString(path + ".name",
                def.getItemMeta() != null ? def.getItemMeta().getDisplayName() : "");
        List<String> lore = config.getStringList(path + ".lore");
        int customModelData = config.getInt(path + ".custom-model-data", 0);

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
                itemMeta.setLore(Text.color(lore));
            }
            if (customModelData != 0) {
                itemMeta.setCustomModelData(customModelData);
            }

            item.setItemMeta(itemMeta);
        }

        return item;
    }

    /**
     * Read the item at the path, falling back to a placeholder that makes the
     * missing key obvious in-game.
     *
     * @param config The configuration to read from.
     * @param path   Path of the item section.
     * @return The item described by the configuration.
     */
    public static ItemStack get(@NotNull PluginConfiguration config, @NotNull String path) {
        return get(config, path, missing(config, path));
    }

    /**
     * Write an item to the path, using the keys {@link #get} reads back.
     *
     * @param config The configuration to write to.
     * @param path   Path of the item section.
     * @param item   The item to write.
     */
    public static void set(@NotNull PluginConfiguration config, @NotNull String path, ItemStack item) {
        config.set(path + ".item", item.getType().name());

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            config.set(path + ".name", meta.hasDisplayName() ? meta.getDisplayName() : "");
            config.set(path + ".lore", meta.hasLore() ? meta.getLore() : null);
            if (meta.hasCustomModelData()) {
                config.set(path + ".custom-model-data", meta.getCustomModelData());
            }
        }
    }

    /**
     * Placeholder item naming the configuration path that could not be found.
     */
    private static ItemStack missing(PluginConfiguration config, String path) {
        ItemStack item = new ItemStack(Material.STONE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("???");
        meta.setLore(List.of("No value found (" + config.getName() + ":" + path + ")"));
        item.setItemMeta(meta);
        return item;
    }
}
