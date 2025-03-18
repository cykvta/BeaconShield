package icu.cykuta.beaconshield.data;

import icu.cykuta.beaconshield.BeaconShield;
import icu.cykuta.beaconshield.beacon.BeaconShieldBlock;
import icu.cykuta.beaconshield.config.BeaconFile;
import icu.cykuta.beaconshield.gui.views.BeaconGUI;
import icu.cykuta.beaconshield.utils.GUIHelper;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BeaconHandler {
    private static BeaconHandler instance;
    private final Map<BeaconShieldBlock, Inventory> beaconShieldBlocks;

    public BeaconHandler() {
        this.beaconShieldBlocks = new HashMap<>();
        this.loadDataFiles();
    }

    /**
     * Grab all data files from the data folder and read them.
     */
    public void loadDataFiles() {
        File pluginDataFolder = BeaconShield.getPlugin().getDataFolder();
        File dataFolder = new File(pluginDataFolder, "data");

        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        File[] dataFiles = dataFolder.listFiles();

        if (dataFiles == null) {
            return;
        }

        for (File dataFile : dataFiles) {
            BeaconShieldBlock beacon = BeaconFile.readBeaconFromFile(dataFile);

            if (beacon != null) {
                // Add the block to the list of beacon shield blocks
                this.beaconShieldBlocks.put(beacon, null);
                ProtectionHandler.registerAllChunksForBeacon(beacon);
            }
        }
    }

    /**
     * Get the map of beacon shield blocks.
     * @return The map of beacon shield blocks.
     */
    public Map<BeaconShieldBlock, Inventory> getBeaconShieldMap() {
        return this.beaconShieldBlocks;
    }

    /**
     * Get list of all registered beacons.
     */
    public List<BeaconShieldBlock> getBeacons() {
        return new ArrayList<>(this.beaconShieldBlocks.keySet());
    }

    /**
     * Add a beacon shield block to the list of beacon shield blocks.
     * @param block The beacon shield block to add.
     */
    public void addBeaconShieldBlock(BeaconShieldBlock block) {
        this.beaconShieldBlocks.put(block, null);
    }

    /**
     * Remove a beacon shield block from the list of beacon shield blocks.
     * @param beacon The beacon shield block to remove.
     */
    public void removeBeaconShieldBlock(BeaconShieldBlock beacon) {
        this.beaconShieldBlocks.remove(beacon);
    }

    /**
     * Save all data in memory to disk.
     */
    public void saveDataInMemoryToDisk() {
        this.beaconShieldBlocks.forEach((beacon, inventory) -> {
            BeaconFile.writeBeaconToFile(beacon);
        });
    }

    /**
     * Check if a block is a beacon shield block in memory.
     * @param block The block to check.
     * @return True if the block is a beacon shield block in memory, false otherwise.
     */
    public boolean isBeaconShieldBlockInMemory(Block block) {
        for (BeaconShieldBlock beaconShieldBlock : this.beaconShieldBlocks.keySet()) {
            if (beaconShieldBlock.getBlock().equals(block)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Get a beacon shield block from memory.
     * @param block The block to get.
     * @return The beacon shield block if it exists, null otherwise.
     */
    public BeaconShieldBlock getBeaconShieldBlock(Block block) {
        for (BeaconShieldBlock beaconShieldBlock : this.beaconShieldBlocks.keySet()) {
            if (beaconShieldBlock.getBlock().equals(block)) {
                return beaconShieldBlock;
            }
        }

        return null;
    }

    /**
     * Get the inventory for a beacon shield block.
     * @param beacon The beacon shield block to get the inventory for.
     * @return The inventory for the beacon shield block.
     */
    @NotNull
    public Inventory getInventory(BeaconShieldBlock beacon) {
        Inventory inventory = this.beaconShieldBlocks.get(beacon);

        if (inventory == null) {
            inventory = GUIHelper.createInventory(new BeaconGUI(), beacon);
            this.setInventory(beacon, inventory);
        }

        return inventory;
    }

    /**
     * Set the inventory for a beacon shield block.
     * @param beacon The beacon shield block.
     * @param inventory The inventory.
     */
    public void setInventory(BeaconShieldBlock beacon, Inventory inventory) {
        this.beaconShieldBlocks.put(beacon, inventory);
    }

    /**
     * Get all beacon shield blocks owned by a player.
     * @param player The player to get the beacon shield blocks for.
     * @return A list of beacon shield blocks owned by the player.
     */
    public List<BeaconShieldBlock> getBeaconShieldBlocksByOwner(OfflinePlayer player) {
        return this.beaconShieldBlocks.keySet().stream().filter(
                beaconShieldBlock -> beaconShieldBlock.getOwner().equals(player)).collect(Collectors.toList());
    }

    /**
     * Get the instance of the BeaconHandler.
     * @return The instance of the BeaconHandler.
     */
    public static BeaconHandler getInstance() {
        if (instance == null) {
            instance = new BeaconHandler();
        }

        return instance;
    }
}
