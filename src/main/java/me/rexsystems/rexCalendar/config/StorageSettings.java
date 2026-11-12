package me.rexsystems.rexCalendar.config;

public final class StorageSettings {

    private final boolean mysql;
    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;
    private final int poolSize;
    private final boolean ssl;

    public StorageSettings(boolean mysql, String host, int port, String database, String username, String password, int poolSize, boolean ssl) {
        this.mysql = mysql;
        this.host = host == null ? "127.0.0.1" : host;
        this.port = port;
        this.database = database == null ? "" : database;
        this.username = username == null ? "" : username;
        this.password = password == null ? "" : password;
        this.poolSize = poolSize;
        this.ssl = ssl;
    }

    public boolean mysql() {
        return mysql;
    }

    public String host() {
        return host;
    }

    public int port() {
        return port;
    }

    public String database() {
        return database;
    }

    public String username() {
        return username;
    }

    public String password() {
        return password;
    }

    public int poolSize() {
        return poolSize;
    }

    public boolean ssl() {
        return ssl;
    }
}
