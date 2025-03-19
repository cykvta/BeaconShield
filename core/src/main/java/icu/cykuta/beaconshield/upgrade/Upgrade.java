package icu.cykuta.beaconshield.upgrade;

import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Upgrade<T extends Event> extends Listener {
    @NotNull
    String getName();

    @NotNull
    ItemStack getItemStack();

    @EventHandler
    void onEvent(T event);

    @Nullable
    ShapedRecipe getRecipe();
}
