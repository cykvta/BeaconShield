package icu.cykuta.beaconshield.config;

import icu.cykuta.beaconshield.BeaconShield;
import icu.cykuta.beaconshield.utils.PluginConfiguration;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;

public class PluginConfig {
    private PluginConfiguration fileConfiguration;
    private final BeaconShield plugin = BeaconShield.getPlugin();
    private File file;
    private final String fileName;

    public PluginConfig(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Creates the file if it doesn't exist.
     */
    public void register() {
        this.file = new File(plugin.getDataFolder(), fileName);

        if (!this.file.exists()) {
            plugin.saveResource(fileName, false);
        }

        this.fileConfiguration = PluginConfiguration.adapt(YamlConfiguration.loadConfiguration(this.file));
    }

    /**
     * Save the file.
     *
     * @throws RuntimeException if an IOException occurs.
     */
    public void save() {
        try {
            this.fileConfiguration.save(this.file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Reload the file.
     *
     * @throws RuntimeException if an IOException or InvalidConfigurationException occurs.
     */
    public void reload() {
        try {
            this.fileConfiguration.load(this.file);
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public PluginConfiguration getFileConfiguration() {
        return this.fileConfiguration;
    }
}
