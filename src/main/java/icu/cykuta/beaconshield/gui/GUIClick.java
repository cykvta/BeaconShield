package icu.cykuta.beaconshield.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

public record GUIClick(ClickType clickType, Player clicker) {
}
