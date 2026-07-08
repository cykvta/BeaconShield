package icu.cykuta.beaconshield.listeners;

import icu.cykuta.beaconshield.BeaconShield;
import icu.cykuta.beaconshield.data.BeaconHandler;
import icu.cykuta.beaconshield.gui.GUI;
import icu.cykuta.beaconshield.gui.GUIClick;
import icu.cykuta.beaconshield.gui.GUIHolder;
import icu.cykuta.beaconshield.gui.views.BeaconGUI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Routes inventory events to the {@link GUI} being viewed:
 * buttons run their action, storage slots accept/give items
 * (including furnace-style shift-clicking) and everything else
 * is cancelled.
 */
public class GUIListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof GUIHolder holder)) {
            return;
        }

        GUI gui = holder.getGUI();

        // The beacon may have been destroyed while this menu was open
        if (!BeaconHandler.getInstance().isRegistered(gui.getBeacon())) {
            event.setCancelled(true);
            Bukkit.getScheduler().runTask(BeaconShield.getPlugin(),
                    () -> event.getWhoClicked().closeInventory());
            return;
        }

        // Shift-clicks are handled manually, furnace-style
        if (event.getClick().isShiftClick()) {
            event.setCancelled(true);
            this.handleShiftClick(event, gui);
            return;
        }

        // Collect-to-cursor could pull decoration/button items out of the GUI
        if (event.getClick() == ClickType.DOUBLE_CLICK) {
            event.setCancelled(true);
            return;
        }

        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory == null || clickedInventory.getType() == InventoryType.PLAYER) {
            return;
        }

        switch (gui.getSlotType(event.getSlot())) {
            case BUTTON -> this.handleButtonClick(event, gui);
            case STORAGE -> this.handleStorageClick(event, gui);
            default -> event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof GUIHolder)) {
            return;
        }

        // Cancel drags that touch the GUI; drags contained in the player
        // inventory are fine.
        int topSize = event.getView().getTopInventory().getSize();
        if (event.getRawSlots().stream().anyMatch(rawSlot -> rawSlot < topSize)) {
            event.setCancelled(true);
        }
    }

    private void handleButtonClick(InventoryClickEvent event, GUI gui) {
        event.setCancelled(true);

        Consumer<GUIClick> action = gui.getButtonAction(event.getSlot());
        if (action != null) {
            action.accept(new GUIClick(event.getClick(), (Player) event.getWhoClicked()));
        }
    }

    /**
     * Storage slots: let vanilla resolve the click (place, take, merge or
     * split the stack) and persist the resulting slot content next tick.
     */
    private void handleStorageClick(InventoryClickEvent event, GUI gui) {
        if (event.getClick() != ClickType.LEFT && event.getClick() != ClickType.RIGHT) {
            event.setCancelled(true);
            return;
        }

        Bukkit.getScheduler().runTask(BeaconShield.getPlugin(), gui::persistStorage);
    }

    /**
     * Furnace-style shift-click: items from the player inventory are routed
     * into the matching storage slots (fuel or upgrades), and items in
     * storage slots are moved back to the player inventory.
     */
    private void handleShiftClick(InventoryClickEvent event, GUI gui) {
        Inventory clickedInventory = event.getClickedInventory();
        ItemStack item = event.getCurrentItem();

        if (clickedInventory == null || item == null || item.getType().isAir()) {
            return;
        }

        Player player = (Player) event.getWhoClicked();

        // Player inventory -> GUI storage (only the beacon menu has storage)
        if (clickedInventory.getType() == InventoryType.PLAYER) {
            if (gui instanceof BeaconGUI beaconGUI) {
                event.setCurrentItem(beaconGUI.storeItem(item));
                player.updateInventory();
            }
            return;
        }

        // GUI storage -> player inventory
        if (gui.getSlotType(event.getSlot()) == GUI.SlotType.STORAGE) {
            Map<Integer, ItemStack> leftover = player.getInventory().addItem(item.clone());
            ItemStack remaining = leftover.isEmpty() ? null : leftover.values().iterator().next();

            gui.getInventory().setItem(event.getSlot(), remaining);
            gui.persistStorage();
            player.updateInventory();
        }
    }
}
