package icu.cykuta.beaconshield.utils;

import icu.cykuta.beaconshield.config.ConfigHandler;
import icu.cykuta.beaconshield.config.PluginConfiguration;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Chat {
    /**
     * Format a message with the plugin's prefix.
     * @param message The message to format.
     * @return The formatted message.
     */
    public static String format(String message) {
        return Text.color(getPrefix() + message);
    }

    /**
     * Get a formatted message from the language file.
     * @param messagePath The path to the message in the language file.
     * @return The message.
     */
    public static String getMessage(String messagePath) {
        PluginConfiguration lang = ConfigHandler.getInstance().getLang();
        String message = lang.getString(messagePath);

        return format(message);
    }

    /**
     * Get the plugin's prefix.
     * @return The plugin's prefix.
     */
    public static String getPrefix() {
        PluginConfiguration lang = ConfigHandler.getInstance().getLang();
        return lang.getString("prefix");
    }

    /**
     * Send a message to a command sender.
     * @param sender The command sender to send the message to.
     * @param messagePath The path to the message in the language file.
     */
    public static void send(CommandSender sender, String messagePath) {
        sender.sendMessage(getMessage(messagePath));
    }

    /**
     * Send a message to a player.
     * @param player The player to send the message to.
     * @param messagePath The path to the message in the language file.
     */
    public static void send(Player player, String messagePath) {
        player.sendMessage(getMessage(messagePath));
    }

    /**
     * Send a message to a command sender with replacements.
     * @param sender The command sender to send the message to.
     * @param messagePath The path to the message in the language file.
     * @param args The placeholders to replace in the message.
     */
    public static void send(CommandSender sender, String messagePath, String... args) {
        sender.sendMessage(
                Text.replace(getMessage(messagePath), args));
    }

    /**
     * Send a message to a player with replacements.
     * @param sender The player to send the message to.
     * @param message The message to send.
     */
    public static void sendRaw(CommandSender sender, String message) {
        sender.sendMessage(format(message));
    }

    /**
     * Send a message to a player with replacements.
     * @param player The player to send the message to.
     * @param message The message to send.
     */
    public static void sendRaw(Player player, String message) {
        player.sendMessage(format(message));
    }
}
