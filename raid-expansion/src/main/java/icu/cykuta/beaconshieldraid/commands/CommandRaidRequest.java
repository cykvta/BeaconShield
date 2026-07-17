package icu.cykuta.beaconshieldraid.commands;

import icu.cykuta.beaconshield.beacon.BeaconShieldBlock;
import icu.cykuta.beaconshieldraid.BeaconShieldRaidExpansion;
import icu.cykuta.beaconshieldraid.raid.Raid;
import icu.cykuta.beaconshieldraid.raid.RaidManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * {@code /bsraid request [raid]} — ask the raid leader to let you join.
 * Only works during the freeze window and within the join radius, from
 * anywhere in it: standing inside the protection is not required. The
 * leader then accepts or rejects.
 *
 * <p>The raid argument is the id of the raided beacon, as put in the
 * clickable invite. Typed by hand without it, the raid is the one of the
 * protection the player stands in.
 */
public class CommandRaidRequest extends RaidSubCommand {

    public CommandRaidRequest(BeaconShieldRaidExpansion plugin, RaidManager manager) {
        super("request", "beaconshield.raid.join", CommandMode.PLAYER_ONLY, plugin, manager);
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        // The invite is sent within the join radius of the raid origin, so
        // most of its recipients stand outside the protection: its button
        // names the raid rather than relying on where the player is.
        Raid raid;
        BeaconShieldBlock beacon;
        if (args.length > 0) {
            raid = manager.getRaidById(args[0]);
            beacon = raid == null ? null : manager.getBeaconById(raid.getBeaconId());
        } else {
            beacon = protectionAt(player);
            raid = beacon == null ? null : manager.getRaid(beacon);
        }

        if (raid == null || beacon == null) {
            msg(sender, "no-active-raid", "&eNo raid here yet. Start one with /bsraid start.");
            return false;
        }

        if (beacon.hasMember(player)) {
            msg(sender, "cannot-raid-own", "&cYou can't raid a protection you belong to.");
            return false;
        }
        if (raid.isInParty(player.getUniqueId())) {
            reply(sender, "&eYou already joined this raid.");
            return false;
        }
        if (!raid.isInFreeze()) {
            msg(sender, "join-window-closed", "&cThe join window has closed; the raid already started.");
            return false;
        }
        if (!manager.isWithinJoinRadius(raid, player)) {
            msg(sender, "join-too-far", "&cYou are too far from the raid to request (within %radius% blocks).",
                    "%radius%", String.valueOf((int) manager.getConfig().getJoinRadius()));
            return false;
        }
        if (raid.hasRequest(player.getUniqueId())) {
            msg(sender, "already-requested", "&eYou already requested to join.");
            return false;
        }

        raid.addRequest(player.getUniqueId());
        manager.notifyLeaderOfRequest(beacon, raid, player);
        msg(sender, "raid-request-sent", "&aJoin request sent to the raid leader.");
        return true;
    }
}
