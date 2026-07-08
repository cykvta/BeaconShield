package icu.cykuta.beaconshield.beacon.fuel;

import icu.cykuta.beaconshield.BeaconShield;
import icu.cykuta.beaconshield.beacon.BeaconShieldBlock;
import icu.cykuta.beaconshield.config.ConfigHandler;
import icu.cykuta.beaconshield.config.PluginConfiguration;
import icu.cykuta.beaconshield.data.BeaconHandler;
import icu.cykuta.beaconshield.gui.views.BeaconGUI;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Locale;
import java.util.Map;

/**
 * Consumes 1 fuel per second from every beacon and refuels it from the
 * fuel storage slot when the fuel runs out.
 *
 * <p>Must run on the main thread: it touches blocks, particles,
 * inventories and persistent data containers.</p>
 */
public class FuelConsumeTask extends BukkitRunnable {
    private final PluginConfiguration config = ConfigHandler.getInstance().getConfig();
    private String warnedParticle;

    @Override
    public void run() {
        if (!BeaconShieldBlock.isFuelSystemEnabled()) {
            return;
        }

        BeaconHandler beaconHandler = BeaconHandler.getInstance();

        for (BeaconShieldBlock beacon : beaconHandler.getBeacons()) {
            BeaconGUI gui = beaconHandler.getLoadedBeaconGUI(beacon);

            if (beacon.getFuelLevel() > 0) {
                beacon.consumeFuel();
                this.runEffect(beacon.getBlock());
            }

            if (beacon.getFuelLevel() <= 0) {
                this.refuel(beacon, gui);
            }

            if (gui != null) {
                gui.renderInfoSlot();
            }
        }
    }

    /**
     * Try to consume one fuel item from the storage slot. If there is no
     * valid fuel, the beacon is marked as unprotected (fuel level -1).
     */
    private void refuel(BeaconShieldBlock beacon, BeaconGUI gui) {
        Map<Integer, ItemStack> storedItems = beacon.getPdcManager().getStoredItems();
        ItemStack fuel = storedItems.get(BeaconGUI.FUEL_STORAGE_SLOT);

        int burnTime = (fuel == null || fuel.getType().isAir() || fuel.getAmount() <= 0)
                ? 0
                : beacon.getBurnTime(fuel);

        if (burnTime <= 0) {
            beacon.setFuelLevel(-1);
            return;
        }

        beacon.setFuelLevel(burnTime);

        // Consume one fuel item
        fuel.setAmount(fuel.getAmount() - 1);
        ItemStack remaining = fuel.getAmount() <= 0 ? null : fuel;

        if (remaining == null) {
            storedItems.remove(BeaconGUI.FUEL_STORAGE_SLOT);
        } else {
            storedItems.put(BeaconGUI.FUEL_STORAGE_SLOT, remaining);
        }
        beacon.getPdcManager().setStoredItems(storedItems);

        // Keep the open GUI in sync
        if (gui != null) {
            gui.getInventory().setItem(BeaconGUI.FUEL_STORAGE_SLOT, remaining);
        }

        this.runEffect(beacon.getBlock());
    }

    /**
     * Spawn the configured particle effect at the beacon block, if enabled.
     */
    private void runEffect(Block block) {
        if (!this.config.getBoolean("fuel-particles")) {
            return;
        }

        Location loc = block.getLocation();
        loc.getWorld().spawnParticle(
                this.getParticle(),
                loc.getX() + 0.5, loc.getY() + 0.5, loc.getZ() + 0.5,
                5, 0.5, 0.5, 0.5, 0
        );
    }

    /**
     * Get the particle configured in "fuel-particle", falling back to
     * FLAME (with a single warning) when the name is invalid.
     */
    private Particle getParticle() {
        String name = this.config.getString("fuel-particle", "FLAME").toUpperCase(Locale.ROOT);

        try {
            return Particle.valueOf(name);
        } catch (IllegalArgumentException e) {
            if (!name.equals(this.warnedParticle)) {
                this.warnedParticle = name;
                BeaconShield.getPlugin().getLogger().warning(
                        "Unknown particle '" + name + "' in config.yml (fuel-particle), using FLAME.");
            }
            return Particle.FLAME;
        }
    }
}
