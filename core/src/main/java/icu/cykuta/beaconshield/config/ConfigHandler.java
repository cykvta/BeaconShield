package icu.cykuta.beaconshield.config;

public class ConfigHandler {
    private static final ConfigHandler instance = new ConfigHandler();

    private final ConfigFile config = new ConfigFile("config.yml");
    private final ConfigFile lang = new ConfigFile("lang.yml");
    private final ConfigFile gui = new ConfigFile("gui.yml");
    private final ConfigFile upgrade = new ConfigFile("upgrade.yml");

    public ConfigHandler() {
        this.register();
    }

    public void register() {
        this.config.register();
        this.lang.register();
        this.gui.register();
        this.upgrade.register();
    }

    public void save() {
        this.config.save();
        this.lang.save();
        this.gui.save();
        this.upgrade.save();
    }

    public void reload() {
        this.config.reload();
        this.lang.reload();
        this.gui.reload();
        this.upgrade.reload();
    }

    public PluginConfiguration getConfig() {
        return this.config.getFileConfiguration();
    }

    public PluginConfiguration getLang() {
        return this.lang.getFileConfiguration();
    }

    public PluginConfiguration getGui() {
        return this.gui.getFileConfiguration();
    }

    public PluginConfiguration getUpgrade() {
        return this.upgrade.getFileConfiguration();
    }

    public static ConfigHandler getInstance() {
        if (instance == null) {
            return new ConfigHandler();
        }
        return instance;
    }
}
