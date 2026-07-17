package icu.cykuta.beaconshieldraid.commands;

import icu.cykuta.beaconshield.beacon.BeaconShieldBlock;
import icu.cykuta.beaconshieldraid.BeaconShieldRaidExpansion;
import icu.cykuta.beaconshieldraid.raid.Raid;
import icu.cykuta.beaconshieldraid.raid.RaidManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * {@code /bsraid status} — global raiding state, or full detail of the
 * raid on the protection the player stands in.
 */
public class CommandRaidStatus extends RaidSubCommand {

    public CommandRaidStatus(BeaconShieldRaidExpansion plugin, RaidManager manager) {
        super("status", null, CommandMode.BOTH, plugin, manager);
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        // Detailed view when standing in a raided protection.
        if (sender instanceof Player player) {
            BeaconShieldBlock beacon = protectionAt(player);
            Raid raid = beacon == null ? null : manager.getRaid(beacon);
            if (raid != null) {
                showRaidDetail(sender, beacon, raid);
                return true;
            }
        }

        reply(sender, "&eRaiding: " + (manager.isRaidingEnabled() ? "&aON" : "&cOFF")
                + (raidPlugin.getSchedule().isEnabled() ? " &7(schedule active)" : ""));
        reply(sender, "&7Active raids: &f" + manager.getActiveRaids().size());
        return true;
    }

    private void showRaidDetail(CommandSender sender, BeaconShieldBlock beacon, Raid raid) {
        int total = manager.getCapturableChunks(beacon).size();
        long now = System.currentTimeMillis();

        reply(sender, "&c&lRaid on &6" + manager.ownerName(beacon) + "&c&l's protection");

        if (raid.isInFreeze()) {
            long left = Math.max(0, (raid.getFreezeEndsAt() - now) / 1000);
            reply(sender, "&ePhase: &6FREEZE &7(" + left + "s to join)");
        } else if (manager.isNexusVulnerable(beacon, raid)) {
            int max = manager.getConfig().getNexusHealth();
            int hp = raid.getNexusHealth() < 0 ? max : raid.getNexusHealth();
            reply(sender, "&ePhase: &cNEXUS &7(" + hp + "/" + max + " HP)");
        } else {
            reply(sender, "&ePhase: &6ACTIVE");
        }

        reply(sender, "&eChunks captured: &f" + raid.getCapturedCount() + "/" + total);
        reply(sender, "&eStarted: &f" + formatElapsed((now - raid.getStartedAt()) / 1000) + " ago");

        String leader = raid.getInitiator() == null ? "?" : nameOf(raid.getInitiator());
        reply(sender, "&eLeader: &f" + leader);
        reply(sender, "&eRaiders (" + raid.getParty().size() + "): &f" + partyNames(raid));
    }

    private String partyNames(Raid raid) {
        List<String> names = new ArrayList<>();
        for (UUID uuid : raid.getParty()) {
            names.add(nameOf(uuid));
        }
        return names.isEmpty() ? "-" : names.stream().collect(Collectors.joining(", "));
    }

    private String nameOf(UUID uuid) {
        String name = Bukkit.getOfflinePlayer(uuid).getName();
        return name == null ? uuid.toString().substring(0, 8) : name;
    }

    private String formatElapsed(long seconds) {
        long minutes = seconds / 60;
        long secs = seconds % 60;
        return minutes > 0 ? minutes + "m " + secs + "s" : secs + "s";
    }
}
