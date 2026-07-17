package icu.cykuta.beaconshieldraid.raid;

import icu.cykuta.beaconshieldraid.config.RaidConfig;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Shows raid information through a configurable channel (action bar, boss
 * bar or throttled chat). Each viewer gets their own line, so raiders can
 * see per-chunk text ("Capturing 40%", "Captured!") while defenders see a
 * defender-oriented message.
 */
public class RaidDisplay {

    private enum Channel { ACTIONBAR, BOSSBAR, CHAT, NONE }

    /** One line of display for a single viewer. */
    public record Line(String text, double fraction) { }

    private final RaidConfig config;

    /** Boss bars keyed by "beaconId|playerUuid" (one per viewer). */
    private final Map<String, BossBar> bars = new HashMap<>();
    /** Last chat send per beacon, for the chat interval throttle. */
    private final Map<String, Long> lastChat = new HashMap<>();

    public RaidDisplay(RaidConfig config) {
        this.config = config;
    }

    /**
     * Show one line per viewer for a raid.
     */
    public void show(String beaconId, Map<Player, Line> lines) {
        switch (channel()) {
            case NONE -> { }
            case ACTIONBAR -> lines.forEach((player, line) ->
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(line.text())));
            case CHAT -> showChat(beaconId, lines);
            case BOSSBAR -> showBossBars(beaconId, lines);
        }
    }

    private void showChat(String beaconId, Map<Player, Line> lines) {
        long now = System.currentTimeMillis();
        if (now - lastChat.getOrDefault(beaconId, 0L) < config.getDisplayChatInterval() * 1000L) {
            return;
        }
        lastChat.put(beaconId, now);
        lines.forEach((player, line) -> player.sendMessage(line.text()));
    }

    private void showBossBars(String beaconId, Map<Player, Line> lines) {
        String prefix = beaconId + "|";

        // Drop bars for viewers no longer shown.
        Iterator<Map.Entry<String, BossBar>> it = bars.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, BossBar> entry = it.next();
            if (entry.getKey().startsWith(prefix)) {
                String uuid = entry.getKey().substring(prefix.length());
                boolean stillShown = lines.keySet().stream()
                        .anyMatch(player -> player.getUniqueId().toString().equals(uuid));
                if (!stillShown) {
                    entry.getValue().removeAll();
                    entry.getValue().setVisible(false);
                    it.remove();
                }
            }
        }

        // Create/update a single-viewer bar for each line.
        for (Map.Entry<Player, Line> entry : lines.entrySet()) {
            Player player = entry.getKey();
            Line line = entry.getValue();
            BossBar bar = bars.computeIfAbsent(prefix + player.getUniqueId(),
                    id -> Bukkit.createBossBar(line.text(), color(), style()));
            bar.setTitle(line.text());
            bar.setProgress(Math.max(0, Math.min(1, line.fraction())));
            if (!bar.getPlayers().contains(player)) {
                bar.removeAll();
                bar.addPlayer(player);
            }
            bar.setVisible(true);
        }
    }

    /**
     * Remove a raid's boss bars / chat throttle (call when the raid ends).
     */
    public void clear(String beaconId) {
        String prefix = beaconId + "|";
        bars.entrySet().removeIf(entry -> {
            if (entry.getKey().startsWith(prefix)) {
                entry.getValue().removeAll();
                entry.getValue().setVisible(false);
                return true;
            }
            return false;
        });
        lastChat.remove(beaconId);
    }

    /**
     * Remove every boss bar (call on plugin disable).
     */
    public void clearAll() {
        bars.values().forEach(bar -> {
            bar.removeAll();
            bar.setVisible(false);
        });
        bars.clear();
        lastChat.clear();
    }

    private Channel channel() {
        try {
            return Channel.valueOf(config.getDisplayType().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return Channel.ACTIONBAR;
        }
    }

    private BarColor color() {
        try {
            return BarColor.valueOf(config.getBossbarColor().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return BarColor.RED;
        }
    }

    private BarStyle style() {
        try {
            return BarStyle.valueOf(config.getBossbarStyle().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return BarStyle.SEGMENTED_10;
        }
    }
}
