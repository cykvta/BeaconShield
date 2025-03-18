package icu.cykuta.beaconshield.utils;

import net.md_5.bungee.api.ChatColor;

import javax.annotation.Nullable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Text {

    /**
     * Colorize a string with the '&' character or hex color codes.
     * @param message The message to colorize.
     * @return The colorized message.
     */
    public static String color(String message) {
        Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");
        Matcher matcher = pattern.matcher(message);
        while (matcher.find()) {
            String hexCode = message.substring(matcher.start(), matcher.end());
            String replaceSharp = hexCode.replace('#', 'x');

            char[] ch = replaceSharp.toCharArray();
            StringBuilder builder = new StringBuilder("");
            for (char c : ch) {
                builder.append("&" + c);
            }

            message = message.replace(hexCode, builder.toString());
            matcher = pattern.matcher(message);
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * Strip color codes from a string.
     * @param message The message to strip color codes from.
     * @return The message without color codes.
     */
    public static String stripColor(String message) {
        return message.replaceAll("§[a-fA-F0-9]", "");
    }

    /**
     * Replace placeholders in a string with the provided replacements.
     * @param text The text to replace placeholders in.
     * @param replacements The replacements to use.
     * @return The text with the placeholders replaced.
     */
    public static String replace(String text, String... replacements) {
        if (text == null || replacements == null || replacements.length == 0) {
            return text;
        }

        StringBuilder result = new StringBuilder(text);

        for (int i = 0; i < replacements.length; i++) {
            String placeholder = "{" + i + "}";
            String replacement = (replacements[i] != null) ? replacements[i] : "";

            int index;
            while ((index = result.indexOf(placeholder)) != -1) {
                result.replace(index, index + placeholder.length(), replacement);
            }
        }

        return result.toString();
    }

    /**
     * Replace placeholders in a list of strings with the provided replacements.
     * @param text The list of strings to replace placeholders in.
     * @param replacements The replacements to use.
     * @return The list of strings with the placeholders replaced.
     */
    public static List<String> replace(@Nullable List<String> text, String... replacements) {
        if (text == null) {
            return new java.util.ArrayList<>();
        }
        return text.stream().map(s -> replace(s, replacements)).collect(Collectors.toList());
    }
}
