package icu.cykuta.beaconshield.utils;

public class Text {

    public static String color(String text) {
        return text.replace("&", "§");
    }

    public static String stripColor(String message) {
        return message.replaceAll("§[a-fA-F0-9]", "");
    }

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
}
