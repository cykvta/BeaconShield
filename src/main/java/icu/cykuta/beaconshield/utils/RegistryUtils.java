package icu.cykuta.beaconshield.utils;

import icu.cykuta.beaconshield.BeaconShield;
import icu.cykuta.beaconshield.data.UpgradeHandler;
import icu.cykuta.beaconshield.listeners.*;
import icu.cykuta.beaconshield.listeners.bukkit.BukkitEventListener;
import icu.cykuta.beaconshield.upgrade.*;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class RegistryUtils {
    /**
     * Get the CommandMap of the server
     * @return CommandMap
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
     * Register a command to the server
     * @param command Command
     */
    public static void registerCommand(Command command) {
        CommandMap commandMap = BeaconShield.getPlugin().getCommandMap();
        commandMap.register("beaconshield", command);
    }

    /**
     * Register an event listener to the server
     * @param listener Listener
     */
    private static void registerEvent(Listener listener) {
        PluginManager pm = BeaconShield.getPlugin().getServer().getPluginManager();
        pm.registerEvents(listener, BeaconShield.getPlugin());
    }

    /**
     * Register all upgrades
     * @see Upgrade for more information
     */
    public static void registerUpgrades() {
        List<Upgrade> upgrades = List.of(
                new DisableFallDamageUpgrade(),
                new DisablePvPUpgrade(),
                new DisableMobSpawningUpgrade(),
                new DisableDrowningUpgrade()
        );

        upgrades.forEach(UpgradeHandler::addUpgrade);
    }

    /**
     * Register all events
     */
    public static void registerEvents() {
        List<Listener> listeners = List.of(
                new BukkitEventListener(),
                new PlaceBeaconListener(),
                new BreakBeaconListener(),
                new InteractBeaconListener(),
                new GUIInteractListener(),
                new ProtectionInteractListener(),
                new GatewayEventListener()
        );

        listeners.forEach(RegistryUtils::registerEvent);
    }
}
