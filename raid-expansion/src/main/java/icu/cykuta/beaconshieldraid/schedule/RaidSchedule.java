package icu.cykuta.beaconshieldraid.schedule;

import icu.cykuta.beaconshieldraid.config.RaidConfig;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Parses and evaluates the automatic raid schedule from {@code config.yml}.
 * When enabled, it decides whether raiding should be on for a given
 * moment based on the configured day/time windows.
 */
public class RaidSchedule {

    private final JavaPlugin plugin;
    private final RaidConfig config;

    private boolean enabled;
    private final List<Window> windows = new ArrayList<>();

    public RaidSchedule(JavaPlugin plugin, RaidConfig config) {
        this.plugin = plugin;
        this.config = config;
        reload();
    }

    /**
     * Re-parse the schedule section. Malformed windows are skipped with a
     * warning instead of aborting the whole schedule.
     */
    public void reload() {
        this.enabled = config.getSettings().getBoolean("schedule.enabled", false);
        this.windows.clear();

        List<Map<?, ?>> raw = config.getSettings().getMapList("schedule.windows");
        for (Map<?, ?> entry : raw) {
            Window window = parseWindow(entry);
            if (window != null) {
                windows.add(window);
            }
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Whether raiding should be active at the given moment according to
     * the windows. A window is matched when its day set contains the day
     * and the time falls inside [from, to); windows whose {@code to} is
     * earlier than {@code from} wrap past midnight.
     */
    public boolean isActiveAt(LocalDateTime now) {
        DayOfWeek day = now.getDayOfWeek();
        LocalTime time = now.toLocalTime();

        for (Window window : windows) {
            if (window.days.contains(day) && window.contains(time)) {
                return true;
            }
        }
        return false;
    }

    private Window parseWindow(Map<?, ?> entry) {
        try {
            Set<DayOfWeek> days = parseDays(entry.get("days"));
            LocalTime from = LocalTime.parse(String.valueOf(entry.get("from")));
            LocalTime to = LocalTime.parse(String.valueOf(entry.get("to")));

            if (days.isEmpty()) {
                plugin.getLogger().warning("Skipping schedule window with no valid days: " + entry);
                return null;
            }
            return new Window(days, from, to);
        } catch (RuntimeException e) {
            plugin.getLogger().warning("Skipping malformed schedule window " + entry + " (" + e.getMessage() + ")");
            return null;
        }
    }

    private Set<DayOfWeek> parseDays(Object raw) {
        Set<DayOfWeek> days = EnumSet.noneOf(DayOfWeek.class);
        if (!(raw instanceof List<?> list)) {
            return days;
        }

        for (Object dayObj : list) {
            DayOfWeek day = DAYS.get(String.valueOf(dayObj).trim().toUpperCase());
            if (day != null) {
                days.add(day);
            } else {
                plugin.getLogger().warning("Unknown day in schedule: " + dayObj);
            }
        }
        return days;
    }

    private static final Map<String, DayOfWeek> DAYS = Map.ofEntries(
            Map.entry("MON", DayOfWeek.MONDAY),
            Map.entry("TUE", DayOfWeek.TUESDAY),
            Map.entry("WED", DayOfWeek.WEDNESDAY),
            Map.entry("THU", DayOfWeek.THURSDAY),
            Map.entry("FRI", DayOfWeek.FRIDAY),
            Map.entry("SAT", DayOfWeek.SATURDAY),
            Map.entry("SUN", DayOfWeek.SUNDAY));

    /** A single day/time window. */
    private record Window(Set<DayOfWeek> days, LocalTime from, LocalTime to) {
        boolean contains(LocalTime time) {
            if (from.equals(to)) {
                return false; // zero-length window
            }
            if (from.isBefore(to)) {
                return !time.isBefore(from) && time.isBefore(to);
            }
            // Overnight window (e.g. 22:00 -> 02:00)
            return !time.isBefore(from) || time.isBefore(to);
        }
    }
}
