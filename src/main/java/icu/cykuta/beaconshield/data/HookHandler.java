package icu.cykuta.beaconshield.data;
import com.sk89q.worldguard.WorldGuard;
import icu.cykuta.beaconshield.providers.DependencyNotEnabledException;
import icu.cykuta.beaconshield.providers.Hook;
import icu.cykuta.beaconshield.providers.hooks.VaultHook;
import icu.cykuta.beaconshield.providers.hooks.WorldGuardHook;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;

public class HookHandler {
    public Hook<Economy> economyHook;
    public Hook<WorldGuard> worldGuardHookHook;

    public void registerHooks() {
        this.economyHook = this.registerHook(new VaultHook());
        this.worldGuardHookHook = this.registerHook(new WorldGuardHook());
    }

    private Hook registerHook(Hook hook) {
        try {
            hook.register();
            if (hook.isEnabled()) {
                Bukkit.getLogger().info("[OnPressL] Hooked into " + hook.getName());
            }

        } catch (DependencyNotEnabledException e) {
            e.printStackTrace();
        }

        return hook;
    }
}
