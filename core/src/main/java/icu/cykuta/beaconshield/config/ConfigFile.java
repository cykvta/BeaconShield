package icu.cykuta.beaconshield.config;

import icu.cykuta.beaconshield.BeaconShield;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class ConfigFile {
    private PluginConfiguration fileConfiguration;
    private final BeaconShield plugin = BeaconShield.getPlugin();
    private File file;
    private final String fileName;

    public ConfigFile(String fileName) {
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

        this.fileConfiguration = PluginConfiguration.loadConfiguration(this.file);
        this.matchConfig(this.fileConfiguration, fileName);
    }

    /**
     * Add new default values to the config file while preserving comments.
     * @param config The config file.
     * @param fileName The file to match in the resources.
     */
    public void matchConfig(PluginConfiguration config, String fileName) {
        try {
            InputStream is = BeaconShield.getPlugin().getResource(fileName);
            if (is != null) {
                PluginConfiguration defConfig = PluginConfiguration.loadConfiguration(is);

                for (String key : defConfig.getConfigurationSection("").getKeys(true)) {
                    if (!config.contains(key)) {
                        config.set(key, defConfig.get(key));
                    }
                }

                for (String key : config.getConfigurationSection("").getKeys(true)) {
                    if (!defConfig.contains(key)) {
                        config.set(key, null);
                    }
                }

                config.save(this.file);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
