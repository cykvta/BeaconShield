package icu.cykuta.beaconshield.config;

import icu.cykuta.api.config.ConfigManager;
import icu.cykuta.api.config.PluginConfiguration;
import icu.cykuta.beaconshield.BeaconShield;

/**
 * Named access to BeaconShield's yml files, backed by the API's
 * {@link ConfigManager} — which handles creating them from the bundled
 * resources, merging in keys added by an update, saving and reloading.
 */
public class ConfigHandler {
    private static final ConfigHandler instance = new ConfigHandler();

    private final ConfigManager manager = new ConfigManager(BeaconShield.getPlugin());

    public ConfigHandler() {
        this.register();
    }

    public void register() {
        this.manager.register("config.yml");
        this.manager.register("lang.yml");
        this.manager.register("gui.yml");
        this.manager.register("upgrade.yml");
    }

    public void save() {
        this.manager.saveAll();
    }

    public void reload() {
        this.manager.reloadAll();
    }

    public PluginConfiguration getConfig() {
        return this.manager.get("config.yml");
    }

    public PluginConfiguration getLang() {
        return this.manager.get("lang.yml");
    }

    public PluginConfiguration getGui() {
        return this.manager.get("gui.yml");
    }

    public PluginConfiguration getUpgrade() {
        return this.manager.get("upgrade.yml");
    }

    public static ConfigHandler getInstance() {
        return instance;
    }
}
