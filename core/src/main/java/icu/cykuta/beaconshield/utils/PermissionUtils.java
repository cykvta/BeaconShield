package icu.cykuta.beaconshield.utils;

import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

public class PermissionUtils {

    /**
     * Get a numeric limit from permissions like "prefix.N" (for example
     * "beaconshield.max-beacons.5"). The highest granted number wins;
     * when the player has no such permission the fallback is returned.
     *
     * @param player   The player to check.
     * @param prefix   The permission prefix, ending with a dot.
     * @param fallback The config value used when no permission matches.
     */
    public static int getLimit(Player player, String prefix, int fallback) {
        int limit = -1;

        for (PermissionAttachmentInfo info : player.getEffectivePermissions()) {
            String permission = info.getPermission();
            if (!info.getValue() || !permission.startsWith(prefix)) {
                continue;
            }

            try {
                limit = Math.max(limit, Integer.parseInt(permission.substring(prefix.length())));
            } catch (NumberFormatException ignored) {
                // Not a numeric limit permission, skip it
            }
        }

        return limit >= 0 ? limit : fallback;
    }
}
