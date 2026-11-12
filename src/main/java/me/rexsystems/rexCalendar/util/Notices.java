package me.rexsystems.rexCalendar.util;

import me.rexsystems.rexCalendar.RexCalendar;
import me.rexsystems.rexCalendar.config.LoadedConfig;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public final class Notices {

    private Notices() {
    }

    public static void send(RexCalendar plugin, CommandSender sender, String path, Map<String, String> tokens) {
        if (plugin == null || sender == null || path == null) {
            return;
        }
        LoadedConfig config = plugin.getConfigManager().current();
        Player player = sender instanceof Player online ? online : null;
        String text = TextParser.apply(config.prefixed(path), tokens == null ? Map.of() : tokens);
        MessageService.send(sender, Placeholders.apply(player, text));
    }
}
