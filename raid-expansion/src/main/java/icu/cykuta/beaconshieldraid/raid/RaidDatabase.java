package icu.cykuta.beaconshieldraid.raid;

import icu.cykuta.beaconshield.BeaconShieldAPI;
import icu.cykuta.beaconshield.beacon.BeaconShieldBlock;
import icu.cykuta.beaconshield.beacon.protection.ProtectedChunk;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Persists raid state in a SQLite database ({@code raids.db}) so raids
 * survive a restart or crash. All access is on the main thread; the
 * whole state is small, so {@link #save} rewrites it in one transaction.
 */
public class RaidDatabase {

    private final File dataDir;
    private final Logger logger;
    private Connection connection;

    public RaidDatabase(File dataDir, Logger logger) {
        this.dataDir = dataDir;
        this.logger = logger;
        connect();
        createTables();
    }

    private void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            if (!dataDir.exists() && !dataDir.mkdirs()) {
                logger.warning("Could not create the data folder for raids.db");
            }
            File file = new File(dataDir, "raids.db");
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
        } catch (ClassNotFoundException | SQLException e) {
            logger.severe("Could not open raids.db: " + e.getMessage());
        }
    }

    private void createTables() {
        if (connection == null) {
            return;
        }
        try (Statement st = connection.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS meta (key TEXT PRIMARY KEY, value TEXT)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS raids ("
                    + "beacon_id TEXT PRIMARY KEY, active INTEGER, started_at INTEGER, freeze_ends_at INTEGER,"
                    + "began INTEGER, last_raider_seen INTEGER, initiator TEXT, nexus_health INTEGER,"
                    + "last_nexus_damage INTEGER, origin_world TEXT, origin_x REAL, origin_y REAL, origin_z REAL)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS raid_party (beacon_id TEXT, uuid TEXT)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS raid_captured (beacon_id TEXT, world TEXT, x INTEGER, z INTEGER)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS raid_progress (beacon_id TEXT, world TEXT, x INTEGER, z INTEGER, seconds REAL)");
        } catch (SQLException e) {
            logger.severe("Could not create raid tables: " + e.getMessage());
        }
    }

    /**
     * Rewrite the whole raid state (raiding flag + every non-idle raid) in
     * a single transaction.
     */
    public void save(RaidManager manager) {
        if (connection == null) {
            return;
        }
        try {
            connection.setAutoCommit(false);

            setMeta("raiding-enabled", Boolean.toString(manager.isRaidingEnabled()));
            clearAllRaids();

            for (Raid raid : manager.getActiveRaids()) {
                if (!raid.isIdle()) {
                    insertRaid(raid);
                }
            }
            connection.commit();
        } catch (SQLException e) {
            rollback();
            logger.warning("Could not save raids: " + e.getMessage());
        } finally {
            autoCommit();
        }
    }

    /**
     * Rebuild raids from the database, restoring the raiding flag. Raids
     * for beacons that no longer exist are ignored.
     */
    public void load(RaidManager manager, BeaconShieldAPI api) {
        if (connection == null) {
            return;
        }
        manager.restoreRaidingEnabled(Boolean.parseBoolean(getMeta("raiding-enabled", "false")));

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM raids")) {
            while (rs.next()) {
                loadRaid(manager, rs);
            }
        } catch (SQLException e) {
            logger.warning("Could not load raids: " + e.getMessage());
        }
    }

    private void loadRaid(RaidManager manager, ResultSet rs) throws SQLException {
        String beaconId = rs.getString("beacon_id");
        BeaconShieldBlock beacon = manager.getBeaconById(beaconId);
        if (beacon == null) {
            return; // beacon removed while the server was off
        }

        Raid raid = new Raid(beacon);
        raid.setActive(rs.getInt("active") != 0);
        raid.setStartedAt(rs.getLong("started_at"));
        raid.setFreezeEndsAt(rs.getLong("freeze_ends_at"));
        raid.setBeganAnnounced(rs.getInt("began") != 0);
        raid.setLastRaiderSeen(System.currentTimeMillis()); // grace period after a restart

        String initiator = rs.getString("initiator");
        if (initiator != null) {
            parseUuid(initiator).ifPresent(raid::setInitiator);
        }
        String originWorld = rs.getString("origin_world");
        if (originWorld != null) {
            raid.setOrigin(originWorld, rs.getDouble("origin_x"), rs.getDouble("origin_y"), rs.getDouble("origin_z"));
        }
        int nexusHealth = rs.getInt("nexus_health");
        if (nexusHealth >= 0) {
            raid.restoreNexus(nexusHealth, rs.getLong("last_nexus_damage"));
        }

        loadParty(beaconId, raid);
        loadCaptured(beaconId, raid);
        loadProgress(beaconId, raid);

        manager.loadRaid(raid);
    }

    private void loadParty(String beaconId, Raid raid) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("SELECT uuid FROM raid_party WHERE beacon_id = ?")) {
            ps.setString(1, beaconId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    parseUuid(rs.getString("uuid")).ifPresent(raid::addToParty);
                }
            }
        }
    }

    private void loadCaptured(String beaconId, Raid raid) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT world, x, z FROM raid_captured WHERE beacon_id = ?")) {
            ps.setString(1, beaconId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ProtectedChunk chunk = toChunk(rs.getString("world"), rs.getInt("x"), rs.getInt("z"));
                    if (chunk != null) {
                        raid.markCaptured(chunk);
                    }
                }
            }
        }
    }

    private void loadProgress(String beaconId, Raid raid) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT world, x, z, seconds FROM raid_progress WHERE beacon_id = ?")) {
            ps.setString(1, beaconId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ProtectedChunk chunk = toChunk(rs.getString("world"), rs.getInt("x"), rs.getInt("z"));
                    if (chunk != null) {
                        raid.setProgress(chunk, rs.getDouble("seconds"));
                    }
                }
            }
        }
    }

    // Writes -----------------------------------------------------------

    private void insertRaid(Raid raid) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO raids (beacon_id, active, started_at,"
                + " freeze_ends_at, began, last_raider_seen, initiator, nexus_health, last_nexus_damage,"
                + " origin_world, origin_x, origin_y, origin_z) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)")) {
            ps.setString(1, raid.getBeaconId());
            ps.setInt(2, raid.isActive() ? 1 : 0);
            ps.setLong(3, raid.getStartedAt());
            ps.setLong(4, raid.getFreezeEndsAt());
            ps.setInt(5, raid.isBeganAnnounced() ? 1 : 0);
            ps.setLong(6, raid.getLastRaiderSeen());
            ps.setString(7, raid.getInitiator() == null ? null : raid.getInitiator().toString());
            ps.setInt(8, raid.getNexusHealth());
            ps.setLong(9, raid.getLastNexusDamage());
            ps.setString(10, raid.getOriginWorld());
            ps.setDouble(11, raid.getOriginX());
            ps.setDouble(12, raid.getOriginY());
            ps.setDouble(13, raid.getOriginZ());
            ps.executeUpdate();
        }

        for (UUID uuid : raid.getParty()) {
            insertChild("INSERT INTO raid_party (beacon_id, uuid) VALUES (?, ?)", raid.getBeaconId(), uuid.toString());
        }
        for (ProtectedChunk chunk : raid.getCapturedChunks()) {
            insertChunk("INSERT INTO raid_captured (beacon_id, world, x, z) VALUES (?,?,?,?)", raid.getBeaconId(), chunk, null);
        }
        for (Map.Entry<ProtectedChunk, Double> entry : raid.getProgressEntries().entrySet()) {
            insertChunk("INSERT INTO raid_progress (beacon_id, world, x, z, seconds) VALUES (?,?,?,?,?)",
                    raid.getBeaconId(), entry.getKey(), entry.getValue());
        }
    }

    private void insertChild(String sql, String beaconId, String value) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, beaconId);
            ps.setString(2, value);
            ps.executeUpdate();
        }
    }

    private void insertChunk(String sql, String beaconId, ProtectedChunk chunk, Double seconds) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, beaconId);
            ps.setString(2, chunk.getWorldName());
            ps.setInt(3, chunk.getX());
            ps.setInt(4, chunk.getZ());
            if (seconds != null) {
                ps.setDouble(5, seconds);
            }
            ps.executeUpdate();
        }
    }

    private void clearAllRaids() throws SQLException {
        try (Statement st = connection.createStatement()) {
            st.executeUpdate("DELETE FROM raids");
            st.executeUpdate("DELETE FROM raid_party");
            st.executeUpdate("DELETE FROM raid_captured");
            st.executeUpdate("DELETE FROM raid_progress");
        }
    }

    // Meta / util ------------------------------------------------------

    private void setMeta(String key, String value) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO meta (key, value) VALUES (?, ?) ON CONFLICT(key) DO UPDATE SET value = ?")) {
            ps.setString(1, key);
            ps.setString(2, value);
            ps.setString(3, value);
            ps.executeUpdate();
        }
    }

    private String getMeta(String key, String def) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT value FROM meta WHERE key = ?")) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("value") : def;
            }
        } catch (SQLException e) {
            return def;
        }
    }

    private ProtectedChunk toChunk(String worldName, int x, int z) {
        World world = Bukkit.getWorld(worldName);
        return world == null ? null : new ProtectedChunk(x, z, world);
    }

    private java.util.Optional<UUID> parseUuid(String raw) {
        try {
            return java.util.Optional.of(UUID.fromString(raw));
        } catch (IllegalArgumentException e) {
            return java.util.Optional.empty();
        }
    }

    private void rollback() {
        try {
            if (connection != null) {
                connection.rollback();
            }
        } catch (SQLException ignored) {
            // nothing to do
        }
    }

    private void autoCommit() {
        try {
            if (connection != null) {
                connection.setAutoCommit(true);
            }
        } catch (SQLException ignored) {
            // nothing to do
        }
    }

    /**
     * Close the database connection (call on plugin disable).
     */
    public void close() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException ignored) {
            // nothing to do
        }
    }
}
