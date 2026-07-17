package icu.cykuta.beaconshieldraid.commands;

import icu.cykuta.beaconshield.beacon.BeaconShieldBlock;
import icu.cykuta.beaconshieldraid.BeaconShieldRaidExpansion;
import icu.cykuta.beaconshieldraid.raid.Raid;
import icu.cykuta.beaconshieldraid.raid.RaidManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * {@code /bsraid invite <player>} — the raid leader directly adds an online
 * player to the party during the freeze window.
 */
public class CommandRaidInvite extends RaidSubCommand {

    public CommandRaidInvite(BeaconShieldRaidExpansion plugin, RaidManager manager) {
        super("invite", null, CommandMode.PLAYER_ONLY, plugin, manager);
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        if (args.length == 0) {
            reply(sender, "&eUsage: /bsraid invite <player>");
            return false;
        }

        Raid raid = leaderRaidInFreeze(player);
        if (raid == null) {
            return false;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            msg(sender, "target-not-online", "&cThat player is not online.");
            return false;
        }

        BeaconShieldBlock beacon = manager.getBeaconById(raid.getBeaconId());
        if (beacon != null && beacon.hasMember(target)) {
            reply(sender, "&cThat player belongs to the protection.");
            return false;
        }
        if (raid.isInParty(target.getUniqueId())) {
            msg(sender, "already-in-party", "&eThat player is already in the party.");
            return false;
        }

        raid.removeRequest(target.getUniqueId());
        raid.addToParty(target.getUniqueId());
        raidPlugin.saveRaids();

        msg(sender, "raid-invited", "&aInvited &6%player% &ato the raid.", "%player%", target.getName());
        msg(target, "raid-invite-received", "&aYou were added to the raid on &6%owner%&a's protection.",
                "%owner%", beacon == null ? "?" : manager.ownerName(beacon));
        return true;
    }
}
