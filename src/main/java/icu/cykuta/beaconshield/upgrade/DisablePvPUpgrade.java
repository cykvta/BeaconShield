package icu.cykuta.beaconshield.upgrade;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class DisablePvPUpgrade extends Upgrade {
    public DisablePvPUpgrade() {
        super("disable_pvp");
    }

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

        if (this.chunkHasUpgrade(event.getEntity().getLocation().getChunk())) {
            event.setCancelled(true);
        }
    }

    @Override
    public @NotNull ItemStack getItemStack() {
        return Upgrade.itemMaker(Material.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE,
                "disable-pvp-name",
                "disable-pvp-desc");
    }
}
