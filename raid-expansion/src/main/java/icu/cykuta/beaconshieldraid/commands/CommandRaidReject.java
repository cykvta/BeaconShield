package icu.cykuta.beaconshieldraid.commands;

import icu.cykuta.beaconshieldraid.BeaconShieldRaidExpansion;
import icu.cykuta.beaconshieldraid.raid.Raid;
import icu.cykuta.beaconshieldraid.raid.RaidManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * {@code /bsraid reject <player>} — leader rejects a pending join request
 * during the freeze window.
 */
public class CommandRaidReject extends RaidSubCommand {

    public CommandRaidReject(BeaconShieldRaidExpansion plugin, RaidManager manager) {
        super("reject", null, CommandMode.PLAYER_ONLY, plugin, manager);
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        if (args.length == 0) {
            reply(sender, "&eUsage: /bsraid reject <player>");
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

        String targetName = Bukkit.getOfflinePlayer(target).getName();
        msg(sender, "leader-rejected", "&aRejected &6%player%&a's request.",
                "%player%", targetName == null ? "?" : targetName);

        Player targetPlayer = Bukkit.getPlayer(target);
        if (targetPlayer != null) {
            msg(targetPlayer, "request-rejected", "&cYour join request was rejected.");
        }
        return true;
    }
}
