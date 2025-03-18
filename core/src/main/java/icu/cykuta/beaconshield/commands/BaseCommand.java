package icu.cykuta.beaconshield.commands;

import icu.cykuta.beaconshield.BeaconShield;
import icu.cykuta.beaconshield.utils.Chat;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseCommand extends Command {

    public enum CommandMode {
        PLAYER_ONLY,
        CONSOLE_ONLY,
        BOTH
    }

    private final String command;
    private final CommandMode commandMode;
    private final String permission;
    private final Map<String, BaseCommand> subcommands = new HashMap<>();
    protected final BeaconShield plugin;
    private final List<String> extraTabCompletions = new ArrayList<>();

    public BaseCommand(String command) {
        this(command, null, CommandMode.BOTH);
    }

    public BaseCommand(String command, CommandMode mode) {
        this(command, null, mode);
    }

    public BaseCommand(String command, String permission) {
        this(command, permission, CommandMode.BOTH);
    }

    public BaseCommand(String command, String permission, CommandMode mode) {
        super(command);
        this.command = command;
        this.plugin = BeaconShield.getPlugin();
        this.permission = permission;
        this.commandMode = mode;
    }

    protected abstract boolean onCommand(CommandSender sender, String[] args);

    /**
     * Method to execute the command.
     */
    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (!isAllowedSender(sender)) {
            Chat.send(sender, "invalid-sender");
            return false;
        }

        if (permission != null && !sender.hasPermission(permission)) {
            Chat.send(sender, "no-permission");
            return false;
        }

        if (args.length > 0 && !subcommands.isEmpty()) {
            String subcommandName = args[0].toLowerCase();
            BaseCommand subcommand = subcommands.get(subcommandName);

            if (subcommand != null) {
                return subcommand.execute(sender, commandLabel, shiftArgs(args));
            }
        }

        return onCommand(sender, args);
    }

    /**
     * Method to tab complete the command.
     */
    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
        BaseCommand currentCommand = this;
        for (int i = 0; i < args.length - 1; i++) {
            BaseCommand nextCommand = currentCommand.subcommands.get(args[i].toLowerCase());
            if (nextCommand == null) {
                return super.tabComplete(sender, alias, args);
            }
            currentCommand = nextCommand;
        }

        List<String> completions = new ArrayList<>();
        for (String subcommand : currentCommand.subcommands.keySet()) {
            if (subcommand.startsWith(args[args.length - 1].toLowerCase())) {
                completions.add(subcommand);
            }
        }

        for(String extraTabCompletion : currentCommand.extraTabCompletions) {
            if(extraTabCompletion.startsWith(args[args.length - 1].toLowerCase())) {
                completions.add(extraTabCompletion);
            }
        }

        return completions.isEmpty() ? super.tabComplete(sender, alias, args) : completions;
    }

    /**
     * Method to add a tab completion to the command.
     */
    public void addTabCompletion(String completion) {
        extraTabCompletions.add(completion);
    }

    /**
     * Method to add a subcommand to the command.
     */
    public void addSubcommand(BaseCommand subcommand) {
        subcommands.put(subcommand.getCommand(), subcommand);
    }

    /**
     * Check if the sender is allowed to execute the command.
     */
    private boolean isAllowedSender(CommandSender sender) {
        return switch (commandMode) {
            case PLAYER_ONLY -> sender instanceof Player;
            case CONSOLE_ONLY -> !(sender instanceof Player);
            case BOTH -> true;
        };
    }

    /**
     * Remove the first element of the array.
     */
    private String[] shiftArgs(String[] args) {
        if (args.length <= 1) return new String[0];
        String[] newArgs = new String[args.length - 1];
        System.arraycopy(args, 1, newArgs, 0, newArgs.length);
        return newArgs;
    }

    /**
     * Get the command name.
     */
    public String getCommand() {
        return command;
    }
}
