package icu.cykuta.beaconshield;

import icu.cykuta.beaconshield.commands.CommandBeaconshield;
import icu.cykuta.beaconshield.config.FileHandler;
import icu.cykuta.beaconshield.data.BeaconDataManager;
import icu.cykuta.beaconshield.data.HookHandler;
import icu.cykuta.beaconshield.task.FuelConsume;
import icu.cykuta.beaconshield.utils.RegistryUtils;
import org.bstats.bukkit.Metrics;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.java.JavaPlugin;

public final class BeaconShield extends JavaPlugin {
    private FileHandler fileHandler;
    private CommandMap commandMap;
    private BeaconDataManager beaconDataManager;
    private HookHandler hookHandler;

    /**
     * TODO Current:
     *      - add fuel system to the beacon via inventory ** Need to be tested, a lot of bugs
     *      - add a system to add upgrades to the beacon (disable mob spawn, disable friendly fire, etc)
     * <p>
     * TODO Future:
     *      - add a system to raid beacons based on chunk conquest
     */

    @Override
    public void onEnable() {
        // Metrics
        new Metrics(this, 25023);

        // Register config
        this.fileHandler = new FileHandler();
        this.fileHandler.register();

        // Register hooks
        this.hookHandler = new HookHandler();
        this.hookHandler.registerHooks();

        // Get command map
        this.commandMap = RegistryUtils.getCommandMap();

        // Register upgrades
        RegistryUtils.registerUpgrades();

        // Register commands
        RegistryUtils.registerCommand(new CommandBeaconshield());

        // Register events
        RegistryUtils.registerEvents();

        // Read data files
        this.beaconDataManager = new BeaconDataManager();
        this.beaconDataManager.loadDataFiles();

        // Every minute save memory data to disk
        getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            this.beaconDataManager.saveDataInMemoryToDisk();
        }, 0, 60 * 20);

        // These two tasks are too dirty, coded at 5 am refactor this shit
        new FuelConsume().runTaskTimerAsynchronously(this, 20, 20);
    }

    @Override
    public void onDisable() {
        // Save data files
        this.beaconDataManager.saveDataInMemoryToDisk();
    }

    public static BeaconShield getPlugin() {
        return getPlugin(BeaconShield.class);
    }

    public FileHandler getFileHandler() {
        return this.fileHandler;
    }

    public CommandMap getCommandMap() {
        return this.commandMap;
    }

    public BeaconDataManager getBeaconDataManager() {
        return this.beaconDataManager;
    }

    public HookHandler getHookHandler() {
        return this.hookHandler;
    }
}
