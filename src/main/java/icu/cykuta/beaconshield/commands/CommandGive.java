package icu.cykuta.beaconshield.commands;

import icu.cykuta.beaconshield.beacon.BeaconShieldBlock;
import icu.cykuta.beaconshield.utils.Chat;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandGive extends BaseCommand {
    public CommandGive() {
        super("give", "beaconshield.give", CommandMode.BOTH);
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player target = getTarget(sender, args);

        if (target == null) {
            Chat.send(sender, "invalid-target");
            return false;
        }

        if (target.getInventory().firstEmpty() == -1) {
            Chat.send(sender, "target-inventory-full");
            return false;
        }

        target.getInventory().addItem(BeaconShieldBlock.createBeaconItem());
        Chat.send(sender, "give-success", target.getName());
        Chat.send(target, "receive-success", sender.getName());
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
        if (args.length > 0) {
            return plugin.getServer().getPlayer(args[0]);
        }

        if (sender instanceof Player) {
            return (Player) sender;
        }

        return null;
    }
}
