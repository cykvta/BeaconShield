package icu.cykuta.beaconshield.utils;

import icu.cykuta.beaconshield.BeaconShield;
import icu.cykuta.beaconshield.beacon.BeaconShieldBlock;
import icu.cykuta.beaconshield.config.ConfigHandler;
import icu.cykuta.beaconshield.config.PluginConfiguration;
import icu.cykuta.beaconshield.data.ProtectionHandler;
import icu.cykuta.beaconshield.data.UpgradeHandler;
import icu.cykuta.beaconshield.gui.views.BeaconGUI;
import icu.cykuta.beaconshield.upgrade.Upgrade;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;

public class UpgradeHelper {

    /**
     * Create an item stack prepared to be an {@Upgrade} item.
     * @param upgrade Upgrade to get the name and lore paths
     * @return The item stack
     */
    public static ItemStack itemMaker(Upgrade upgrade) {
        PluginConfiguration upgradeCfg = ConfigHandler.getInstance().getUpgrade();
        Material material = upgradeCfg.getMaterial(upgrade.getName() + ".item");
        String name = Text.color(upgradeCfg.getString(upgrade.getName() + ".name"));
        List<String> lore = Text.color(upgradeCfg.getStringList(upgrade.getName() + ".description"));
        int customModelData = upgradeCfg.getInt(upgrade.getName() + ".custom_model_data");

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;

        // Set display name and lore
        meta.setDisplayName(name);
        meta.setLore(lore);

        // Set custom model data
        if (customModelData != 0) {
            meta.setCustomModelData(customModelData);
        }

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

    /**
     * Create a recipe for an upgrade.
     */
    public static ShapedRecipe createRecipe(Upgrade upgrade) {
        PluginConfiguration upgradeCfg = ConfigHandler.getInstance().getUpgrade();

        if (!upgradeCfg.getBoolean(upgrade.getName() + ".recipe.enabled", false)) {
            return null;
        }

        NamespacedKey key = new NamespacedKey(BeaconShield.getPlugin(), upgrade.getName());
        ShapedRecipe recipe = new ShapedRecipe(key, upgrade.getItemStack());

        List<String> shape = upgradeCfg.getStringList(upgrade.getName() + ".recipe.shape");
        if (shape.isEmpty()) {
            throw new IllegalArgumentException("Recipe shape for " + upgrade.getName() + " is empty!");
        }

        ConfigurationSection ingredientsSection = upgradeCfg.getConfigurationSection(upgrade.getName() + ".recipe.ingredients");
        if (ingredientsSection == null) {
            throw new IllegalArgumentException("Recipe ingredients for " + upgrade.getName() + " are missing or not properly formatted!");
        }

        Map<String, Object> ingredients = ingredientsSection.getValues(false);
        if (ingredients.isEmpty()) {
            throw new IllegalArgumentException("Recipe ingredients for " + upgrade.getName() + " are empty!");
        }

        recipe.shape(shape.toArray(new String[0]));

        for (Map.Entry<String, Object> entry : ingredients.entrySet()) {
            String keyChar = entry.getKey();
            String materialString = entry.getValue().toString();
            Material material = Material.matchMaterial(materialString);
            if (material == null) {
                throw new IllegalArgumentException("Invalid material " + materialString + " for recipe ingredient " + keyChar + " in " + upgrade.getName() + "!");
            }
            recipe.setIngredient(keyChar.charAt(0), material);
        }

        return recipe;
    }
}
