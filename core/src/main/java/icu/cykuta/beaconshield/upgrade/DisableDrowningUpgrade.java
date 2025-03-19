package icu.cykuta.beaconshield.upgrade;

import icu.cykuta.beaconshield.utils.UpgradeHelper;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityAirChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class DisableDrowningUpgrade implements Upgrade<EntityAirChangeEvent> {

    @Override
    public @NotNull String getName() {
        return "disable_drowning";
    }

    @Override
    public @NotNull ItemStack getItemStack() {
        return UpgradeHelper.itemMaker(this);
    }

    @Override
    public @Nullable ShapedRecipe getRecipe() {
        return UpgradeHelper.createRecipe(this);
    }

    @Override
    @EventHandler
    public void onEvent(EntityAirChangeEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        if (UpgradeHelper.chunkHasUpgrade(this, event.getEntity().getLocation().getChunk())) {
            event.setCancelled(true);
        }
    }
}
