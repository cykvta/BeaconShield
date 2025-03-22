package icu.cykuta.beaconshield.commands;

import icu.cykuta.beaconshield.beacon.BeaconShieldBlock;
import icu.cykuta.beaconshield.data.BeaconHandler;
import icu.cykuta.beaconshield.utils.Chat;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import java.util.List;

public class CommandList extends BaseCommand {
    public CommandList() {
        super("list", "beaconshield.list", CommandMode.BOTH);
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        List<BeaconShieldBlock> beacons = BeaconHandler.getInstance().getBeacons();
        int page;
        int itemsPerPage = 5;
        int maxPage = (int) Math.ceil((double) beacons.size() / itemsPerPage);

        if (beacons.isEmpty()) {
            Chat.send(sender, "beacon-list-empty");
            return false;
        }

        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                Chat.send(sender, "invalid-page");
                return false;
            }
        } else {
            page = 1;
        }

        if (page < 1 || page > maxPage) {
            Chat.send(sender, "invalid-page");
            return false;
        }

        Chat.send(sender, "beacon-list-header");
        for (int i = (page - 1) * itemsPerPage; i < Math.min(page * itemsPerPage, beacons.size()); i++) {
            BeaconShieldBlock beacon = beacons.get(i);

            // Variables
            String id = beacon.getId();
            String owner = beacon.getOwner().getName();
            Location location = beacon.getBlock().getLocation();
            String x = String.valueOf((int) location.getX());
            String y = String.valueOf((int) location.getY());
            String z = String.valueOf((int) location.getZ());
            String world = location.getWorld().getName();

            // Send message
            Chat.send(sender, "beacon-list-item", x, y, z, world, owner, id);
        }
        Chat.send(sender, "beacon-list-footer", String.valueOf(page), String.valueOf(maxPage));

        return false;
    }
}
