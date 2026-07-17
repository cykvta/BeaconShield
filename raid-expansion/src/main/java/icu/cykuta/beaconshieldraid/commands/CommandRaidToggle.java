package icu.cykuta.beaconshieldraid.commands;

import icu.cykuta.beaconshieldraid.BeaconShieldRaidExpansion;
import icu.cykuta.beaconshieldraid.raid.RaidManager;
import org.bukkit.command.CommandSender;

/**
 * {@code /bsraid toggle [on|off]} — enable/disable raiding globally.
 * Without an argument it flips the current state.
 */
public class CommandRaidToggle extends RaidSubCommand {

    public CommandRaidToggle(BeaconShieldRaidExpansion plugin, RaidManager manager) {
        super("toggle", "beaconshield.raid.admin", CommandMode.BOTH, plugin, manager);
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        boolean target;
        if (args.length == 0) {
            target = !manager.isRaidingEnabled();
        } else if (args[0].equalsIgnoreCase("on")) {
            target = true;
        } else if (args[0].equalsIgnoreCase("off")) {
            target = false;
        } else {
            reply(sender, "&eUsage: /bsraid toggle [on|off]");
            return false;
        }

        manager.setRaidingEnabled(target);
        raidPlugin.saveRaids();
        reply(sender, target ? "&aRaiding enabled." : "&aRaiding disabled.");
        return true;
    }
}
