package icu.cykuta.beaconshield.commands;

import icu.cykuta.beaconshield.utils.Chat;
import org.bukkit.command.CommandSender;

public class CommandReload extends BaseCommand {
    public CommandReload() {
        super("reload", "beaconshield.reload", CommandMode.BOTH);
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        plugin.getFileHandler().reload();
        Chat.send(sender, "reload-config");
        return true;
    }
}
