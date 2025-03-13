package icu.cykuta.beaconshield.providers.hooks;

import com.sk89q.worldguard.WorldGuard;
import icu.cykuta.beaconshield.providers.DependencyNotEnabledException;
import icu.cykuta.beaconshield.providers.Hook;

public class WorldGuardHook extends Hook<WorldGuard> {

    public WorldGuardHook() {
        super("WorldGuard", HookType.SOFT_DEPENDENCY);
    }

    @Override
    public void register() throws DependencyNotEnabledException {
        if (this.pluginManager.getPlugin("WorldGuard") != null) {
            this.hook = WorldGuard.getInstance();
        }
    }
}