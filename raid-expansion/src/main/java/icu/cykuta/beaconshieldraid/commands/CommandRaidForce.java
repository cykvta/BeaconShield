package icu.cykuta.beaconshieldraid.commands;

import icu.cykuta.beaconshield.beacon.BeaconShieldBlock;
import icu.cykuta.beaconshieldraid.BeaconShieldRaidExpansion;
import icu.cykuta.beaconshieldraid.raid.RaidManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * {@code /bsraid force} — instantly breach the protection the admin stands
 * in (capture every chunk and expose the nexus).
 */
public class CommandRaidForce extends RaidSubCommand {

    public CommandRaidForce(BeaconShieldRaidExpansion plugin, RaidManager manager) {
        super("force", "beaconshield.raid.admin", CommandMode.PLAYER_ONLY, plugin, manager);
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        BeaconShieldBlock beacon = protectionAt(player);
        if (beacon == null) {
            reply(sender, "&cYou are not standing in a protection.");
            return false;
        }

        int captured = manager.forceBreach(beacon);
        raidPlugin.saveRaids();
        reply(sender, "&aForced raid on &6" + manager.ownerName(beacon)
                + "&a's protection (" + captured + " chunk(s) captured; nexus exposed).");
        if (!manager.isRaidingEnabled()) {
            reply(sender, "&7Note: raiding is OFF, so the nexus can't be broken yet. Use /bsraid on.");
        }
        return true;
    }
}
