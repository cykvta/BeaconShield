package icu.cykuta.beaconshield.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

public class GUIClick {
    private final ClickType clickType;
    private final Player clicker;

    public GUIClick(ClickType clickType, Player clicker) {
        this.clickType = clickType;
        this.clicker = clicker;
    }

    public ClickType getClickType() {
        return clickType;
    }

    public Player getClicker() {
        return clicker;
    }
}
