package icu.cykuta.beaconshield.listeners;

import icu.cykuta.beaconshield.beacon.BeaconShieldBlock;
import icu.cykuta.beaconshield.events.GUIInteractEvent;
import icu.cykuta.beaconshield.gui.GUI;
import icu.cykuta.beaconshield.gui.GUIClick;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.function.Consumer;

public class GUIInteractListener implements Listener {

    @EventHandler
    public void onInventoryInteract(GUIInteractEvent event) {
        GUI gui = event.getInventoryHolder().getGUI();
        BeaconShieldBlock beacon = event.getInventoryHolder().getBeaconBlock();

        switch (gui.getSlotType(event.getSlot())) {
            case BUTTON:
                handleButtonClick(event, gui);
                break;

            case STORAGE:
                handleStorageClick(event, beacon);
                break;

            case DECORATION:
            case NONE:
                event.setCancelled(true);
                break;
        }
    }

    /**
     * Handles button click events.
     * @param event The GUIInteractEvent.
     * @param gui The GUI being interacted with.
     */
    private void handleButtonClick(GUIInteractEvent event, GUI gui) {
        Consumer<GUIClick> buttonAction = gui.getButtonSlotAction(event.getSlot());
        if (buttonAction != null) {
            event.setCancelled(true);
            buttonAction.accept(new GUIClick(event.getClick(), event.getPlayer()));
        }
    }

    /**
     * Handles storage slot click events.
     * @param event The GUIInteractEvent.
     * @param beacon The BeaconShieldBlock associated with the GUI.
     */
    private void handleStorageClick(GUIInteractEvent event, BeaconShieldBlock beacon) {
        if (event.getClick() == ClickType.LEFT) {
            updateStorageSlot(beacon, event.getSlot(), event.getCursor());
        } else {
            event.setCancelled(true);
        }
    }

    /**
     * Updates the storage slot with the new item stack.
     * @param beacon The BeaconShieldBlock associated with the GUI.
     * @param slot The slot being updated.
     * @param newItem The new item stack to place in the slot.
     */
    private void updateStorageSlot(BeaconShieldBlock beacon, int slot, ItemStack newItem) {
        Map<Integer, ItemStack> storedItems = beacon.getStoredItemsFromPDC();
        ItemStack existingItem = storedItems.get(slot);

        if (newItem == null) {
            // Remove the item if the new item is null (player is taking the item)
            storedItems.remove(slot);
        } else if (existingItem == null) {
            // Place the new item if the slot is empty
            storedItems.put(slot, newItem);
        } else if (existingItem.isSimilar(newItem)) {
            // Sum the amounts if the items are the same type
            int newAmount = existingItem.getAmount() + newItem.getAmount();
            existingItem.setAmount(newAmount);
            storedItems.put(slot, existingItem);
        } else {
            // Replace the item if it's a different type
            storedItems.put(slot, newItem);
        }

        // Update the stored items in the PDC
        beacon.setStoredItemsToPDC(storedItems);
    }
}