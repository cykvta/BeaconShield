package icu.cykuta.beaconshieldraid.commands;

import icu.cykuta.beaconshieldraid.BeaconShieldRaidExpansion;
import icu.cykuta.beaconshieldraid.raid.Raid;
import icu.cykuta.beaconshieldraid.raid.RaidManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * {@code /bsraid kick <player>} — the raid leader removes a member from the
 * party during the freeze window.
 */
public class CommandRaidKick extends RaidSubCommand {

    public CommandRaidKick(BeaconShieldRaidExpansion plugin, RaidManager manager) {
        super("kick", null, CommandMode.PLAYER_ONLY, plugin, manager);
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        if (args.length == 0) {
            reply(sender, "&eUsage: /bsraid kick <player>");
            return false;
        }

        Raid raid = leaderRaidInFreeze(player);
        if (raid == null) {
            return false;
        }

        UUID target = resolveInSet(raid.getParty(), args[0]);
        if (target == null) {
            msg(sender, "not-in-party", "&eThat player is not in the party.");
            return false;
        }
        if (raid.isLeader(target)) {
            reply(sender, "&cYou can't remove yourself (the leader).");
            return false;
        }

        raid.removeFromParty(target);
        raidPlugin.saveRaids();

        String targetName = Bukkit.getOfflinePlayer(target).getName();
        msg(sender, "raid-kicked", "&aRemoved &6%player% &afrom the raid.",
                "%player%", targetName == null ? "?" : targetName);

        Player targetPlayer = Bukkit.getPlayer(target);
        if (targetPlayer != null) {
            msg(targetPlayer, "raid-kick-received", "&cYou were removed from the raid.");
        }
        return true;
    }
}
