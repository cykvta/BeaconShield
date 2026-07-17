package icu.cykuta.beaconshieldraid.raid;

import icu.cykuta.beaconshield.beacon.BeaconShieldBlock;
import icu.cykuta.beaconshield.beacon.protection.ProtectedChunk;
import icu.cykuta.beaconshieldraid.config.RaidConfig;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Runs once per second. Drives every active raid: fuel checks, the freeze
 * countdown, chunk capture (hot points), nexus regeneration and the
 * freeze/progress display. Raids only exist once started or forced, so
 * there is no global on/off gate here.
 */
public class RaidTickTask extends BukkitRunnable {

    /** Seconds of real time between runs (task period is 20 ticks). */
    private static final double SECONDS_PER_RUN = 1.0;

    private final RaidManager manager;

    public RaidTickTask(RaidManager manager) {
        this.manager = manager;
    }

    @Override
    public void run() {
        RaidConfig config = manager.getConfig();
        for (BeaconShieldBlock beacon : manager.getApi().getBeacons()) {
            tickBeacon(beacon, config);
        }
    }

    private void tickBeacon(BeaconShieldBlock beacon, RaidConfig config) {
        Raid raid = manager.getRaid(beacon);
        if (raid == null) {
            return;
        }

        // Out of fuel mid-raid: the raid ends and the beacon falls.
        if (!beacon.canProtect()) {
            manager.endRaidByFuel(beacon);
            return;
        }

        // Freeze window: show the countdown, nothing is captured yet.
        if (raid.isInFreeze()) {
            showFreeze(beacon, raid, config);
            return;
        }
        if (!raid.isBeganAnnounced()) {
            raid.setBeganAnnounced(true);
            raid.setLastRaiderSeen(System.currentTimeMillis());
            manager.announceRaidBegan(beacon);
        }

        World world = beacon.getWorld();
        if (world == null) {
            return;
        }

        List<ProtectedChunk> capturable = manager.getCapturableChunks(beacon);
        if (!capturable.isEmpty() && raid.getCapturedCount() < capturable.size()) {
            ChunkPresence presence = countPresence(beacon, raid, world);
            for (ProtectedChunk chunk : capturable) {
                if (!raid.isCaptured(chunk)) {
                    tickChunk(beacon, raid, chunk, presence, config);
                }
            }
        }

        regenNexus(raid, config);
        updateActiveDisplay(beacon, raid, capturable, world, config);
    }

    /**
     * Handle raider presence and the "defenders win" timer, then show the
     * directed display: the normal progress, or the emptied-protection
     * countdown when no raider is inside.
     */
    private void updateActiveDisplay(BeaconShieldBlock beacon, Raid raid,
                                     List<ProtectedChunk> capturable, World world, RaidConfig config) {
        boolean raidersPresent = manager.hasRaiderInProtection(beacon, raid, world);
        if (raidersPresent) {
            raid.setLastRaiderSeen(System.currentTimeMillis());
        }

        List<Player> raiders = manager.getOnlineRaiders(raid);
        List<Player> defenders = manager.getOnlineDefenders(beacon);

        if (config.getDefendersWinSeconds() > 0 && !raidersPresent) {
            long idleMs = System.currentTimeMillis() - raid.getLastRaiderSeen();
            long limitMs = config.getDefendersWinSeconds() * 1000L;
            if (idleMs >= limitMs) {
                manager.defendersWin(beacon);
                return;
            }
            long left = (long) Math.ceil((limitMs - idleMs) / 1000.0);
            showCountdown(beacon, raiders, defenders, left, config);
            return;
        }

        showProgress(beacon, raid, capturable, raiders, defenders, config);
    }

    // Capture ----------------------------------------------------------

    private void tickChunk(BeaconShieldBlock beacon, Raid raid, ProtectedChunk chunk,
                           ChunkPresence presence, RaidConfig config) {
        long key = key(chunk.getX(), chunk.getZ());
        int attackers = presence.attackers.getOrDefault(key, 0);
        int defenders = presence.defenders.getOrDefault(key, 0);

        boolean advancing = attackers >= config.getMinAttackers() && attackers > defenders;

        if (advancing) {
            double progress = raid.getProgress(chunk) + SECONDS_PER_RUN;
            if (progress >= config.getCaptureTimeSeconds()) {
                manager.captureChunk(beacon, raid, chunk);
            } else {
                raid.setProgress(chunk, progress);
            }
            return;
        }

        if (raid.getProgress(chunk) <= 0) {
            return;
        }
        switch (config.getContestedBehavior()) {
            case PAUSE -> { /* keep the accumulated progress */ }
            case RESET -> raid.setProgress(chunk, 0);
            case DECAY -> raid.setProgress(chunk, raid.getProgress(chunk) - config.getDecayPerSecond());
        }
    }

    private void regenNexus(Raid raid, RaidConfig config) {
        if (!raid.isNexusStarted() || config.getNexusRegenSeconds() <= 0) {
            return;
        }
        long elapsed = System.currentTimeMillis() - raid.getLastNexusDamage();
        if (elapsed >= config.getNexusRegenSeconds() * 1000L) {
            raid.regenNexus(config.getNexusHealth());
        }
    }

    // Display ----------------------------------------------------------

    private void showFreeze(BeaconShieldBlock beacon, Raid raid, RaidConfig config) {
        long secondsLeft = Math.max(0, (long) Math.ceil((raid.getFreezeEndsAt() - System.currentTimeMillis()) / 1000.0));
        double fraction = config.getFreezeSeconds() > 0 ? secondsLeft / (double) config.getFreezeSeconds() : 0;
        String seconds = String.valueOf(secondsLeft);

        String raiderText = config.display("freeze-display-raider",
                "&eRaid starts in &6%seconds%s &7— /bsraid request to join", "%seconds%", seconds);
        String defenderText = config.display("freeze-display-defender",
                "&cYour base is being raided! &6%seconds%s &cuntil it begins", "%seconds%", seconds);

        Map<Player, RaidDisplay.Line> lines = new HashMap<>();
        putGroup(lines, manager.getOnlineRaiders(raid), raiderText, fraction);
        putGroup(lines, manager.getOnlineDefenders(beacon), defenderText, fraction);
        manager.getDisplay().show(beacon.getId(), lines);
    }

    private void showProgress(BeaconShieldBlock beacon, Raid raid, List<ProtectedChunk> capturable,
                              List<Player> raiders, List<Player> defenders, RaidConfig config) {
        Map<Player, RaidDisplay.Line> lines = new HashMap<>();

        if (manager.isNexusVulnerable(beacon, raid)) {
            int max = config.getNexusHealth();
            int hp = raid.getNexusHealth() < 0 ? max : raid.getNexusHealth();
            double fraction = max > 0 ? hp / (double) max : 0;
            String health = String.valueOf(hp);
            String maxHealth = String.valueOf(max);
            putGroup(lines, raiders, config.display("nexus-display-raider",
                    "&cBreak the beacon! &6%health%&c/&6%max_health%",
                    "%health%", health, "%max_health%", maxHealth), fraction);
            putGroup(lines, defenders, config.display("nexus-display-defender",
                    "&aProtect the beacon! &6%health%&a/&6%max_health%",
                    "%health%", health, "%max_health%", maxHealth), fraction);
        } else {
            int captured = raid.getCapturedCount();
            int total = capturable.size();
            double overall = total > 0 ? captured / (double) total : 0;

            putGroup(lines, defenders, config.display("capture-display-defender",
                    "&cDefend! Chunks lost &6%captured%&c/&6%total%",
                    "%captured%", String.valueOf(captured), "%total%", String.valueOf(total)), overall);

            // Each raider sees the status of the chunk they stand on.
            for (Player raider : raiders) {
                lines.put(raider, raiderCaptureLine(raid, capturable, raider, captured, total, overall, config));
            }
        }
        manager.getDisplay().show(beacon.getId(), lines);
    }

    /**
     * Per-raider capture line: "Captured!" (green) when the chunk under the
     * raider is already taken, the chunk's capture percent while taking it,
     * or the overall progress when they are outside a capturable chunk.
     */
    private RaidDisplay.Line raiderCaptureLine(Raid raid, List<ProtectedChunk> capturable, Player raider,
                                               int captured, int total, double overall, RaidConfig config) {
        String cap = String.valueOf(captured);
        String tot = String.valueOf(total);
        ProtectedChunk here = new ProtectedChunk(raider.getLocation().getChunk());

        if (raid.isCaptured(here)) {
            return new RaidDisplay.Line(config.display("capture-display-captured",
                    "&a&lCaptured! &6%captured%&a/&6%total%", "%captured%", cap, "%total%", tot), 1.0);
        }

        int percent;
        double fraction;
        if (capturable.contains(here)) {
            fraction = raid.getProgress(here) / config.getCaptureTimeSeconds();
            percent = (int) Math.round(fraction * 100);
        } else {
            percent = currentCapturePercent(raid, capturable, config);
            fraction = overall;
        }
        return new RaidDisplay.Line(config.display("capture-display-raider",
                "&eCapturing &6%captured%&e/&6%total% &7(%percent%%)",
                "%captured%", cap, "%total%", tot, "%percent%", String.valueOf(percent)), fraction);
    }

    /**
     * Shown while the protection is empty of raiders and the defenders-win
     * timer is counting down.
     */
    private void showCountdown(BeaconShieldBlock beacon, List<Player> raiders, List<Player> defenders,
                               long secondsLeft, RaidConfig config) {
        double fraction = config.getDefendersWinSeconds() > 0
                ? secondsLeft / (double) config.getDefendersWinSeconds() : 0;
        String seconds = String.valueOf(secondsLeft);

        Map<Player, RaidDisplay.Line> lines = new HashMap<>();
        putGroup(lines, raiders, config.display("defenders-winning-raider",
                "&cNo raiders in the protection! Raid fails in &6%seconds%s", "%seconds%", seconds), fraction);
        putGroup(lines, defenders, config.display("defenders-winning-defender",
                "&aNo attackers! Raid fails in &6%seconds%s &a— hold the line", "%seconds%", seconds), fraction);
        manager.getDisplay().show(beacon.getId(), lines);
    }

    private void putGroup(Map<Player, RaidDisplay.Line> lines, List<Player> players, String text, double fraction) {
        RaidDisplay.Line line = new RaidDisplay.Line(text, fraction);
        for (Player player : players) {
            lines.put(player, line);
        }
    }

    /**
     * The highest capture progress (as a percentage) among the chunks
     * still being contested, for the display.
     */
    private int currentCapturePercent(Raid raid, List<ProtectedChunk> capturable, RaidConfig config) {
        double best = 0;
        for (ProtectedChunk chunk : capturable) {
            if (!raid.isCaptured(chunk)) {
                best = Math.max(best, raid.getProgress(chunk));
            }
        }
        return (int) Math.round(best / config.getCaptureTimeSeconds() * 100);
    }

    // Presence ---------------------------------------------------------

    private ChunkPresence countPresence(BeaconShieldBlock beacon, Raid raid, World world) {
        ChunkPresence presence = new ChunkPresence();
        for (Player player : world.getPlayers()) {
            long key = key(player.getLocation().getBlockX() >> 4, player.getLocation().getBlockZ() >> 4);
            if (beacon.hasMember(player)) {
                presence.defenders.merge(key, 1, Integer::sum);
            } else if (manager.countsAsAttacker(beacon, raid, player)) {
                presence.attackers.merge(key, 1, Integer::sum);
            }
        }
        return presence;
    }

    private static long key(int x, int z) {
        return ((long) x << 32) ^ (z & 0xffffffffL);
    }

    /** Attacker/defender counts keyed by packed chunk coordinates. */
    private static final class ChunkPresence {
        final Map<Long, Integer> attackers = new HashMap<>();
        final Map<Long, Integer> defenders = new HashMap<>();
    }
}
