package icu.cykuta.beaconshield.upgrade;

import icu.cykuta.beaconshield.utils.UpgradeHelper;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;


public class DisableFallDamageUpgrade implements Upgrade<EntityDamageEvent> {

    @Override
    public @NotNull String getName() {
        return "disable_fall_damage";
    }

    @Override
    public @NotNull ItemStack getItemStack() {
        return UpgradeHelper.itemMaker(Material.WILD_ARMOR_TRIM_SMITHING_TEMPLATE,
                "disable-fall-damage-name",
                "disable-fall-damage-desc");
    }

    @Override
    @EventHandler
    public void onEvent(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) {
            return;
        }

        if (UpgradeHelper.chunkHasUpgrade(this, event.getEntity().getLocation().getChunk())) {
            event.setCancelled(true);
        }
    }
}
