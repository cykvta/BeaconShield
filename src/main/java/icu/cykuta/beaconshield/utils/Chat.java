package icu.cykuta.beaconshield.utils;

import icu.cykuta.beaconshield.BeaconShield;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class Chat {
    public static String format(String message) {
        return Text.color(getPrefix() + message);
    }

    public static String getMessage(String messagePath) {
        FileConfiguration lang = BeaconShield.getPlugin().getFileHandler().getLang();
        String message = lang.getString(messagePath);

        if (message == null) {
            message = "Can't find message: " + messagePath + " in lang.yml";
        }

        return format(message);
    }

    public static String getPrefix() {
        FileConfiguration lang = BeaconShield.getPlugin().getFileHandler().getLang();
        String prefix = lang.getString("prefix");
        return prefix == null ? "" : prefix;
    }

    public static void send(CommandSender sender, String messagePath) {
        sender.sendMessage(getMessage(messagePath));
    }

    public static void send(Player player, String messagePath) {
        player.sendMessage(getMessage(messagePath));
    }

    public static void send(CommandSender sender, String messagePath, String... args) {
        sender.sendMessage(
                Text.replace(getMessage(messagePath), args));
    }

    public static void sendRaw(CommandSender sender, String message) {
        sender.sendMessage(format(message));
    }

    public static void sendRaw(Player player, String message) {
        player.sendMessage(format(message));
    }
}
