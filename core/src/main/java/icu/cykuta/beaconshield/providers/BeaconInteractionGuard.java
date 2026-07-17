package icu.cykuta.beaconshield.providers;

import icu.cykuta.beaconshield.beacon.BeaconShieldBlock;
import org.bukkit.entity.Player;

/**
 * Optional hook that can lock a beacon's menu so nobody can open it. When
 * a guard is registered (see
 * {@code BeaconShieldAPI.setBeaconInteractionGuard}) and it returns
 * {@code true}, right-clicking the beacon does nothing. The guard may
 * message the player itself (e.g. "locked during a raid").
 *
 * <p>Used by the raid expansion to lock the beacon while it is being raided.
 */
@FunctionalInterface
public interface BeaconInteractionGuard {

    /**
     * @param beacon The beacon being interacted with.
     * @param player The player trying to open the menu.
     * @return {@code true} to block opening its menu.
     */
    boolean isLocked(BeaconShieldBlock beacon, Player player);
}
