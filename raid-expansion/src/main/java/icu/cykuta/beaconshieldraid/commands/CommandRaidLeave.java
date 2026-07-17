package icu.cykuta.beaconshieldraid.commands;

import icu.cykuta.beaconshield.beacon.BeaconShieldBlock;
import icu.cykuta.beaconshieldraid.BeaconShieldRaidExpansion;
import icu.cykuta.beaconshieldraid.raid.Raid;
import icu.cykuta.beaconshieldraid.raid.RaidManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * {@code /bsraid leave} — leave the raid you are part of (from anywhere,
 * at any time). If the leader leaves, the raid is cancelled.
 */
public class CommandRaidLeave extends RaidSubCommand {

    public CommandRaidLeave(BeaconShieldRaidExpansion plugin, RaidManager manager) {
        super("leave", "beaconshield.raid.join", CommandMode.PLAYER_ONLY, plugin, manager);
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        Raid raid = manager.findRaidOfPlayer(player.getUniqueId());
        if (raid == null) {
            msg(sender, "not-in-a-raid", "&eYou are not part of any raid.");
            return false;
        }

        BeaconShieldBlock beacon = manager.getBeaconById(raid.getBeaconId());

        if (raid.isLeader(player.getUniqueId())) {
            if (beacon != null) {
                manager.leaderLeft(beacon);
                raidPlugin.saveRaids();
            }
            reply(sender, "&aYou left and cancelled the raid.");
            return true;
        }

        raid.removeFromParty(player.getUniqueId());
        raidPlugin.saveRaids();
        msg(sender, "raid-left", "&aYou left the raid.");
        return true;
    }
}
