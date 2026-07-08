package icu.cykuta.beaconshield.listeners;

import icu.cykuta.beaconshield.beacon.BeaconShieldBlock;
import icu.cykuta.beaconshield.config.ConfigHandler;
import icu.cykuta.beaconshield.config.PluginConfiguration;
import icu.cykuta.beaconshield.data.ProtectionHandler;
import icu.cykuta.beaconshield.utils.Chat;
import icu.cykuta.beaconshield.utils.Text;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Sends the greeting/farewell messages when a player enters or
 * leaves a protected territory.
 */
public class ChunkGatewayListener implements Listener {

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();

        if (to == null || to.getWorld() == null || from.getWorld() == null) {
            return;
        }

        // Cheap chunk-coordinate comparison, no chunk is loaded
        int fromChunkX = from.getBlockX() >> 4;
        int fromChunkZ = from.getBlockZ() >> 4;
        int toChunkX = to.getBlockX() >> 4;
        int toChunkZ = to.getBlockZ() >> 4;
        boolean sameWorld = from.getWorld().equals(to.getWorld());

        if (sameWorld && fromChunkX == toChunkX && fromChunkZ == toChunkZ) {
            return;
        }

        BeaconShieldBlock fromBeacon = ProtectionHandler.getBeacon(from.getWorld(), fromChunkX, fromChunkZ);
        BeaconShieldBlock toBeacon = ProtectionHandler.getBeacon(to.getWorld(), toChunkX, toChunkZ);

        if (fromBeacon == toBeacon) {
            return;
        }

        if (fromBeacon != null) {
            this.sendMessages(event.getPlayer(), fromBeacon, "farewell");
        }
        if (toBeacon != null) {
            this.sendMessages(event.getPlayer(), toBeacon, "greeting");
        }
    }

    /**
     * Send the configured chat message and title of the given section
     * ("greeting" or "farewell").
     */
    private void sendMessages(Player player, BeaconShieldBlock beacon, String section) {
        PluginConfiguration config = ConfigHandler.getInstance().getConfig();
        String ownerName = beacon.getOwner().getName();

        String chat = config.getString(section + ".message");
        String title = config.getString(section + ".title");
        String subtitle = config.getString(section + ".subtitle");

        if (!chat.isEmpty()) {
            Chat.sendRaw(player, Text.replace(chat, ownerName));
        }

        if (!title.isEmpty() || !subtitle.isEmpty()) {
            player.sendTitle(
                    Text.color(Text.replace(title, ownerName)),
                    Text.color(Text.replace(subtitle, ownerName)),
                    10, 40, 10);
        }
    }
}
