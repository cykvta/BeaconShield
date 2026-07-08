package icu.cykuta.beaconshield.commands;

import icu.cykuta.beaconshield.beacon.BeaconShieldBlock;
import icu.cykuta.beaconshield.beacon.protection.PlayerRole;
import icu.cykuta.beaconshield.data.ProtectionHandler;
import icu.cykuta.beaconshield.utils.Chat;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Adds a player as member of the territory the sender is standing in.
 * Chat alternative to the invite GUI.
 */
public class CommandInvite extends BaseCommand {

    public CommandInvite() {
        super("invite", "beaconshield.invite", CommandMode.PLAYER_ONLY);
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

        if (!beacon.hasPermissionLevel(player, PlayerRole.OWNER)) {
            Chat.send(player, "no-permission-action");
            return false;
        }

        OfflinePlayer target = resolvePlayer(args[0]);
        if (target == null) {
            Chat.send(player, "invalid-target");
            return false;
        }

        if (beacon.hasMember(target)) {
            Chat.send(player, "already-member", target.getName());
            return false;
        }

        beacon.addAllowedPlayer(target, PlayerRole.MEMBER);
        Chat.send(player, "member-added", target.getName());
        return true;
    }

    /**
     * Resolve a player by name: online first, then the offline player
     * cache (players that have joined the server before).
     */
    static OfflinePlayer resolvePlayer(String name) {
        Player online = Bukkit.getPlayerExact(name);
        if (online != null) {
            return online;
        }

        for (OfflinePlayer offline : Bukkit.getOfflinePlayers()) {
            if (name.equalsIgnoreCase(offline.getName())) {
                return offline;
            }
        }
        return null;
    }
}
