package icu.cykuta.beaconshieldraid.commands;

import icu.cykuta.beaconshield.beacon.BeaconShieldBlock;
import icu.cykuta.beaconshieldraid.BeaconShieldRaidExpansion;
import icu.cykuta.beaconshieldraid.raid.RaidManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * {@code /bsraid start} — start a raid on the protection the player stands
 * in, paying the configured cost.
 */
public class CommandRaidStart extends RaidSubCommand {

    public CommandRaidStart(BeaconShieldRaidExpansion plugin, RaidManager manager) {
        super("start", "beaconshield.raid.start", CommandMode.PLAYER_ONLY, plugin, manager);
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        if (!manager.isRaidingEnabled()) {
            msg(sender, "raids-not-active", "&cRaids are not active right now.");
            return false;
        }

        BeaconShieldBlock beacon = protectionAt(player);
        if (beacon == null) {
            reply(sender, "&cYou are not standing in a protection.");
            return false;
        }
        if (beacon.hasMember(player)) {
            msg(sender, "cannot-raid-own", "&cYou can't raid a protection you belong to.");
            return false;
        }
        if (manager.getRaid(beacon) != null) {
            msg(sender, "already-raided", "&eThat protection is already being raided. Use /bsraid join.");
            return false;
        }
        if (!manager.hasEnoughDefendersOnline(beacon)) {
            msg(sender, "not-enough-defenders-online",
                    "&cNot enough of this protection's members are online to raid it (%online%/%required%).",
                    "%online%", String.valueOf(manager.countOnlineMembers(beacon)),
                    "%required%", String.valueOf(manager.requiredOnlineDefenders(beacon)));
            return false;
        }

        double cost = manager.computeStartCost(beacon);
        if (cost > 0 && !manager.getApi().hasBalance(player, cost)) {
            msg(sender, "not-enough-money", "&cYou need &6%cost%&c to start this raid.",
                    "%cost%", formatCost(cost));
            return false;
        }
        if (cost > 0) {
            manager.getApi().withdraw(player, cost);
        }

        manager.startRaid(beacon, player);
        raidPlugin.saveRaids();
        msg(sender, "raid-start-success",
                "&aRaid started (cost: &6%cost%&a). Capture every chunk, then break the nexus.",
                "%cost%", formatCost(cost));
        return true;
    }

    private String formatCost(double cost) {
        return cost == Math.floor(cost) ? String.valueOf((long) cost) : String.format("%.2f", cost);
    }
}
