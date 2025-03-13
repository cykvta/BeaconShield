package icu.cykuta.beaconshield.upgrade;

import org.bukkit.Material;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class DisableMobSpawningUpgrade extends Upgrade {
    public DisableMobSpawningUpgrade() {
        super("disable_mob_spawning");

    }

    @EventHandler
    public void onEvent(EntitySpawnEvent event) {
        if (!(event.getEntity() instanceof Monster)) {
            return;
        }

        if (this.chunkHasUpgrade(event.getEntity().getLocation().getChunk())) {
            event.setCancelled(true);
        }

    }

    @Override
    public @NotNull ItemStack getItemStack() {
        return Upgrade.itemMaker(Material.WARD_ARMOR_TRIM_SMITHING_TEMPLATE,
                "disable-mob-spawning-name",
                "disable-mob-spawning-desc");
    }
}
