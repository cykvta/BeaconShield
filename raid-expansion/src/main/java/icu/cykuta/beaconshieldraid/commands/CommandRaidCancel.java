package icu.cykuta.beaconshieldraid.commands;

import icu.cykuta.beaconshield.beacon.BeaconShieldBlock;
import icu.cykuta.beaconshieldraid.BeaconShieldRaidExpansion;
import icu.cykuta.beaconshieldraid.raid.Raid;
import icu.cykuta.beaconshieldraid.raid.RaidManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * {@code /bsraid cancel} — cancel a raid. The raid leader can cancel their
 * own raid from anywhere; an admin can cancel the raid on the protection
 * they are standing in.
 */
public class CommandRaidCancel extends RaidSubCommand {

    public CommandRaidCancel(BeaconShieldRaidExpansion plugin, RaidManager manager) {
        // Permission checked inside (leader OR admin).
        super("cancel", null, CommandMode.PLAYER_ONLY, plugin, manager);
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        // Prefer the protection the player stands in, else the raid they lead.
        BeaconShieldBlock beacon = protectionAt(player);
        Raid raid = beacon == null ? null : manager.getRaid(beacon);
        if (raid == null) {
            raid = manager.findRaidOfPlayer(player.getUniqueId());
            beacon = raid == null ? null : manager.getBeaconById(raid.getBeaconId());
        }

        if (raid == null || beacon == null) {
            reply(sender, "&eThere is no raid here to cancel.");
            return false;
        }

        boolean admin = player.hasPermission("beaconshield.raid.admin");
        if (!admin && !raid.isLeader(player.getUniqueId())) {
            msg(sender, "not-raid-leader", "&cOnly the raid leader or an admin can cancel it.");
            return false;
        }

        manager.cancelRaid(beacon);
        raidPlugin.saveRaids();
        reply(sender, "&aCancelled the raid on &6" + manager.ownerName(beacon) + "&a's protection.");
        return true;
    }
}
