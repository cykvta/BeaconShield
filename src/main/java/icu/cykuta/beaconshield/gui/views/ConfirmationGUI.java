package icu.cykuta.beaconshield.gui.views;

import icu.cykuta.beaconshield.BeaconShield;
import icu.cykuta.beaconshield.gui.GUI;
import icu.cykuta.beaconshield.gui.GUIClick;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

public class ConfirmationGUI extends GUI {
    private final Consumer<GUIClick> consumer;
    private final GUI previousGUI;

    public ConfirmationGUI(GUI previousGUI, Consumer<GUIClick> consumer) {
        super("inventory-title-confirm", 27);
        this.consumer = consumer;
        this.previousGUI = previousGUI;
    }

    @Override
    public void populateInventory() {
        this.setDecorationSlots(
                 0,  1,  2,  3,  4,  5,  6,  7,  8,
                 9, 10,     12, 13, 14,     16, 17,
                18, 19, 20, 21, 22, 23, 24, 25, 26
        );

        FileConfiguration lang = BeaconShield.getPlugin().getFileHandler().getLang();
        this.addInventoryButton(11, lang.getString("button-yes"), Material.GREEN_WOOL, this.consumer);
        this.addInventoryButton(15, lang.getString("button-no"), Material.RED_WOOL,
                (guiClick) -> this.openGUI(guiClick.getClicker(), this.previousGUI));
    }
}
