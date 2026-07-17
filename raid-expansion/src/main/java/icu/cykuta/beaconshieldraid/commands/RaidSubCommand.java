package icu.cykuta.beaconshieldraid.commands;

import icu.cykuta.beaconshield.beacon.BeaconShieldBlock;
import icu.cykuta.beaconshield.beacon.protection.ProtectedChunk;
import icu.cykuta.beaconshield.commands.BaseCommand;
import icu.cykuta.beaconshieldraid.BeaconShieldRaidExpansion;
import icu.cykuta.beaconshieldraid.raid.Raid;
import icu.cykuta.beaconshieldraid.raid.RaidManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

/**
 * Base for every {@code /bsraid} sub-command. Reuses the core
 * {@link BaseCommand} (sender-mode and permission handling, sub-command
 * dispatch) and adds shared helpers for the raid expansion.
 */
public abstract class RaidSubCommand extends BaseCommand {

    protected final BeaconShieldRaidExpansion raidPlugin;
    protected final RaidManager manager;

    protected RaidSubCommand(String command, String permission, CommandMode mode,
                             BeaconShieldRaidExpansion raidPlugin, RaidManager manager) {
        super(command, permission, mode);
        this.raidPlugin = raidPlugin;
        this.manager = manager;
    }

    /**
     * Send a raid message from a config key, falling back to a default
     * when the key is missing/empty.
     */
    protected void msg(CommandSender sender, String key, String fallback, String... replacements) {
        String message = manager.getConfig().message(key, replacements);
        if (message == null) {
            message = manager.getConfig().format(applyReplacements(fallback, replacements));
        }
        sender.sendMessage(message);
    }

    /**
     * Send an ad-hoc colored reply with the raid prefix.
     */
    protected void reply(CommandSender sender, String message) {
        sender.sendMessage(manager.getConfig().format(message));
    }

    /**
     * The protection at a player's current chunk (resolves even suppressed
     * chunks), or {@code null}.
     */
    protected BeaconShieldBlock protectionAt(Player player) {
        ProtectedChunk chunk = new ProtectedChunk(player.getLocation().getChunk());
        return manager.getApi().getBeaconOwning(chunk);
    }

    /**
     * Resolve the raid the player leads at their location, requiring it to
     * still be in the freeze window. Sends the appropriate error and
     * returns {@code null} when any condition fails. Used by invite/kick.
     */
    protected Raid leaderRaidInFreeze(Player player) {
        BeaconShieldBlock beacon = protectionAt(player);
        if (beacon == null) {
            reply(player, "&cYou are not standing in a protection.");
            return null;
        }
        Raid raid = manager.getRaid(beacon);
        if (raid == null) {
            msg(player, "no-active-raid", "&eNo raid here yet. Start one with /bsraid start.");
            return null;
        }
        if (!raid.isLeader(player.getUniqueId())) {
            msg(player, "not-raid-leader", "&cOnly the raid leader can do that.");
            return null;
        }
        if (!raid.isInFreeze()) {
            msg(player, "not-in-freeze", "&cYou can only do that during the freeze time.");
            return null;
        }
        return raid;
    }

    /**
     * Resolve a player name to a UUID contained in the given set (party or
     * requests). Checks online players first, then offline names.
     */
    protected UUID resolveInSet(Set<UUID> set, String name) {
        Player online = Bukkit.getPlayerExact(name);
        if (online != null && set.contains(online.getUniqueId())) {
            return online.getUniqueId();
        }
        for (UUID uuid : set) {
            OfflinePlayer offline = Bukkit.getOfflinePlayer(uuid);
            if (name.equalsIgnoreCase(offline.getName())) {
                return uuid;
            }
        }
        return null;
    }

    private String applyReplacements(String text, String... replacements) {
        String result = text;
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            result = result.replace(replacements[i], replacements[i + 1]);
        }
        return result;
    }
}
