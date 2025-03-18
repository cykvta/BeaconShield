package icu.cykuta.beaconshield;

import icu.cykuta.beaconshield.data.BeaconHandler;
import icu.cykuta.beaconshield.beacon.fuel.FuelConsumeTask;
import icu.cykuta.beaconshield.utils.RegistryUtils;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

public final class BeaconShield extends JavaPlugin {
    private BeaconHandler beaconHandler;
    private static BeaconShield instance;
    private static BeaconShieldAPI api;

    @Override
    public void onEnable() {
        instance = this;
        api = new BeaconShieldAPI();

        // Metrics
        new Metrics(this, 25023);

        // Register upgrades, commands and events
        RegistryUtils.registerUpgrades();
        RegistryUtils.registerCommands();
        RegistryUtils.registerEvents();

        // Read data files
        this.beaconHandler = BeaconHandler.getInstance();

        // Every minute save memory data to disk
        getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            this.beaconHandler.saveDataInMemoryToDisk();
        }, 0, 60 * 20);

        // Fuel consume task
        new FuelConsumeTask().runTaskTimerAsynchronously(this, 20, 20);
    }

    @Override
    public void onDisable() {
        this.beaconHandler.saveDataInMemoryToDisk();
    }

    public static BeaconShield getPlugin() {
        return instance;
    }

    public static BeaconShieldAPI getAPI() {
        return api;
    }
}
