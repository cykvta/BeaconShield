package icu.cykuta.beaconshield.data;

import icu.cykuta.beaconshield.BeaconShield;
import icu.cykuta.beaconshield.beacon.BeaconShieldBlock;
import icu.cykuta.beaconshield.config.BeaconFile;
import icu.cykuta.beaconshield.gui.GUIHolder;
import icu.cykuta.beaconshield.gui.views.BeaconGUI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Registry of all beacon shields loaded in memory, along with their
 * cached main GUI (created lazily on first open).
 */
public class BeaconHandler {
    private static BeaconHandler instance;

    private final Map<BeaconShieldBlock, BeaconGUI> beacons = new HashMap<>();

    private BeaconHandler() {
        this.loadDataFiles();
    }

    /**
     * Read every beacon data file from the data folder.
     */
    private void loadDataFiles() {
        File dataFolder = new File(BeaconShield.getPlugin().getDataFolder(), "data");

        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            return;
        }

        File[] dataFiles = dataFolder.listFiles();
        if (dataFiles == null) {
            return;
        }

        for (File dataFile : dataFiles) {
            BeaconShieldBlock beacon = BeaconFile.readBeaconFromFile(dataFile);

            if (beacon != null) {
                this.beacons.put(beacon, null);
                ProtectionHandler.registerAllChunksForBeacon(beacon);
            }
        }
    }

    /**
     * Get a list of all registered beacons.
     */
    public List<BeaconShieldBlock> getBeacons() {
        return new ArrayList<>(this.beacons.keySet());
    }

    public void addBeaconShieldBlock(BeaconShieldBlock beacon) {
        this.beacons.put(beacon, null);
    }

    public void removeBeaconShieldBlock(BeaconShieldBlock beacon) {
        this.beacons.remove(beacon);
    }

    public boolean isRegistered(BeaconShieldBlock beacon) {
        return this.beacons.containsKey(beacon);
    }

    /**
     * Save all beacons in memory to disk.
     */
    public void saveDataInMemoryToDisk() {
        this.beacons.keySet().forEach(BeaconFile::writeBeaconToFile);
    }

    /**
     * Get the beacon shield registered at the position of the given block.
     *
     * @return The beacon, or null if the block is not a beacon shield.
     */
    @Nullable
    public BeaconShieldBlock getBeaconShieldBlock(Block block) {
        for (BeaconShieldBlock beacon : this.beacons.keySet()) {
            if (beacon.isAt(block)) {
                return beacon;
            }
        }
        return null;
    }

    /**
     * Get the cached main GUI of a beacon, creating it if needed.
     * Unregistered (destroyed) beacons get a transient GUI that is
     * never cached, so they cannot be resurrected in the registry.
     */
    @NotNull
    public BeaconGUI getBeaconGUI(BeaconShieldBlock beacon) {
        BeaconGUI gui = this.beacons.get(beacon);
        if (gui != null) {
            return gui;
        }

        gui = new BeaconGUI(beacon);
        if (this.beacons.containsKey(beacon)) {
            this.beacons.put(beacon, gui);
        }
        return gui;
    }

    /**
     * Get the main GUI of a beacon only if its inventory was already built.
     */
    @Nullable
    public BeaconGUI getLoadedBeaconGUI(BeaconShieldBlock beacon) {
        BeaconGUI gui = this.beacons.get(beacon);
        return (gui != null && gui.isBuilt()) ? gui : null;
    }

    /**
     * Close every open BeaconShield menu and drop the cached GUIs so
     * they are rebuilt with fresh config values on the next open.
     */
    public void invalidateGUIs() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getOpenInventory().getTopInventory().getHolder() instanceof GUIHolder) {
                player.closeInventory();
            }
        }

        this.beacons.replaceAll((beacon, gui) -> null);
    }

    /**
     * Get all beacon shields owned by a player.
     */
    public List<BeaconShieldBlock> getBeaconShieldBlocksByOwner(OfflinePlayer player) {
        return this.beacons.keySet().stream()
                .filter(beacon -> beacon.getOwner().getUniqueId().equals(player.getUniqueId()))
                .collect(Collectors.toList());
    }

    public static BeaconHandler getInstance() {
        if (instance == null) {
            instance = new BeaconHandler();
        }
        return instance;
    }
}
