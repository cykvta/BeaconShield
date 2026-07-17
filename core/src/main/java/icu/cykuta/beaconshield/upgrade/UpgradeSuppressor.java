package icu.cykuta.beaconshield.upgrade;

import org.bukkit.Chunk;

/**
 * Optional hook that lets an external plugin temporarily neutralise all
 * territory upgrades on a chunk without touching their items. When a
 * suppressor is registered (see
 * {@code BeaconShieldAPI.setUpgradeSuppressor}) and it returns
 * {@code true} for a chunk, every upgrade behaves as if absent there.
 *
 * <p>Used by the raid expansion to re-enable PvP, mob spawning and
 * environmental damage inside a protection while it is being raided.
 */
@FunctionalInterface
public interface UpgradeSuppressor {

    /**
     * @param chunk A chunk that belongs to a protection.
     * @return {@code true} to disable the protection's upgrades on it.
     */
    boolean isSuppressed(Chunk chunk);
}
