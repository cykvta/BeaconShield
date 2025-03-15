package icu.cykuta.beaconshield;

import icu.cykuta.beaconshield.commands.CommandBeaconshield;
import icu.cykuta.beaconshield.data.BeaconHandler;
import icu.cykuta.beaconshield.task.FuelConsume;
import icu.cykuta.beaconshield.utils.RegistryUtils;
import org.bstats.bukkit.Metrics;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.java.JavaPlugin;

public final class BeaconShield extends JavaPlugin {
    private static BeaconShield instance;
    private CommandMap commandMap;
    private BeaconHandler beaconHandler;

    @Override
    public void onEnable() {
        // Save instance
        instance = this;

        // Metrics
        new Metrics(this, 25023);

        // Get command map
        this.commandMap = RegistryUtils.getCommandMap();

        // Register upgrades
        RegistryUtils.registerUpgrades();

        // Register commands
        RegistryUtils.registerCommand(new CommandBeaconshield());

        // Register events
        RegistryUtils.registerEvents();

        // Read data files
        this.beaconHandler = BeaconHandler.getInstance();

        // Every minute save memory data to disk
        getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            this.beaconHandler.saveDataInMemoryToDisk();
        }, 0, 60 * 20);

        // Fuel consume task
        new FuelConsume().runTaskTimerAsynchronously(this, 20, 20);
    }

    @Override
    public void onDisable() {
        this.beaconHandler.saveDataInMemoryToDisk();
    }

    public static BeaconShield getPlugin() {
        return instance;
    }

    public CommandMap getCommandMap() {
        return this.commandMap;
    }
}
