package icu.cykuta.beaconshield;

import icu.cykuta.beaconshield.beacon.BeaconShieldBlock;
import icu.cykuta.beaconshield.data.BeaconHandler;
import icu.cykuta.beaconshield.data.ProtectionHandler;
import icu.cykuta.beaconshield.data.UpgradeHandler;
import icu.cykuta.beaconshield.upgrade.Upgrade;
import icu.cykuta.beaconshield.utils.RegistryUtils;
import org.bukkit.Chunk;

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
}
