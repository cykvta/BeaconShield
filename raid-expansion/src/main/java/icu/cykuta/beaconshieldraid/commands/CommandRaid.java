package icu.cykuta.beaconshieldraid.commands;

import icu.cykuta.beaconshieldraid.BeaconShieldRaidExpansion;
import icu.cykuta.beaconshieldraid.raid.RaidManager;
import org.bukkit.command.CommandSender;

/**
 * Root {@code /bsraid} command. Registers every sub-command and prints the
 * help list when used on its own. Reuses the core command framework
 * ({@link RaidSubCommand} → {@code BaseCommand}).
 */
public class CommandRaid extends RaidSubCommand {

    public CommandRaid(BeaconShieldRaidExpansion plugin, RaidManager manager) {
        super("bsraid", null, CommandMode.BOTH, plugin, manager);
        registerSubcommands();
    }

    private void registerSubcommands() {
        addSubcommand(new CommandRaidStart(raidPlugin, manager));
        addSubcommand(new CommandRaidRequest(raidPlugin, manager));
        addSubcommand(new CommandRaidAccept(raidPlugin, manager));
        addSubcommand(new CommandRaidReject(raidPlugin, manager));
        addSubcommand(new CommandRaidInvite(raidPlugin, manager));
        addSubcommand(new CommandRaidKick(raidPlugin, manager));
        addSubcommand(new CommandRaidLeave(raidPlugin, manager));
        addSubcommand(new CommandRaidCancel(raidPlugin, manager));
        addSubcommand(new CommandRaidStatus(raidPlugin, manager));
        addSubcommand(new CommandRaidToggle(raidPlugin, manager));
        addSubcommand(new CommandRaidReload(raidPlugin, manager));
        addSubcommand(new CommandRaidForce(raidPlugin, manager));
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        reply(sender, "&c&lBeaconShield Raids");
        reply(sender, "&f/bsraid start &7- Start a raid where you stand");
        reply(sender, "&f/bsraid request [raid] &7- Ask to join a raid (the one where you stand, if not given)");
        reply(sender, "&f/bsraid accept|reject <player> &7- Leader: answer a join request");
        reply(sender, "&f/bsraid invite|kick <player> &7- Leader: add/remove a party member");
        reply(sender, "&f/bsraid leave &7- Leave the raid (leader leaving cancels it)");
        reply(sender, "&f/bsraid cancel &7- Leader/admin: cancel the raid");
        reply(sender, "&f/bsraid status &7- Show the raiding state");
        if (sender.hasPermission("beaconshield.raid.admin")) {
            reply(sender, "&f/bsraid toggle [on|off] &7- Enable/disable raiding");
            reply(sender, "&f/bsraid force &7- Force the raid where you stand");
            reply(sender, "&f/bsraid reload &7- Reload the config");
        }
        return true;
    }
}
