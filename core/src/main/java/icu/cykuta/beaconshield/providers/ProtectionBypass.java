package icu.cykuta.beaconshield.providers;

import org.bukkit.Chunk;
import org.bukkit.entity.Player;

/**
 * Optional hook that lets an external plugin grant a player the right to
 * act (build, break, interact) inside a protected chunk they normally
 * could not. When a bypass is registered (see
 * {@code BeaconShieldAPI.setProtectionBypass}) and it returns {@code true}
 * for a (player, chunk) pair, the core treats the action as allowed.
 *
 * <p>Used by the raid expansion so that raiders can grief a protection
 * that is being raided.
 */
@FunctionalInterface
public interface ProtectionBypass {

    /**
     * @param player The acting player.
     * @param chunk  The protected chunk the action targets.
     * @return {@code true} to allow the action despite the protection.
     */
    boolean allows(Player player, Chunk chunk);
}
