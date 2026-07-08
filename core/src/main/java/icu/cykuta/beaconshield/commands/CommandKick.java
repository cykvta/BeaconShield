package icu.cykuta.beaconshield.commands;

import icu.cykuta.beaconshield.beacon.BeaconShieldBlock;
import icu.cykuta.beaconshield.beacon.protection.PlayerRole;
import icu.cykuta.beaconshield.data.ProtectionHandler;
import icu.cykuta.beaconshield.utils.Chat;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Removes a member from the territory the sender is standing in.
 * Chat alternative to the member edit GUI.
 */
public class CommandKick extends BaseCommand {

    public CommandKick() {
        super("kick", "beaconshield.kick", CommandMode.PLAYER_ONLY);
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        if (args.length == 0) {
            Chat.send(player, "missing-argument");
            return false;
        }

        BeaconShieldBlock beacon = ProtectionHandler.getBeacon(player.getLocation().getChunk());
        if (beacon == null) {
            Chat.send(player, "not-in-territory");
            return false;
        }

        if (!beacon.hasPermissionLevel(player, PlayerRole.OFFICER)) {
            Chat.send(player, "no-permission-action");
            return false;
        }

        OfflinePlayer target = CommandInvite.resolvePlayer(args[0]);
        if (target == null) {
            Chat.send(player, "invalid-target");
            return false;
        }

        if (!beacon.hasMember(target)) {
            Chat.send(player, "not-a-member", target.getName());
            return false;
        }

        if (beacon.hasRole(target, PlayerRole.OWNER)) {
            Chat.send(player, "cannot-kick-owner");
            return false;
        }

        // Officers can only kick regular members; owners can kick anyone.
        if (beacon.hasRole(target, PlayerRole.OFFICER)
                && !beacon.hasPermissionLevel(player, PlayerRole.OWNER)) {
            Chat.send(player, "cannot-kick-officer");
            return false;
        }

        beacon.removeAllowedPlayer(target);
        Chat.send(player, "member-removed", target.getName());
        return true;
    }
}
