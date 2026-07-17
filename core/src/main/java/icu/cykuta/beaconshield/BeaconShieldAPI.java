package icu.cykuta.beaconshield;

import icu.cykuta.beaconshield.beacon.BeaconShieldBlock;
import icu.cykuta.beaconshield.beacon.protection.ProtectedChunk;
import icu.cykuta.beaconshield.data.BeaconHandler;
import icu.cykuta.beaconshield.data.HookHandler;
import icu.cykuta.beaconshield.data.ProtectionHandler;
import icu.cykuta.beaconshield.data.UpgradeHandler;
import icu.cykuta.beaconshield.listeners.BeaconBlockListener;
import icu.cykuta.beaconshield.listeners.ProtectionInteractListener;
import icu.cykuta.beaconshield.providers.BeaconInteractionGuard;
import icu.cykuta.beaconshield.providers.ProtectionBypass;
import icu.cykuta.beaconshield.upgrade.Upgrade;
import icu.cykuta.beaconshield.upgrade.UpgradeSuppressor;
import icu.cykuta.beaconshield.utils.RegistryUtils;
import icu.cykuta.beaconshield.utils.UpgradeHelper;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BeaconShieldAPI {

    /**
     * Get list of all registered beacons
     */
    public List<BeaconShieldBlock> getBeacons() {
        return BeaconHandler.getInstance().getBeacons();
    }

    /**
     * Register a upgrade
     */
    public void registerUpgrade(Upgrade<?> upgrade) {
        RegistryUtils.addUpgrade(upgrade);
    }

    /**
     * Check if this chunk is protected by a beacon
     */
    public boolean isChunkProtected(Chunk chunk) {
        return ProtectionHandler.isChunkProtected(chunk);
    }

    /**
     * Get the beacon that protects this chunk
     */
    public BeaconShieldBlock getBeacon(Chunk chunk) {
        return ProtectionHandler.getBeacon(chunk);
    }

    /**
     * Get all registered upgrades
     */
    public List<Upgrade> getUpgrades() {
        return UpgradeHandler.getUpgrades();
    }

    /**
     * Get an upgrade by its name
     */
    public Upgrade getUpgrade(String name) {
        return UpgradeHandler.getUpgrade(name);
    }

    /**
     * Get the beacon that owns a chunk, whether or not its protection is
     * currently active. Unlike {@link #getBeacon(Chunk)} this also finds
     * beacons whose protection over the chunk has been suppressed (see
     * {@link #suppressChunkProtection(ProtectedChunk)}).
     */
    @Nullable
    public BeaconShieldBlock getBeaconOwning(ProtectedChunk chunk) {
        for (BeaconShieldBlock beacon : getBeacons()) {
            if (beacon.isProtectedChunk(chunk) || beacon.isCoreChunk(chunk)) {
                return beacon;
            }
        }
        return null;
    }

    /**
     * Temporarily disable BeaconShield's protection over a single chunk
     * without removing it from its beacon. The chunk stops resolving in
     * the protection index, so every core protection check (breaking,
     * building, interacting, explosions, fire, pistons, liquids) treats
     * it as unprotected until {@link #restoreChunkProtection} is called.
     *
     * <p>Intended for expansions such as raids that need to open a chunk
     * up to grief while keeping the territory intact.
     */
    public void suppressChunkProtection(ProtectedChunk chunk) {
        ProtectionHandler.removeChunk(chunk);
    }

    /**
     * Re-enable BeaconShield's protection over a chunk previously passed
     * to {@link #suppressChunkProtection(ProtectedChunk)}.
     */
    public void restoreChunkProtection(ProtectedChunk chunk, BeaconShieldBlock beacon) {
        ProtectionHandler.addChunk(chunk, beacon);
    }

    /**
     * Check whether a chunk currently resolves to an active protection in
     * the index (i.e. it is not suppressed).
     */
    public boolean isChunkProtectionActive(ProtectedChunk chunk) {
        return ProtectionHandler.isChunkProtected(chunk);
    }

    /**
     * Fully destroy a beacon through the canonical destruction path
     * (drops loot optionally, tears down the protection, deletes the data
     * file and fires {@code BeaconShieldDestroyedEvent}).
     *
     * @param beacon    The beacon to destroy.
     * @param destroyer The player responsible, or {@code null}.
     * @param dropLoot  Whether to drop the beacon item and stored items.
     */
    public void destroyBeacon(BeaconShieldBlock beacon, @Nullable Player destroyer, boolean dropLoot) {
        BeaconHandler.getInstance().destroyBeacon(beacon, destroyer, dropLoot);
    }

    /**
     * Register a hook that can disable all territory upgrades on a chunk
     * (PvP, mob spawning, fall/drowning damage) without removing their
     * items. Pass {@code null} to clear it. Used by the raid expansion to
     * re-enable combat inside a protection while it is being raided.
     */
    public void setUpgradeSuppressor(@Nullable UpgradeSuppressor suppressor) {
        UpgradeHelper.setUpgradeSuppressor(suppressor);
    }

    /**
     * Register a hook that grants a player the right to build/break/interact
     * inside a protected chunk they normally could not (e.g. raiders on a
     * raided protection). Pass {@code null} to clear it.
     */
    public void setProtectionBypass(@Nullable ProtectionBypass bypass) {
        ProtectionInteractListener.setProtectionBypass(bypass);
    }

    /**
     * Register a hook that can lock a beacon's menu so nobody can open it
     * (e.g. while it is being raided). Pass {@code null} to clear it.
     */
    public void setBeaconInteractionGuard(@Nullable BeaconInteractionGuard guard) {
        BeaconBlockListener.setInteractionGuard(guard);
    }

    /**
     * Whether a Vault economy is hooked. When it is not, the economy
     * helpers below behave as "free" (balances are treated as sufficient
     * and withdrawals always succeed).
     */
    public boolean isEconomyAvailable() {
        return HookHandler.getInstance().economyHook.isEnabled();
    }

    /**
     * Get a player's balance, or 0 when no economy is available.
     */
    public double getBalance(OfflinePlayer player) {
        Economy economy = economy();
        return economy == null ? 0 : economy.getBalance(player);
    }

    /**
     * Check whether a player can afford an amount (always true when no
     * economy is available).
     */
    public boolean hasBalance(OfflinePlayer player, double amount) {
        Economy economy = economy();
        return economy == null || economy.has(player, amount);
    }

    /**
     * Withdraw an amount from a player. Returns true on success, and also
     * when no economy is available (free).
     */
    public boolean withdraw(OfflinePlayer player, double amount) {
        Economy economy = economy();
        if (economy == null) {
            return true;
        }
        return economy.withdrawPlayer(player, amount).transactionSuccess();
    }

    @Nullable
    private Economy economy() {
        return isEconomyAvailable() ? HookHandler.getInstance().economyHook.getHook() : null;
    }
}
