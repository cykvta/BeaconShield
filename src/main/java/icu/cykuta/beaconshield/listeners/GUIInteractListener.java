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
            storedItems.remove(slot); // Remove the item if the new item is null (player is taking the item)

        } else if (existingItem == null) {
            storedItems.put(slot, newItem); // Place the new item if the slot is empty

        } else if (existingItem.isSimilar(newItem)) {
            int newAmount = existingItem.getAmount() + newItem.getAmount(); // Sum the amounts if the items are the same type
            existingItem.setAmount(newAmount);
            storedItems.put(slot, existingItem);

        } else {
            storedItems.put(slot, newItem); // Replace the item if it's a different type
        }

        beacon.setStoredItemsToPDC(storedItems); // Update the stored items in the PDC
    }
}