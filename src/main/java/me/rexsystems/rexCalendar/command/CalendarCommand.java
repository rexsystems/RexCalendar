package me.rexsystems.rexCalendar.command;

import me.rexsystems.rexCalendar.RexCalendar;
import me.rexsystems.rexCalendar.util.MessageService;
import me.rexsystems.rexCalendar.util.Notices;
import me.rexsystems.rexCalendar.util.Placeholders;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class CalendarCommand implements CommandExecutor, TabCompleter {

    private final RexCalendar plugin;

    public CalendarCommand(RexCalendar plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            return help(sender);
        }
        return switch (args[0].toLowerCase(Locale.ROOT)) {
            case "reload" -> reload(sender);
            case "open" -> open(sender);
            default -> help(sender);
        };
    }

    private boolean help(CommandSender sender) {
        Notices.send(plugin, sender, "help-header", Map.of());
        Player player = sender instanceof Player online ? online : null;
        for (String line : plugin.getConfigManager().current().helpLines()) {
            MessageService.send(sender, Placeholders.apply(player, line));
        }
        return true;
    }

    private boolean reload(CommandSender sender) {
        if (!sender.hasPermission("rexcalendar.reload")) {
            Notices.send(plugin, sender, "no-permission", Map.of());
            return true;
        }
        plugin.reloadPlugin();
        Notices.send(plugin, sender, "reload", Map.of());
        return true;
    }

    private boolean open(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            Notices.send(plugin, sender, "players-only", Map.of());
            return true;
        }
        if (!player.hasPermission("rexcalendar.use") && !player.hasPermission("rexcalendar.admin")) {
            Notices.send(plugin, sender, "no-permission", Map.of());
            return true;
        }
        plugin.openCalendar(player, 0);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return filter(List.of("help", "open", "reload"), args[0]);
        }
        return List.of();
    }

    private static List<String> filter(List<String> options, String prefix) {
        String needle = prefix == null ? "" : prefix.toLowerCase(Locale.ROOT);
        List<String> matches = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase(Locale.ROOT).startsWith(needle)) {
                matches.add(option);
            }
        }
        return matches;
    }
}
