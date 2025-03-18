package icu.cykuta.beaconshield.commands;

import icu.cykuta.beaconshield.data.UpgradeHandler;
import icu.cykuta.beaconshield.upgrade.Upgrade;
import icu.cykuta.beaconshield.utils.Chat;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandUpgrade extends BaseCommand {

    protected CommandUpgrade() {
        super("upgrade", "beaconshield.upgrade", CommandMode.BOTH);

        // Add completion for all upgrades
        for(Upgrade upgrade : UpgradeHandler.getUpgrades()) {
            this.addTabCompletion(upgrade.getName());
        }
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        if(args.length == 0) {
            Chat.send(sender, "missing-argument");
            return false;
        }

        Upgrade upgrade = UpgradeHandler.getUpgrade(args[0]);
        if(upgrade == null) {
            Chat.send(sender, "invalid-upgrade", args[0]);
            return false;
        }

        Player target = getTarget(sender, args);

        if (target == null) {
            Chat.send(sender, "invalid-target");
            return false;
        }

        // give the player the upgrade item
        target.getInventory().addItem(upgrade.getItemStack());
        return true;
    }

    /**
     * Get the target player from the command arguments, if not specified, return the sender,
     * but if the sender is not a player, return null.
     *
     * @param sender The command sender
     * @param args The command arguments
     * @return The target player
     */
    private Player getTarget(CommandSender sender, String[] args) {
        if (args.length > 1) {
            return plugin.getServer().getPlayer(args[1]);
        }

        if (sender instanceof Player) {
            return (Player) sender;
        }

        return null;
    }
}
