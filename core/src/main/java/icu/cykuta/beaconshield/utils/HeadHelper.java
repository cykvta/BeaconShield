package icu.cykuta.beaconshield.utils;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;
import java.util.stream.Collectors;

public class HeadHelper {

    /**
     * Create a player head item with a custom name and optional lore.
     */
    public static ItemStack getHead(OfflinePlayer player, String name, String... lore) {
        ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) itemStack.getItemMeta();

        if (meta != null) {
            meta.setOwningPlayer(player);
            meta.setDisplayName(Text.color("&f" + name));

            if (lore != null && lore.length > 0) {
                meta.setLore(Arrays.stream(lore)
                        .filter(line -> line != null)
                        .map(Text::color)
                        .collect(Collectors.toList()));
            }

            itemStack.setItemMeta(meta);
        }

        return itemStack;
    }

    /**
     * Create a player head item named after the player.
     */
    public static ItemStack getHead(OfflinePlayer player) {
        return getHead(player, player.getName());
    }
}
