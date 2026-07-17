package icu.cykuta.beaconshieldraid.config;

import icu.cykuta.beaconshieldraid.BeaconShieldRaidExpansion;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

/**
 * Typed, cached view over the raid expansion's settings ({@code config.yml})
 * and messages ({@code lang.yml}). Both files live under the core folder
 * ({@code plugins/BeaconShield/expansions/<name>/}). Call {@link #reload()}
 * to re-read them.
 */
public class RaidConfig {

    /**
     * How a chunk's capture progress behaves while it is not being taken.
     */
    public enum ContestedBehavior { PAUSE, RESET, DECAY }

    private final BeaconShieldRaidExpansion plugin;

    private FileConfiguration config;
    private FileConfiguration lang;

    private int captureTimeSeconds;
    private int minAttackers;
    private ContestedBehavior contestedBehavior;
    private double decayPerSecond;
    private int nexusHealth;
    private boolean nexusDropLoot;
    private int nexusRegenSeconds;
    private boolean resetOnDisable;
    private boolean initialRaidingEnabled;
    private boolean ignoreUpgradesDuringRaid;
    private boolean allowOfflineRaid;
    private boolean onlineRequirementPercent;
    private double onlineRequirementValue;
    private boolean startCostEnabled;
    private String startCostFormula;
    private int freezeSeconds;
    private double joinRadius;
    private boolean joinInviteEnabled;
    private int defendersWinSeconds;
    private String displayType;
    private int displayChatInterval;
    private String bossbarColor;
    private String bossbarStyle;

    public RaidConfig(BeaconShieldRaidExpansion plugin) {
        this.plugin = plugin;
        reload();
    }

    /**
     * The raw settings file, for components that read their own section
     * (e.g. the schedule).
     */
    public FileConfiguration getSettings() {
        return config;
    }

    /**
     * Re-read config.yml and lang.yml from the expansion data folder,
     * copying the bundled defaults the first time.
     */
    public void reload() {
        this.config = YamlConfiguration.loadConfiguration(plugin.saveDefaultResource("config.yml"));
        this.lang = YamlConfiguration.loadConfiguration(plugin.saveDefaultResource("lang.yml"));

        this.captureTimeSeconds = Math.max(1, config.getInt("capture.time-seconds", 60));
        this.minAttackers = Math.max(1, config.getInt("capture.min-attackers", 1));
        this.contestedBehavior = parseBehavior(config.getString("capture.contested-behavior", "DECAY"));
        this.decayPerSecond = Math.max(0, config.getDouble("capture.decay-per-second", 1));
        this.nexusHealth = Math.max(1, config.getInt("nexus.health", 10));
        this.nexusDropLoot = config.getBoolean("nexus.drop-loot", true);
        this.nexusRegenSeconds = Math.max(0, config.getInt("nexus.regen-seconds", 30));
        this.resetOnDisable = config.getBoolean("reset-on-disable", true);
        this.initialRaidingEnabled = config.getBoolean("raiding-enabled", false);
        this.ignoreUpgradesDuringRaid = config.getBoolean("ignore-upgrades-during-raid", true);
        this.allowOfflineRaid = config.getBoolean("online-requirement.allow-offline", false);
        parseOnlineRequirement(config.getString("online-requirement.required", "50%"));
        this.startCostEnabled = config.getBoolean("start-cost.enabled", true);
        this.startCostFormula = config.getString("start-cost.formula", "100");
        this.freezeSeconds = Math.max(0, config.getInt("join.freeze-seconds", 30));
        this.joinRadius = Math.max(0, config.getDouble("join.radius", 10));
        this.joinInviteEnabled = config.getBoolean("join.invite", true);
        this.defendersWinSeconds = Math.max(0, config.getInt("defenders-win-seconds", 120));
        this.displayType = config.getString("display.type", "ACTIONBAR");
        this.displayChatInterval = Math.max(1, config.getInt("display.chat-interval-seconds", 5));
        this.bossbarColor = config.getString("display.bossbar-color", "RED");
        this.bossbarStyle = config.getString("display.bossbar-style", "SEGMENTED_10");
    }

    /**
     * Parse the {@code online-requirement.required} setting, which is either
     * a percentage of the members ("50%") or an exact count ("2").
     */
    private void parseOnlineRequirement(String raw) {
        String value = raw == null ? "50%" : raw.trim();
        if (value.endsWith("%")) {
            this.onlineRequirementPercent = true;
            String number = value.substring(0, value.length() - 1).trim();
            try {
                this.onlineRequirementValue = Math.max(0, Double.parseDouble(number));
            } catch (NumberFormatException e) {
                plugin.getLogger().warning("Invalid online-requirement.required '" + raw + "', defaulting to 50%.");
                this.onlineRequirementValue = 50;
            }
        } else {
            this.onlineRequirementPercent = false;
            try {
                this.onlineRequirementValue = Math.max(0, Long.parseLong(value));
            } catch (NumberFormatException e) {
                plugin.getLogger().warning("Invalid online-requirement.required '" + raw + "', defaulting to 1.");
                this.onlineRequirementValue = 1;
            }
        }
    }

    /**
     * How many of a protection's members must be online to raid it. A
     * percentage is rounded up (50% of 5 members -> 3), and the result is
     * always at least 1 and never more than the member count.
     */
    public int requiredOnlineToRaid(int memberCount) {
        if (memberCount <= 0) {
            return 0;
        }
        int required;
        if (onlineRequirementPercent) {
            required = (int) Math.ceil(memberCount * onlineRequirementValue / 100.0);
        } else {
            required = (int) onlineRequirementValue;
        }
        return Math.min(memberCount, Math.max(1, required));
    }

    /**
     * Whether a protection may be raided while none of its members are
     * online.
     */
    public boolean isAllowOfflineRaid() {
        return allowOfflineRaid;
    }

    private ContestedBehavior parseBehavior(String raw) {
        try {
            return ContestedBehavior.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            plugin.getLogger().warning("Invalid capture.contested-behavior '" + raw + "', defaulting to DECAY.");
            return ContestedBehavior.DECAY;
        }
    }

    public int getCaptureTimeSeconds() {
        return captureTimeSeconds;
    }

    public int getMinAttackers() {
        return minAttackers;
    }

    public ContestedBehavior getContestedBehavior() {
        return contestedBehavior;
    }

    public double getDecayPerSecond() {
        return decayPerSecond;
    }

    public int getNexusHealth() {
        return nexusHealth;
    }

    public boolean isNexusDropLoot() {
        return nexusDropLoot;
    }

    public int getNexusRegenSeconds() {
        return nexusRegenSeconds;
    }

    public boolean isResetOnDisable() {
        return resetOnDisable;
    }

    public boolean isInitialRaidingEnabled() {
        return initialRaidingEnabled;
    }

    public boolean isIgnoreUpgradesDuringRaid() {
        return ignoreUpgradesDuringRaid;
    }

    public boolean isStartCostEnabled() {
        return startCostEnabled;
    }

    public String getStartCostFormula() {
        return startCostFormula;
    }

    public int getFreezeSeconds() {
        return freezeSeconds;
    }

    public double getJoinRadius() {
        return joinRadius;
    }

    /**
     * Whether starting a raid announces the clickable join invite to nearby
     * outsiders. When off, joining goes through /bsraid request or a leader
     * invite; nothing else about the freeze window changes.
     */
    public boolean isJoinInviteEnabled() {
        return joinInviteEnabled;
    }

    public int getDefendersWinSeconds() {
        return defendersWinSeconds;
    }

    public String getDisplayType() {
        return displayType;
    }

    public int getDisplayChatInterval() {
        return displayChatInterval;
    }

    public String getBossbarColor() {
        return bossbarColor;
    }

    public String getBossbarStyle() {
        return bossbarStyle;
    }

    /**
     * Build a colored message from a {@code lang.yml} key, applying the
     * prefix and {@code %placeholder% -> value} replacements. Returns
     * {@code null} when the message is empty (so callers can skip it).
     *
     * @param key          The message key in lang.yml.
     * @param replacements Alternating placeholder/value pairs.
     */
    public String message(String key, String... replacements) {
        String raw = lang.getString(key, "");
        if (raw == null || raw.isEmpty()) {
            return null;
        }

        String result = prefix() + applyReplacements(raw, replacements);
        return ChatColor.translateAlternateColorCodes('&', result);
    }

    /**
     * Colorize an ad-hoc message and prepend the prefix. Used for command
     * replies that are not backed by a lang key.
     */
    public String format(String message) {
        return ChatColor.translateAlternateColorCodes('&', prefix() + message);
    }

    /**
     * Colorize a lang value with no prefix, falling back to a default. Used
     * for chat-component pieces like the join button.
     */
    public String colored(String key, String fallback) {
        String raw = lang.getString(key, fallback);
        if (raw == null || raw.isEmpty()) {
            raw = fallback;
        }
        return ChatColor.translateAlternateColorCodes('&', raw);
    }

    /**
     * Like {@link #colored} but with {@code %placeholder% -> value}
     * replacements. Used for the freeze/progress display text.
     */
    public String display(String key, String fallback, String... replacements) {
        String raw = lang.getString(key, fallback);
        if (raw == null || raw.isEmpty()) {
            raw = fallback;
        }
        return ChatColor.translateAlternateColorCodes('&', applyReplacements(raw, replacements));
    }

    private String prefix() {
        String prefix = lang.getString("prefix", "");
        return prefix == null ? "" : prefix;
    }

    private String applyReplacements(String text, String... replacements) {
        String result = text;
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            result = result.replace(replacements[i], replacements[i + 1]);
        }
        return result;
    }
}
