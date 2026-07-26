package icu.cykuta.beaconshield.update;

import icu.cykuta.api.update.UpdateChecker;
import icu.cykuta.beaconshield.BeaconShield;
import icu.cykuta.beaconshield.config.ConfigHandler;
import icu.cykuta.beaconshield.utils.Chat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Compares the running version against the latest GitHub release of
 * BeaconShield and reports it: once in the console, and then to every player
 * with {@code beaconshield.update-notify} when they join.
 *
 * <p>The check itself lives in the API's {@link UpdateChecker} (asynchronous
 * request, callback back on the main thread); this class only decides what to
 * do with the outcome. It can be turned off with {@code update-check-enabled}
 * in config.yml.</p>
 */
public class UpdateNotifier implements Listener {

    /** The repository releases are read from. */
    private static final String REPOSITORY = "cykvta/BeaconShield";

    /** Who is told about a new version when joining. */
    private static final String NOTIFY_PERMISSION = "beaconshield.update-notify";

    private final BeaconShield plugin;
    private UpdateChecker.Result result;

    public UpdateNotifier(BeaconShield plugin) {
        this.plugin = plugin;
    }

    /**
     * Run the check. Does nothing when disabled in config.yml.
     */
    public void start() {
        boolean enabled = ConfigHandler.getInstance().getConfig().getBoolean("update-check-enabled", true);
        new UpdateChecker(plugin, REPOSITORY, enabled).check(this::onResult);
    }

    /**
     * Handle the outcome: keep it and start notifying only when outdated, so
     * an up-to-date server pays for no listener at all.
     */
    private void onResult(UpdateChecker.Result result) {
        if (!result.isOutdated()) {
            return;
        }

        this.result = result;
        plugin.getLogger().warning("A new version is available: " + result.latestVersion()
                + " (running " + result.currentVersion() + ")");
        plugin.getLogger().warning("Download it at " + result.downloadUrl());

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (result == null || !player.hasPermission(NOTIFY_PERMISSION)) {
            return;
        }

        Chat.send(player, "update-available",
                result.latestVersion(), result.currentVersion(), result.downloadUrl());
    }
}
