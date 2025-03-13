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
    public static CommandMap getCommandMap() {
        try {
            return (CommandMap) Bukkit.getServer().getClass().getDeclaredMethod("getCommandMap")
                    .invoke(Bukkit.getServer());
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static void registerCommand(Command command) {
        CommandMap commandMap = BeaconShield.getPlugin().getCommandMap();
        commandMap.register("beaconshield", command);
    }

    private static void registerEvent(Listener listener) {
        PluginManager pm = BeaconShield.getPlugin().getServer().getPluginManager();
        pm.registerEvents(listener, BeaconShield.getPlugin());
    }

    public static void registerUpgrades() {
        List<Upgrade> upgrades = List.of(
                new DisableFallDamageUpgrade(),
                new DisablePvPUpgrade(),
                new DisableMobSpawningUpgrade(),
                new DisableDrowningUpgrade()
        );

        upgrades.forEach(UpgradeHandler::addUpgrade);
    }

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
