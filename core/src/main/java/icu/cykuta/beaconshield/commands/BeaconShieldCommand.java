package icu.cykuta.beaconshield.commands;

import icu.cykuta.api.command.BaseCommand;
import icu.cykuta.beaconshield.BeaconShield;
import icu.cykuta.beaconshield.utils.Chat;
import org.bukkit.command.CommandSender;

/**
 * Base for every BeaconShield command. The command framework itself
 * (sender modes, permissions, sub-command dispatch and tab completion) comes
 * from {@link BaseCommand}; this class only binds it to the plugin and routes
 * its two replies through {@code lang.yml}.
 */
public abstract class BeaconShieldCommand extends BaseCommand {

    protected BeaconShieldCommand(String command, String permission, CommandMode mode) {
        super(BeaconShield.getPlugin(), command, permission, mode);
    }

    @Override
    protected void onNoPermission(CommandSender sender) {
        Chat.send(sender, "no-permission");
    }

    @Override
    protected void onInvalidSender(CommandSender sender) {
        Chat.send(sender, "invalid-sender");
    }
}
