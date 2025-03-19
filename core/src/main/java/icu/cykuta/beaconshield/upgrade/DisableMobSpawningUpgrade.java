package icu.cykuta.beaconshield.upgrade;

import icu.cykuta.beaconshield.utils.UpgradeHelper;
import org.bukkit.Material;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DisableMobSpawningUpgrade implements Upgrade<EntitySpawnEvent> {

    @Override
    public @NotNull String getName() {
        return "disable_mob_spawning";
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
    public void onEvent(EntitySpawnEvent event) {
        if (!(event.getEntity() instanceof Monster)) {
            return;
        }

        if (UpgradeHelper.chunkHasUpgrade(this, event.getEntity().getLocation().getChunk())) {
            event.setCancelled(true);
        }
    }
}
