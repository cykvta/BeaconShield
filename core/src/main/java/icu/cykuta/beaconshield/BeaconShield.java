package icu.cykuta.beaconshield;

import icu.cykuta.beaconshield.config.ConfigHandler;
import icu.cykuta.beaconshield.data.BeaconHandler;
import icu.cykuta.beaconshield.beacon.fuel.FuelConsumeTask;
import icu.cykuta.beaconshield.providers.hooks.BeaconShieldExpansion;
import icu.cykuta.beaconshield.utils.RegistryUtils;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

public final class BeaconShield extends JavaPlugin {
    private static final long SAVE_INTERVAL_TICKS = 60L * 20L;

    private static BeaconShield instance;
    private static BeaconShieldAPI api;
    private BeaconHandler beaconHandler;

    @Override
    public void onEnable() {
        instance = this;
        api = new BeaconShieldAPI();

        // Anonymous usage metrics (bstats.org), can be disabled in config.yml
        if (ConfigHandler.getInstance().getConfig().getBoolean("metrics-enabled", true)) {
            new Metrics(this, 25023);
        }

        // Register upgrades, commands, events and recipes
        RegistryUtils.registerUpgrades();
        RegistryUtils.registerCommands();
        RegistryUtils.registerEvents();
        RegistryUtils.registerRecipes();

        // Read data files
        this.beaconHandler = BeaconHandler.getInstance();

        // PlaceholderAPI placeholders (%beaconshield_*%)
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new BeaconShieldExpansion().register();
        }

        // Periodically save memory data to disk. Runs on the main thread:
        // beacons are mutated on the main thread and serializing them from
        // another thread could write corrupt data.
        getServer().getScheduler().runTaskTimer(this,
                this.beaconHandler::saveDataInMemoryToDisk, SAVE_INTERVAL_TICKS, SAVE_INTERVAL_TICKS);

        // Fuel consume task (touches the Bukkit API, must be sync)
        new FuelConsumeTask().runTaskTimer(this, 20, 20);
    }

    @Override
    public void onDisable() {
        if (this.beaconHandler != null) {
            this.beaconHandler.saveDataInMemoryToDisk();
        }
    }

    public static BeaconShield getPlugin() {
        return instance;
    }

    public static BeaconShieldAPI getAPI() {
        return api;
    }
}
