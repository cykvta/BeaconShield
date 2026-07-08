package icu.cykuta.beaconshield.utils;

import icu.cykuta.beaconshield.BeaconShield;
import icu.cykuta.beaconshield.beacon.BeaconShieldBlock;
import icu.cykuta.beaconshield.commands.CommandBeaconshield;
import icu.cykuta.beaconshield.data.UpgradeHandler;
import icu.cykuta.beaconshield.listeners.*;
import icu.cykuta.beaconshield.upgrade.*;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.PluginManager;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class RegistryUtils {

    /**
     * Get the CommandMap of the server.
     */
    public static CommandMap getCommandMap() {
        try {
            return (CommandMap) Bukkit.getServer().getClass().getDeclaredMethod("getCommandMap")
                    .invoke(Bukkit.getServer());
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Register a command to the server.
     */
    private static void registerCommand(Command command) {
        getCommandMap().register("beaconshield", command);
    }

    /**
     * Register an event listener to the server.
     */
    private static void registerEvent(Listener listener) {
        PluginManager pm = BeaconShield.getPlugin().getServer().getPluginManager();
        pm.registerEvents(listener, BeaconShield.getPlugin());
    }

    /**
     * Register an upgrade: its listener, its item and its recipe (if any).
     */
    public static void addUpgrade(Upgrade<?> upgrade) {
        Bukkit.getPluginManager().registerEvents(upgrade, BeaconShield.getPlugin());
        UpgradeHandler.put(upgrade, upgrade.getItemStack());
        addRecipe(upgrade.getRecipe());
    }

    /**
     * Register all upgrades.
     */
    public static void registerUpgrades() {
        List<Upgrade<?>> upgrades = List.of(
                new DisableFallDamageUpgrade(),
                new DisablePvPUpgrade(),
                new DisableMobSpawningUpgrade(),
                new DisableDrowningUpgrade()
        );

        upgrades.forEach(RegistryUtils::addUpgrade);
    }

    /**
     * Register all event listeners.
     */
    public static void registerEvents() {
        List<Listener> listeners = List.of(
                new BeaconBlockListener(),
                new GUIListener(),
                new ProtectionInteractListener(),
                new ProtectionGriefListener(),
                new ChunkGatewayListener()
        );

        listeners.forEach(RegistryUtils::registerEvent);
    }

    /**
     * Register all commands.
     */
    public static void registerCommands() {
        List<Command> commands = List.of(
                new CommandBeaconshield()
        );

        commands.forEach(RegistryUtils::registerCommand);
    }

    /**
     * Register the crafting recipes.
     */
    public static void registerRecipes() {
        ShapedRecipe recipe = BeaconShieldBlock.createRecipe();
        addRecipe(recipe);
    }

    /**
     * Register a recipe, ignoring nulls (disabled recipes).
     */
    private static void addRecipe(Recipe recipe) {
        if (recipe != null) {
            Bukkit.addRecipe(recipe);
        }
    }
}
