package icu.cykuta.beaconshield.events;

import icu.cykuta.beaconshield.gui.GUIHolder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class GUIInteractEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private boolean isCancelled;
    private final Player player;
    private final InventoryAction action;
    private final GUIHolder inventoryHolder;
    private final ClickType click;
    private final ItemStack cursor;
    private final ItemStack clickedItem;
    private final int slot;
    private final Runnable cancelAction;

    public GUIInteractEvent(
            Player player,
            InventoryAction action,
            GUIHolder inventoryHolder,
            ClickType click,
            ItemStack cursor,
            ItemStack clickedItem,
            int slot,
            Runnable cancelAction
    ) {
        this.isCancelled = false;
        this.player = player;
        this.inventoryHolder = inventoryHolder;
        this.action = action;
        this.click = click;
        this.cursor = cursor;
        this.clickedItem = clickedItem;
        this.slot = slot;
        this.cancelAction = cancelAction;
    }

    public Player getPlayer() {
        return this.player;
    }

    public GUIHolder getInventoryHolder() {
        return this.inventoryHolder;
    }

    public InventoryAction getAction() {
        return this.action;
    }

    public ClickType getClick() {
        return this.click;
    }

    public ItemStack getCursor() {
        return this.cursor;
    }

    public ItemStack getClickedItem() {
        if (this.clickedItem == null) {
            return new ItemStack(Material.AIR);
        }
        return this.clickedItem;
    }

    public int getSlot() {
        return this.slot;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.isCancelled = b;
        if (b) {
            this.cancelAction.run();
        }
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }
}
