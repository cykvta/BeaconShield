package icu.cykuta.beaconshield.utils;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;
import java.util.stream.Collectors;

public class HeadHelper {

    public static ItemStack getHead(OfflinePlayer player, String name, String... lore) {
        ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = getHeadMeta(player, itemStack, Text.color("&f" + name));

        // Try to set lore, if lore is null, catch the exception
        try { meta.setLore(Arrays.stream(lore).map(Text::color).collect(Collectors.toList()));
        } catch (NullPointerException ignored) { }

        itemStack.setItemMeta(meta);
        return itemStack;
    }

    public static ItemStack getHead(OfflinePlayer player) {
        return getHead(player, player.getName());
    }

    public static SkullMeta getHeadMeta(OfflinePlayer player, ItemStack itemStack, String name) {
        SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();

        assert skullMeta != null;
        skullMeta.setOwningPlayer(player);
        skullMeta.setDisplayName(name);
        return skullMeta;
    }
}
