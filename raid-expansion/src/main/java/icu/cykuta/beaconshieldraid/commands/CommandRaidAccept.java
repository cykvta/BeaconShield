package icu.cykuta.beaconshieldraid.commands;

import icu.cykuta.beaconshield.beacon.BeaconShieldBlock;
import icu.cykuta.beaconshieldraid.BeaconShieldRaidExpansion;
import icu.cykuta.beaconshieldraid.raid.Raid;
import icu.cykuta.beaconshieldraid.raid.RaidManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * {@code /bsraid accept <player>} — leader accepts a pending join request
 * during the freeze window.
 */
public class CommandRaidAccept extends RaidSubCommand {

    public CommandRaidAccept(BeaconShieldRaidExpansion plugin, RaidManager manager) {
        super("accept", null, CommandMode.PLAYER_ONLY, plugin, manager);
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        if (args.length == 0) {
            reply(sender, "&eUsage: /bsraid accept <player>");
            return false;
        }

        Raid raid = leaderRaidInFreeze(player);
        if (raid == null) {
            return false;
        }

        UUID target = resolveInSet(raid.getRequests(), args[0]);
        if (target == null) {
            msg(sender, "no-such-request", "&cNo pending request from that player.");
            return false;
        }

        raid.removeRequest(target);
        raid.addToParty(target);
        raidPlugin.saveRaids();

        String targetName = Bukkit.getOfflinePlayer(target).getName();
        msg(sender, "leader-accepted", "&aAccepted &6%player%&a into the raid.",
                "%player%", targetName == null ? "?" : targetName);

        Player targetPlayer = Bukkit.getPlayer(target);
        if (targetPlayer != null) {
            BeaconShieldBlock beacon = manager.getBeaconById(raid.getBeaconId());
            msg(targetPlayer, "request-accepted", "&aYour join request was accepted!",
                    "%owner%", beacon == null ? "?" : manager.ownerName(beacon));
        }
        return true;
    }
}
