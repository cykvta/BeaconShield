package icu.cykuta.beaconshield.upgrade;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityAirChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class DisableDrowningUpgrade extends Upgrade {
    public DisableDrowningUpgrade() {
        super("disable_drowning");
    }

    @EventHandler
    public void onPlayerMove(EntityAirChangeEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        if (this.chunkHasUpgrade(event.getEntity().getLocation().getChunk())) {
            event.setCancelled(true);
        }
    }


    @Override
    public @NotNull ItemStack getItemStack() {
        return Upgrade.itemMaker(Material.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE, "disable-drowning-name", "disable-drowning-desc");
    }
}
