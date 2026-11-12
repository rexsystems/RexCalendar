package me.rexsystems.rexCalendar.config;

import me.rexsystems.rexCalendar.RexCalendar;

public final class ConfigManager {

    private final RexCalendar plugin;
    private volatile LoadedConfig loaded;

    public ConfigManager(RexCalendar plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        new ConfigAutoUpdater(plugin).update();
        plugin.reloadConfig();
        this.loaded = LoadedConfig.read(plugin.getConfig(), plugin.getLogger());
    }

    public void reload() {
        load();
    }

    public LoadedConfig current() {
        LoadedConfig value = loaded;
        if (value == null) {
            throw new IllegalStateException("Config is not loaded");
        }
        return value;
    }
}
