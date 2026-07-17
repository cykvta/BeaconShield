package icu.cykuta.beaconshieldraid.listeners;

import icu.cykuta.beaconshield.beacon.BeaconShieldBlock;
import icu.cykuta.beaconshield.events.BeaconShieldDestroyedEvent;
import icu.cykuta.beaconshieldraid.config.RaidConfig;
import icu.cykuta.beaconshieldraid.events.ProtectionRaidedEvent;
import icu.cykuta.beaconshieldraid.raid.Raid;
import icu.cykuta.beaconshieldraid.raid.RaidManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

/**
 * Turns the beacon into a nexus. Once every non-core chunk of a
 * protection is captured, each hit an attacker lands on the beacon block
 * drains one nexus health; when it reaches zero the protection falls and
 * the beacon is destroyed.
 */
public class NexusListener implements Listener {

    private final RaidManager manager;

    public NexusListener(RaidManager manager) {
        this.manager = manager;
    }

    /**
     * The core cancels every beacon break (beacons cannot be mined), so we
     * observe the cancelled event to count hits and never un-cancel it —
     * the block only really disappears when we destroy it at 0 HP.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void onNexusHit(BlockBreakEvent event) {
        BeaconShieldBlock beacon = BeaconShieldBlock.getBeaconShieldBlock(event.getBlock());
        if (beacon == null) {
            return;
        }

        Raid raid = manager.getRaid(beacon);
        if (raid == null || raid.isInFreeze()) {
            return; // no active raid, or still in the freeze window
        }

        Player player = event.getPlayer();
        // Only the attacking party (or an open forced raid) can breach it.
        if (!manager.countsAsAttacker(beacon, raid, player)) {
            return;
        }

        if (!manager.isNexusVulnerable(beacon, raid)) {
            return; // still chunks left to capture
        }

        RaidConfig config = manager.getConfig();
        if (!raid.isNexusStarted()) {
            raid.startNexus(config.getNexusHealth());
        }

        int remaining = raid.damageNexus();
        if (remaining > 0) {
            String message = config.message("nexus-damaged",
                    "%owner%", manager.ownerName(beacon),
                    "%health%", String.valueOf(remaining),
                    "%max_health%", String.valueOf(config.getNexusHealth()));
            if (message != null) {
                player.sendMessage(message);
            }
            return;
        }

        // Nexus destroyed: the protection falls.
        Bukkit.getPluginManager().callEvent(new ProtectionRaidedEvent(beacon, player));
        manager.broadcast(config.message("nexus-destroyed", "%owner%", manager.ownerName(beacon)));
        manager.getApi().destroyBeacon(beacon, player, config.isNexusDropLoot());
    }

    /**
     * Clean up the raid state whenever a beacon is destroyed. If a defender
     * (owner/member) removed the beacon mid-raid, announce that the raid
     * ended; the nexus/fuel paths already announce their own outcome.
     */
    @EventHandler
    public void onBeaconDestroyed(BeaconShieldDestroyedEvent event) {
        BeaconShieldBlock beacon = event.getBeaconShieldBlock();
        if (manager.getRaid(beacon) == null) {
            return;
        }

        Player destroyer = event.getPlayer();
        if (destroyer != null && beacon.hasMember(destroyer)) {
            manager.broadcast(manager.getConfig().message("raid-ended-beacon-removed",
                    "%owner%", manager.ownerName(beacon)));
        }
        manager.removeRaid(beacon);
    }
}
