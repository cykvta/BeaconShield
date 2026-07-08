package icu.cykuta.beaconshield.gui.views;

import icu.cykuta.beaconshield.beacon.BeaconShieldBlock;
import icu.cykuta.beaconshield.gui.GUI;
import icu.cykuta.beaconshield.gui.GUIClick;

import java.util.function.Consumer;

/**
 * Simple confirm/deny dialog. On deny the previous GUI is reopened.
 */
public class ConfirmationGUI extends GUI {
    private final GUI previousGUI;
    private final Consumer<GUIClick> onConfirm;

    public ConfirmationGUI(BeaconShieldBlock beacon, GUI previousGUI, Consumer<GUIClick> onConfirm) {
        super(beacon, "inventory-title-confirm", 27);
        this.previousGUI = previousGUI;
        this.onConfirm = onConfirm;
    }

    @Override
    protected void populate() {
        this.addDecoration(
                 0,  1,  2,  3,  4,  5,  6,  7,  8,
                 9, 10,     12, 13, 14,     16, 17,
                18, 19, 20, 21, 22, 23, 24, 25, 26
        );

        this.addButton(11, "confirmation.confirm", this.onConfirm);
        this.addButton(15, "confirmation.deny", click -> this.previousGUI.open(click.clicker()));
    }
}
