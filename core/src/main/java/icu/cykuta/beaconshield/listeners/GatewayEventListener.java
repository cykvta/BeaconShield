package icu.cykuta.beaconshield.listeners;

import icu.cykuta.beaconshield.beacon.BeaconShieldBlock;
import icu.cykuta.beaconshield.config.ConfigHandler;
import icu.cykuta.beaconshield.events.internal.InternalPlayerProtectedChunkGatewayEvent;
import icu.cykuta.beaconshield.utils.Chat;
import icu.cykuta.beaconshield.config.PluginConfiguration;
import icu.cykuta.beaconshield.utils.Text;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class GatewayEventListener implements Listener {
    @EventHandler
    public void onPlayerProtectedChunkGatewayEvent(InternalPlayerProtectedChunkGatewayEvent event) {
        PluginConfiguration config = ConfigHandler.getInstance().getConfig();
        Player player = event.getPlayer();
        BeaconShieldBlock beacon = event.getBeacon();

        String chat;
        String title;
        String subtitle;

        if (event.getAction() == InternalPlayerProtectedChunkGatewayEvent.Action.ENTER) {
            chat = config.getString("greeting.message");
            title = config.getString("greeting.title");
            subtitle = config.getString("greeting.subtitle");
        } else {
            chat = config.getString("farewell.message");
            title = config.getString("farewell.title");
            subtitle = config.getString("farewell.subtitle");
        }

        if (!chat.isEmpty()) {
            chat = Text.replace(Text.color(chat), beacon.getOwner().getName());
            Chat.sendRaw(player, chat);
        }

        if (!title.isEmpty() || !subtitle.isEmpty()) {
            title = title.isEmpty() ? "" : Text.replace(Text.color(title), beacon.getOwner().getName());
            subtitle = subtitle.isEmpty() ? "" : Text.replace(Text.color(subtitle), beacon.getOwner().getName());
            player.sendTitle(title, subtitle, 10, 40, 10);
        }

    }
}
