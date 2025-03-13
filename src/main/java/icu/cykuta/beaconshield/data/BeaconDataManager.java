package icu.cykuta.beaconshield.data;

import icu.cykuta.beaconshield.BeaconShield;
import icu.cykuta.beaconshield.beacon.BeaconShieldBlock;
import icu.cykuta.beaconshield.gui.views.BeaconGUI;
import icu.cykuta.beaconshield.utils.FileUtils;
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

public class BeaconDataManager {
    private final Map<BeaconShieldBlock, Inventory> beaconShieldBlocks;

    public BeaconDataManager() {
        this.beaconShieldBlocks = new HashMap<>();
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
            BeaconShieldBlock block = FileUtils.readBeaconFromFile(dataFile);

            if (block != null) {
                // Add the block to the list of beacon shield blocks
                this.beaconShieldBlocks.put(block, null);
                // Register the block
                block.register();
            }
        }
    }

    public ArrayList<BeaconShieldBlock> getAllBeaconShieldBlocks() {
        return this.beaconShieldBlocks.keySet().stream().collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    public Map<BeaconShieldBlock, Inventory> getBeaconShieldMap() {
        return this.beaconShieldBlocks;
    }

    public ArrayList<Inventory> getInventories() {
        return this.beaconShieldBlocks.values().stream().collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    public void addBeaconShieldBlock(BeaconShieldBlock block) {
        this.beaconShieldBlocks.put(block, null);
    }

    public void removeBeaconShieldBlock(BeaconShieldBlock block) {
        this.beaconShieldBlocks.remove(block);
    }

    public void saveDataInMemoryToDisk() {
        this.beaconShieldBlocks.forEach((beaconShieldBlock, inventory) -> {
            beaconShieldBlock.save();
        });
    }

    public boolean isBeaconShieldBlockInMemory(Block block) {
        for (BeaconShieldBlock beaconShieldBlock : this.beaconShieldBlocks.keySet()) {
            if (beaconShieldBlock.getBlock().equals(block)) {
                return true;
            }
        }

        return false;
    }

    public BeaconShieldBlock getBeaconShieldBlock(Block block) {
        for (BeaconShieldBlock beaconShieldBlock : this.beaconShieldBlocks.keySet()) {
            if (beaconShieldBlock.getBlock().equals(block)) {
                return beaconShieldBlock;
            }
        }

        return null;
    }

    @NotNull
    public Inventory getInventory(BeaconShieldBlock beaconShieldBlock) {
        Inventory inventory = this.beaconShieldBlocks.get(beaconShieldBlock);

        if (inventory == null) {
            inventory = GUIHelper.createInventory(new BeaconGUI(), beaconShieldBlock);
            this.setInventory(beaconShieldBlock, inventory);
        }

        return inventory;
    }

    public void setInventory(BeaconShieldBlock block, Inventory inventory) {
        this.beaconShieldBlocks.put(block, inventory);
    }

    public List<BeaconShieldBlock> getBeaconShieldBlocksByOwner(OfflinePlayer player) {
        return this.beaconShieldBlocks.keySet().stream().filter(
                beaconShieldBlock -> beaconShieldBlock.getOwner().equals(player)).collect(Collectors.toList());
    }
}
