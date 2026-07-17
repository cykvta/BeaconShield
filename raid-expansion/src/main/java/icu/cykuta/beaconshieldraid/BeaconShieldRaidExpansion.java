package icu.cykuta.beaconshieldraid;

import icu.cykuta.beaconshield.BeaconShield;
import icu.cykuta.beaconshield.BeaconShieldAPI;
import icu.cykuta.beaconshield.utils.RegistryUtils;
import icu.cykuta.beaconshieldraid.commands.CommandRaid;
import icu.cykuta.beaconshieldraid.config.RaidConfig;
import icu.cykuta.beaconshieldraid.listeners.NexusListener;
import icu.cykuta.beaconshieldraid.raid.RaidDatabase;
import icu.cykuta.beaconshieldraid.raid.RaidManager;
import icu.cykuta.beaconshieldraid.raid.RaidTickTask;
import icu.cykuta.beaconshieldraid.schedule.RaidSchedule;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.LocalDateTime;

/**
 * Entry point of the raid expansion. Its config, lang and database live
 * under the core's folder ({@code plugins/BeaconShield/expansions/<name>/}),
 * not the plugin's own data folder.
 */
public final class BeaconShieldRaidExpansion extends JavaPlugin {

    /** Capture tick period: once per second. */
    private static final long TICK_PERIOD = 20L;
    /** Schedule check period: once per minute. */
    private static final long SCHEDULE_PERIOD = 20L * 60L;
    /** How often the raid state is flushed to disk (crash safety). */
    private static final long SAVE_PERIOD = 20L * 60L;

    private File dataDir;
    private RaidConfig raidConfig;
    private RaidSchedule schedule;
    private RaidManager manager;
    private RaidDatabase database;

    @Override
    public void onEnable() {
        BeaconShieldAPI api = BeaconShield.getAPI();
        if (api == null) {
            getLogger().severe("BeaconShield core API not available; disabling.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // config/lang/data live inside the core folder, under expansions/.
        this.dataDir = new File(BeaconShield.getPlugin().getDataFolder(), "expansions/" + getName());
        if (!dataDir.exists() && !dataDir.mkdirs()) {
            getLogger().warning("Could not create the expansion data folder.");
        }

        this.raidConfig = new RaidConfig(this);
        this.schedule = new RaidSchedule(this, raidConfig);
        this.manager = new RaidManager(api, raidConfig);
        this.database = new RaidDatabase(dataDir, getLogger());

        // Restore raids from a previous run (survives restart/crash). The
        // core has already loaded its beacons at this point.
        database.load(manager, api);

        getServer().getPluginManager().registerEvents(new NexusListener(manager), this);

        // While a protection is being raided (past its freeze), neutralise
        // its territory upgrades (PvP, mob spawning, fall/drowning damage).
        api.setUpgradeSuppressor(chunk ->
                raidConfig.isIgnoreUpgradesDuringRaid() && manager.hasActiveRaid(chunk));

        // Raiders may grief the whole protection while its raid is active.
        api.setProtectionBypass(manager::isRaider);

        // Nobody can open the beacon menu while it is being raided.
        api.setBeaconInteractionGuard(manager::handleBeaconLock);

        // Register /bsraid through the core command framework (BaseCommand
        // + the server CommandMap), same as the core commands.
        RegistryUtils.getCommandMap().register("beaconshieldraid", new CommandRaid(this, manager));

        // Hot-point capture tick (touches the Bukkit API, must be sync).
        new RaidTickTask(manager).runTaskTimer(this, TICK_PERIOD, TICK_PERIOD);

        // Periodic schedule evaluation.
        getServer().getScheduler().runTaskTimer(this, this::applySchedule, TICK_PERIOD, SCHEDULE_PERIOD);

        // Periodically flush raid state so a crash loses at most one interval.
        getServer().getScheduler().runTaskTimer(this, this::saveRaids, SAVE_PERIOD, SAVE_PERIOD);
    }

    @Override
    public void onDisable() {
        BeaconShieldAPI api = BeaconShield.getAPI();
        if (api != null) {
            api.setUpgradeSuppressor(null);
            api.setProtectionBypass(null);
            api.setBeaconInteractionGuard(null);
        }
        // Persist raids so they resume on the next start (no reset here).
        saveRaids();
        if (manager != null) {
            manager.getDisplay().clearAll();
        }
        if (database != null) {
            database.close();
        }
    }

    /**
     * Copy a bundled resource into the expansion data folder if it is not
     * there yet, and return its file.
     */
    public File saveDefaultResource(String name) {
        File file = new File(dataDir, name);
        if (!file.exists()) {
            try (InputStream in = getResource(name)) {
                if (in != null) {
                    Files.copy(in, file.toPath());
                }
            } catch (IOException e) {
                getLogger().warning("Could not write default " + name + ": " + e.getMessage());
            }
        }
        return file;
    }

    /**
     * Apply the automatic schedule (no-op when the schedule is disabled).
     */
    private void applySchedule() {
        if (schedule.isEnabled()) {
            manager.setRaidingEnabled(schedule.isActiveAt(LocalDateTime.now()));
        }
    }

    /**
     * Reload config.yml/lang.yml and refresh the config/schedule views,
     * then re-apply the schedule immediately.
     */
    public void reloadPluginConfig() {
        raidConfig.reload();
        schedule.reload();
        applySchedule();
    }

    /**
     * Flush the raid state to the database now.
     */
    public void saveRaids() {
        if (database != null && manager != null) {
            database.save(manager);
        }
    }

    public File getDataDir() {
        return dataDir;
    }

    public RaidSchedule getSchedule() {
        return schedule;
    }

    public RaidManager getManager() {
        return manager;
    }
}
