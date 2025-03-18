package icu.cykuta.beaconshield.providers;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

public abstract class Hook<T> {
    protected boolean enabled;
    protected T hook;
    protected HookType type;
    protected String name;
    protected PluginManager pluginManager;

    public enum HookType {
        DEPENDENCY,
        SOFT_DEPENDENCY
    }

    public Hook(String name, HookType type) {
        this.name = name;
        this.type = type;
        this.enabled = false;
        this.hook = null;
        this.pluginManager = Bukkit.getPluginManager();
    }

    public boolean isEnabled() {
        return this.enabled && this.hook != null;
    }

    public String getName() {
        return this.name;
    }

    public T getHook() {
        return this.hook;
    }

    /**
     * This method is used to set the hook object and enable the hook
     */
    protected void setHook(Object objHook) throws DependencyNotEnabledException {
        if (objHook == null) {
            this.enabled = false;
            this.hook = null;
            if (this.type == HookType.DEPENDENCY) {
                throw new DependencyNotEnabledException(this);
            }
        } else {
            this.hook = (T) objHook;
            this.enabled = true;
        }
    }

    public abstract void register() throws DependencyNotEnabledException;
}
