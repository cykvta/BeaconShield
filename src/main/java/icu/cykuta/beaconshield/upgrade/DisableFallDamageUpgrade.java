package icu.cykuta.beaconshield.upgrade;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;


public class DisableFallDamageUpgrade extends Upgrade {

    public DisableFallDamageUpgrade() {
        super("disable_fall_damage");
    }

    @EventHandler
    public void onEvent(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) {
            return;
        }

        if (this.chunkHasUpgrade(event.getEntity().getLocation().getChunk())) {
            event.setCancelled(true);
        }
    }

    @Override
    public @NotNull ItemStack getItemStack() {
        return Upgrade.itemMaker(Material.WILD_ARMOR_TRIM_SMITHING_TEMPLATE,
                "disable-fall-damage-name",
                "disable-fall-damage-desc");
    }
}
