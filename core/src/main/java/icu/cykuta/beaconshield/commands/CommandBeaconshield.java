package icu.cykuta.beaconshield.commands;

import icu.cykuta.beaconshield.utils.Chat;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class CommandBeaconshield extends BaseCommand {

    public CommandBeaconshield() {
        super("BeaconShield", "beaconshield.command", CommandMode.BOTH);
        this.registerAliases();
        this.registerSubcommands();
    }

    private void registerAliases() {
        List<String> aliases = new ArrayList<>();
        aliases.add("bsd");
        this.setAliases(aliases);
    }

    private void registerSubcommands() {
        this.addSubcommand(new CommandReload());
        this.addSubcommand(new CommandGive());
        this.addSubcommand(new CommandUpgrade());
        this.addSubcommand(new CommandList());
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Chat.sendRaw(sender, "BeaconShield by Cykuta");
        Chat.sendRaw(sender, "Version: &b" + plugin.getDescription().getVersion());
        Chat.sendRaw(sender, "---------------------------------");
        Chat.sendRaw(sender, "Subcommands:");
        Chat.sendRaw(sender, "  /bsd &7- &fShow this help");
        Chat.sendRaw(sender, "  /bsd reload &7- &fReload the config");
        Chat.sendRaw(sender, "  /bsd give <player> &7- &fGive a BeaconShield to a player");
        Chat.sendRaw(sender, "  /bsd upgrade <upgrade> &7- &fGive an upgrade item to a player");
        Chat.sendRaw(sender, "  /bsd list <page> &7- &fList all beacons");
        Chat.sendRaw(sender, "---------------------------------");
        return true;
    }
}
