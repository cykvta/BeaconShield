package icu.cykuta.beaconshield.upgrade;

import icu.cykuta.beaconshield.utils.UpgradeHelper;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DisablePvPUpgrade implements Upgrade<EntityDamageByEntityEvent> {

    @Override
    public @NotNull String getName() {
        return "disable_pvp";
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
    public void onEvent(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
            return;
        }

        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        if (UpgradeHelper.chunkHasUpgrade(this, event.getEntity().getLocation().getChunk())) {
            event.setCancelled(true);
        }
    }
}
