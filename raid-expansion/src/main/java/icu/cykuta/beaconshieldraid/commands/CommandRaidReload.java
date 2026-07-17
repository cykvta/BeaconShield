package icu.cykuta.beaconshieldraid.commands;

import icu.cykuta.beaconshieldraid.BeaconShieldRaidExpansion;
import icu.cykuta.beaconshieldraid.raid.RaidManager;
import org.bukkit.command.CommandSender;

/**
 * {@code /bsraid reload} — reload the raid expansion configuration.
 */
public class CommandRaidReload extends RaidSubCommand {

    public CommandRaidReload(BeaconShieldRaidExpansion plugin, RaidManager manager) {
        super("reload", "beaconshield.raid.admin", CommandMode.BOTH, plugin, manager);
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        raidPlugin.reloadPluginConfig();
        reply(sender, "&aRaid expansion configuration reloaded.");
        return true;
    }
}
