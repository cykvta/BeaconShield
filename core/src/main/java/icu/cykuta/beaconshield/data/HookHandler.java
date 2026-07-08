package icu.cykuta.beaconshield.data;

import com.sk89q.worldguard.WorldGuard;
import icu.cykuta.beaconshield.providers.DependencyNotEnabledException;
import icu.cykuta.beaconshield.providers.Hook;
import icu.cykuta.beaconshield.providers.hooks.VaultHook;
import icu.cykuta.beaconshield.providers.hooks.WorldGuardHook;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;

public class HookHandler {
    private static HookHandler instance;

    public final Hook<Economy> economyHook;
    public final Hook<WorldGuard> worldGuardHook;

    private HookHandler() {
        this.economyHook = registerHook(new VaultHook());
        this.worldGuardHook = registerHook(new WorldGuardHook());
    }

    private static <T extends Hook<?>> T registerHook(T hook) {
        try {
            hook.register();
            if (hook.isEnabled()) {
                Bukkit.getLogger().info("[BeaconShield] Hooked into " + hook.getName());
            }
        } catch (DependencyNotEnabledException e) {
            e.printStackTrace();
        }

        return hook;
    }

    public static HookHandler getInstance() {
        if (instance == null) {
            instance = new HookHandler();
        }
        return instance;
    }
}
