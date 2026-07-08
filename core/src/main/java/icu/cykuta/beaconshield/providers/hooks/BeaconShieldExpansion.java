package icu.cykuta.beaconshield.providers.hooks;

import icu.cykuta.beaconshield.BeaconShield;
import icu.cykuta.beaconshield.beacon.BeaconShieldBlock;
import icu.cykuta.beaconshield.config.ConfigHandler;
import icu.cykuta.beaconshield.data.BeaconHandler;
import icu.cykuta.beaconshield.data.ProtectionHandler;
import icu.cykuta.beaconshield.utils.Text;
import icu.cykuta.beaconshield.utils.Time;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * PlaceholderAPI expansion. Available placeholders:
 *
 * <ul>
 *   <li>%beaconshield_beacons% - beacons owned by the player</li>
 *   <li>%beaconshield_chunks% - chunks protected by the player's beacons</li>
 *   <li>%beaconshield_here_protected% - whether the player's chunk is protected</li>
 *   <li>%beaconshield_here_owner% - owner of the territory the player is in</li>
 *   <li>%beaconshield_here_chunks% - size of the territory the player is in</li>
 *   <li>%beaconshield_here_fuel_time% - remaining protection time of that territory</li>
 * </ul>
 */
public class BeaconShieldExpansion extends PlaceholderExpansion {

    @Override
    public @NotNull String getIdentifier() {
        return "beaconshield";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Cykuta";
    }

    @Override
    public @NotNull String getVersion() {
        return BeaconShield.getPlugin().getDescription().getVersion();
    }

    /**
     * Keep the expansion registered across PlaceholderAPI reloads.
     */
    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return null;
        }

        return switch (params) {
            case "beacons" -> String.valueOf(this.getOwnedBeacons(player));
            case "chunks" -> String.valueOf(this.getOwnedChunks(player));
            case "here_protected" -> String.valueOf(this.getBeaconHere(player) != null);
            case "here_owner" -> this.getHereInfo(player, HereInfo.OWNER);
            case "here_chunks" -> this.getHereInfo(player, HereInfo.CHUNKS);
            case "here_fuel_time" -> this.getHereInfo(player, HereInfo.FUEL_TIME);
            default -> null;
        };
    }

    private enum HereInfo {
        OWNER, CHUNKS, FUEL_TIME
    }

    private int getOwnedBeacons(Player player) {
        return BeaconHandler.getInstance().getBeaconShieldBlocksByOwner(player).size();
    }

    private int getOwnedChunks(Player player) {
        return BeaconHandler.getInstance().getBeaconShieldBlocksByOwner(player).stream()
                .mapToInt(beacon -> beacon.getProtectedChunks().size())
                .sum();
    }

    /**
     * Get the beacon protecting the chunk the player is standing in,
     * or null when the chunk is unprotected.
     */
    @Nullable
    private BeaconShieldBlock getBeaconHere(Player player) {
        return ProtectionHandler.getBeacon(player.getLocation().getChunk());
    }

    /**
     * Get information about the territory the player is standing in,
     * or an empty string when there is none.
     */
    private String getHereInfo(Player player, HereInfo info) {
        BeaconShieldBlock beacon = this.getBeaconHere(player);
        if (beacon == null) {
            return "";
        }

        return switch (info) {
            case OWNER -> beacon.getOwner().getName();
            case CHUNKS -> String.valueOf(beacon.getProtectedChunks().size());
            case FUEL_TIME -> this.getFuelTime(beacon);
        };
    }

    private String getFuelTime(BeaconShieldBlock beacon) {
        if (!BeaconShieldBlock.isFuelSystemEnabled()) {
            return Text.color(ConfigHandler.getInstance().getLang().getString("fuel-infinite"));
        }
        return Time.secondsToTime(Math.max(beacon.getFuelLevel(), 0));
    }
}
