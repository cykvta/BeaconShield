package icu.cykuta.beaconshield.commands;

import icu.cykuta.beaconshield.config.ConfigHandler;
import icu.cykuta.beaconshield.data.BeaconHandler;
import icu.cykuta.beaconshield.data.UpgradeHandler;
import icu.cykuta.beaconshield.utils.Chat;
import org.bukkit.command.CommandSender;

public class CommandReload extends BaseCommand {
    public CommandReload() {
        super("reload", "beaconshield.reload", CommandMode.BOTH);
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        // Reload every yml file from disk
        ConfigHandler.getInstance().reload();

        // Rebuild the upgrade items with the new upgrade.yml values
        UpgradeHandler.refreshItems();

        // Close and drop the cached menus so they are rebuilt with the
        // new gui.yml/lang.yml values
        BeaconHandler.getInstance().invalidateGUIs();

        Chat.send(sender, "reload-config");
        return true;
    }
}
