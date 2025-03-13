package icu.cykuta.beaconshield.providers.hooks;

import icu.cykuta.beaconshield.providers.DependencyNotEnabledException;
import icu.cykuta.beaconshield.providers.Hook;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultHook extends Hook<Economy> {

    public VaultHook() {
        super("Vault", HookType.SOFT_DEPENDENCY);
    }

    @Override
    public void register() throws DependencyNotEnabledException {
        if (this.pluginManager.getPlugin("Vault") != null) {
            RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp != null) {
                this.setHook(rsp.getProvider());
            }
        }
    }
}
