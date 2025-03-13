package icu.cykuta.beaconshield.gui;

import icu.cykuta.beaconshield.beacon.BeaconShieldBlock;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class GUIHolder implements InventoryHolder {
    private final BeaconShieldBlock beaconBlock;
    private final GUI gui;

    public GUIHolder(BeaconShieldBlock beaconBlock, GUI gui) {
        this.beaconBlock = beaconBlock;
        this.gui = gui;
    }

    public BeaconShieldBlock getBeaconBlock() {
        return this.beaconBlock;
    }

    public GUI getGUI() {
        return this.gui;
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return null;
    }
}
