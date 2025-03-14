package icu.cykuta.beaconshield.config;

import icu.cykuta.beaconshield.utils.PluginConfiguration;

public class FileHandler {
    private final PluginConfig config;
    private final PluginConfig lang;

    public FileHandler() {
        this.config = new PluginConfig("config.yml");
        this.lang = new PluginConfig("lang.yml");
    }

    public void register() {
        this.config.register();
        this.lang.register();
    }

    public void save() {
        this.config.save();
        this.lang.save();
    }

    public void reload() {
        this.config.reload();
        this.lang.reload();
    }

    public PluginConfiguration getConfig() {
        return this.config.getFileConfiguration();
    }

    public PluginConfiguration getLang() {
        return this.lang.getFileConfiguration();
    }
}
