package me.rexsystems.rexCalendar;

import me.rexsystems.rexCalendar.claim.ClaimGate;
import me.rexsystems.rexCalendar.claim.ClaimService;
import me.rexsystems.rexCalendar.command.CalendarCommand;
import me.rexsystems.rexCalendar.config.ConfigManager;
import me.rexsystems.rexCalendar.config.LoadedConfig;
import me.rexsystems.rexCalendar.feedback.ClaimFeedback;
import me.rexsystems.rexCalendar.gui.CalendarListener;
import me.rexsystems.rexCalendar.gui.CalendarMenu;
import me.rexsystems.rexCalendar.storage.ClaimDatabase;
import me.rexsystems.rexCalendar.util.Notices;
import me.rexsystems.rexCalendar.util.SchedulerUtils;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class RexCalendar extends JavaPlugin {

    private static final int BSTATS_PLUGIN_ID = 35264;

    private final ClaimGate claimGate = new ClaimGate();
    private final Set<String> warnings = ConcurrentHashMap.newKeySet();
    private ConfigManager configManager;
    private ClaimDatabase database;
    private ClaimService claimService;
    private ClaimFeedback feedback;
    private CalendarMenu menu;

    @Override
    public void onEnable() {
        this.configManager = new ConfigManager(this);
        this.configManager.load();
        this.database = new ClaimDatabase(this);
        try {
            this.database.start(configManager.current().storage());
        } catch (Exception failure) {
            getLogger().severe("Storage failed: " + failure.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        this.feedback = new ClaimFeedback(this);
        this.claimService = new ClaimService(this);
        this.menu = new CalendarMenu(this);
        getServer().getPluginManager().registerEvents(new CalendarListener(this), this);

        CalendarCommand command = new CalendarCommand(this);
        PluginCommand pluginCommand = getCommand("rexcalendar");
        if (pluginCommand != null) {
            pluginCommand.setExecutor(command);
            pluginCommand.setTabCompleter(command);
        }

        String mode = configManager.current().storage().mysql() ? "mysql" : "sqlite";
        new Metrics(this, BSTATS_PLUGIN_ID);
        getLogger().info("RexCalendar enabled. Storage: " + mode + ".");
    }

    @Override
    public void onDisable() {
        if (feedback != null) {
            feedback.clear();
        }
        if (database != null) {
            database.stop();
        }
        getLogger().info("RexCalendar disabled.");
    }

    public void reloadPlugin() {
        warnings.clear();
        configManager.reload();
    }

    public void openCalendar(Player player, int page) {
        SchedulerUtils.runForPlayer(this, player, () -> {
            if (!player.isOnline()) {
                return;
            }
            LoadedConfig config = configManager.current();
            if (!player.hasPermission("rexcalendar.use") && !player.hasPermission("rexcalendar.admin")) {
                Notices.send(this, player, "no-permission", Map.of());
                return;
            }
            if (player.getWorld() == null || !config.worldAllowed(player.getWorld().getName())) {
                Notices.send(this, player, "world", Map.of());
                return;
            }
            menu.open(player, page);
        });
    }

    public void warnOnce(String key, String message) {
        if (warnings.add(key)) {
            getLogger().warning(message);
        }
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ClaimDatabase getDatabase() {
        return database;
    }

    public ClaimGate getClaimGate() {
        return claimGate;
    }

    public ClaimService getClaimService() {
        return claimService;
    }

    public ClaimFeedback getFeedback() {
        return feedback;
    }

    public CalendarMenu getMenu() {
        return menu;
    }
}
