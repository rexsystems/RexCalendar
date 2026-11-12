package me.rexsystems.rexCalendar.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.rexsystems.rexCalendar.RexCalendar;
import me.rexsystems.rexCalendar.config.StorageSettings;

import java.io.File;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

public final class ClaimDatabase {

    private final RexCalendar plugin;
    private final Object monitor = new Object();
    private final List<Driver> registered = new ArrayList<>();
    private Connection sqlite;
    private HikariDataSource pool;
    private boolean mysql;

    public ClaimDatabase(RexCalendar plugin) {
        this.plugin = plugin;
    }

    public void start(StorageSettings settings) throws Exception {
        synchronized (monitor) {
            stop();
            ClassLoader previous = Thread.currentThread().getContextClassLoader();
            // Hikari loads the driver through the context classloader.
            Thread.currentThread().setContextClassLoader(plugin.getClass().getClassLoader());
            try {
                if (settings != null && settings.mysql()) {
                    startMysql(settings);
                } else {
                    startSqlite();
                }
            } catch (Exception failure) {
                stop();
                throw failure;
            } finally {
                Thread.currentThread().setContextClassLoader(previous);
            }
        }
    }

    public void stop() {
        synchronized (monitor) {
            if (pool != null) {
                pool.close();
                pool = null;
            }
            if (sqlite != null) {
                try {
                    sqlite.close();
                } catch (SQLException ignored) {
                }
                sqlite = null;
            }
            for (Driver driver : registered) {
                try {
                    DriverManager.deregisterDriver(driver);
                } catch (SQLException ignored) {
                }
            }
            registered.clear();
            mysql = false;
        }
    }

    public boolean hasClaim(UUID player, String dayKey) throws SQLException {
        synchronized (monitor) {
            Connection connection = open();
            try {
                return hasClaim(connection, player, dayKey);
            } finally {
                closeIfPooled(connection, mysql);
            }
        }
    }

    public Set<String> claimedBetween(UUID player, String fromInclusive, String toExclusive) throws SQLException {
        synchronized (monitor) {
            Set<String> keys = new HashSet<>();
            Connection connection = open();
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT day_key FROM rexcalendar_claims WHERE player_uuid = ? AND day_key >= ? AND day_key < ?")) {
                statement.setString(1, player.toString());
                statement.setString(2, fromInclusive);
                statement.setString(3, toExclusive);
                try (ResultSet rows = statement.executeQuery()) {
                    while (rows.next()) {
                        keys.add(rows.getString(1));
                    }
                }
            } finally {
                closeIfPooled(connection, mysql);
            }
            return keys;
        }
    }

    public ClaimWrite insert(UUID player, String dayKey, String ipHash, int ipLimit) {
        synchronized (monitor) {
            Connection connection = null;
            boolean returnToPool = mysql;
            try {
                connection = open();
                boolean previous = connection.getAutoCommit();
                int previousIsolation = connection.getTransactionIsolation();
                connection.setAutoCommit(false);
                if (mysql) {
                    connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
                }
                try {
                    if (hasClaim(connection, player, dayKey)) {
                        connection.rollback();
                        return ClaimWrite.DUPLICATE;
                    }
                    try (PreparedStatement statement = connection.prepareStatement(
                            "INSERT INTO rexcalendar_claims (player_uuid, day_key, claimed_at) VALUES (?, ?, ?)")) {
                        statement.setString(1, player.toString());
                        statement.setString(2, dayKey);
                        statement.setLong(3, System.currentTimeMillis());
                        statement.executeUpdate();
                    }
                    if (ipLimit > 0 && ipHash != null && !ipHash.isEmpty()) {
                        try (PreparedStatement statement = connection.prepareStatement(
                                "INSERT INTO rexcalendar_ip_claims (ip_hash, day_key, player_uuid) VALUES (?, ?, ?)")) {
                            statement.setString(1, ipHash);
                            statement.setString(2, dayKey);
                            statement.setString(3, player.toString());
                            statement.executeUpdate();
                        }
                        int count = countAddresses(connection, ipHash, dayKey);
                        if (count > ipLimit) {
                            connection.rollback();
                            return ClaimWrite.IP_LIMIT;
                        }
                    }
                    connection.commit();
                    return ClaimWrite.STORED;
                } catch (SQLException failure) {
                    rollback(connection);
                    if (isDuplicate(failure)) {
                        return ClaimWrite.DUPLICATE;
                    }
                    plugin.getLogger().warning("Claim insert failed for " + player + " on " + dayKey + ": " + failure.getMessage());
                    return ClaimWrite.FAILED;
                } finally {
                    if (mysql) {
                        connection.setTransactionIsolation(previousIsolation);
                    }
                    connection.setAutoCommit(previous);
                }
            } catch (SQLException failure) {
                plugin.getLogger().warning("Claim insert failed for " + player + " on " + dayKey + ": " + failure.getMessage());
                return ClaimWrite.FAILED;
            } finally {
                closeIfPooled(connection, returnToPool);
            }
        }
    }

    public void delete(UUID player, String dayKey) throws SQLException {
        synchronized (monitor) {
            Connection connection = null;
            boolean returnToPool = mysql;
            try {
                connection = open();
                boolean previous = connection.getAutoCommit();
                connection.setAutoCommit(false);
                try {
                    try (PreparedStatement statement = connection.prepareStatement(
                            "DELETE FROM rexcalendar_claims WHERE player_uuid = ? AND day_key = ?")) {
                        statement.setString(1, player.toString());
                        statement.setString(2, dayKey);
                        statement.executeUpdate();
                    }
                    try (PreparedStatement statement = connection.prepareStatement(
                            "DELETE FROM rexcalendar_ip_claims WHERE player_uuid = ? AND day_key = ?")) {
                        statement.setString(1, player.toString());
                        statement.setString(2, dayKey);
                        statement.executeUpdate();
                    }
                    connection.commit();
                } catch (SQLException failure) {
                    rollback(connection);
                    throw failure;
                } finally {
                    connection.setAutoCommit(previous);
                }
            } finally {
                closeIfPooled(connection, returnToPool);
            }
        }
    }

    private void startSqlite() throws Exception {
        File file = new File(plugin.getDataFolder(), "calendar.db");
        register("org.sqlite.JDBC", "me.rexsystems.rexCalendar.libs.sqlite.JDBC");
        Driver driver = registered.get(registered.size() - 1);
        sqlite = driver.connect("jdbc:sqlite:" + file.getAbsolutePath(), new Properties());
        if (sqlite == null) {
            throw new SQLException("SQLite driver rejected calendar.db");
        }
        mysql = false;
        try (Statement statement = sqlite.createStatement()) {
            statement.execute("PRAGMA busy_timeout=5000");
        }
        createTables(sqlite);
    }

    private void startMysql(StorageSettings settings) throws Exception {
        String driverClass = register(
                "com.mysql.cj.jdbc.Driver",
                "me.rexsystems.rexCalendar.libs.mysql.cj.jdbc.Driver");
        String url = "jdbc:mysql://" + settings.host() + ":" + settings.port() + "/" + settings.database()
                + "?characterEncoding=utf8&serverTimezone=UTC&useSSL=" + settings.ssl();
        if (!settings.ssl()) {
            url = url + "&allowPublicKeyRetrieval=true";
        }
        HikariConfig hikari = new HikariConfig();
        hikari.setJdbcUrl(url);
        hikari.setUsername(settings.username());
        hikari.setPassword(settings.password());
        hikari.setDriverClassName(driverClass);
        hikari.setMaximumPoolSize(settings.poolSize());
        hikari.setMinimumIdle(1);
        hikari.setPoolName("RexCalendar");
        hikari.setConnectionTimeout(10_000L);
        hikari.setMaxLifetime(600_000L);
        pool = new HikariDataSource(hikari);
        mysql = true;
        try (Connection connection = pool.getConnection()) {
            createTables(connection);
        }
    }

    private String register(String original, String relocated) throws Exception {
        Class<?> type;
        ClassLoader loader = plugin.getClass().getClassLoader();
        try {
            type = Class.forName(original, true, loader);
        } catch (ClassNotFoundException ex) {
            type = Class.forName(relocated, true, loader);
        }
        Driver loaded = (Driver) type.getDeclaredConstructor().newInstance();
        DriverShim shim = new DriverShim(loaded);
        DriverManager.registerDriver(shim);
        registered.add(shim);
        return type.getName();
    }

    private void createTables(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS rexcalendar_claims (
                      player_uuid VARCHAR(36) NOT NULL,
                      day_key VARCHAR(10) NOT NULL,
                      claimed_at BIGINT NOT NULL,
                      PRIMARY KEY (player_uuid, day_key)
                    )
                    """);
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS rexcalendar_ip_claims (
                      ip_hash CHAR(64) NOT NULL,
                      day_key VARCHAR(10) NOT NULL,
                      player_uuid VARCHAR(36) NOT NULL,
                      PRIMARY KEY (ip_hash, day_key, player_uuid)
                    )
                    """);
        }
    }

    private Connection open() throws SQLException {
        if (mysql) {
            if (pool == null) {
                throw new SQLException("MySQL pool is closed");
            }
            return pool.getConnection();
        }
        if (sqlite == null || sqlite.isClosed()) {
            throw new SQLException("SQLite connection is closed");
        }
        return sqlite;
    }

    private static void closeIfPooled(Connection connection, boolean pooled) {
        if (!pooled || connection == null) {
            return;
        }
        try {
            connection.close();
        } catch (SQLException ignored) {
        }
    }

    private static boolean hasClaim(Connection connection, UUID player, String dayKey) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT 1 FROM rexcalendar_claims WHERE player_uuid = ? AND day_key = ?")) {
            statement.setString(1, player.toString());
            statement.setString(2, dayKey);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next();
            }
        }
    }

    private static int countAddresses(Connection connection, String ipHash, String dayKey) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT COUNT(*) FROM rexcalendar_ip_claims WHERE ip_hash = ? AND day_key = ?")) {
            statement.setString(1, ipHash);
            statement.setString(2, dayKey);
            try (ResultSet rows = statement.executeQuery()) {
                if (rows.next()) {
                    return rows.getInt(1);
                }
                return 0;
            }
        }
    }

    private static void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException ignored) {
        }
    }

    private static boolean isDuplicate(SQLException failure) {
        int code = failure.getErrorCode();
        if (code == 19 || code == 1062 || code == 1555 || code == 2067) {
            return true;
        }
        return "23000".equals(failure.getSQLState());
    }
}
