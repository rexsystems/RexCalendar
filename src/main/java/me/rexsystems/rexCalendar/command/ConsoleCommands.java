package me.rexsystems.rexCalendar.command;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class ConsoleCommands {

    private ConsoleCommands() {
    }

    public static boolean run(Plugin plugin, List<String> commands) {
        List<String> ready = prepare(commands);
        if (ready.isEmpty()) {
            return true;
        }
        if (Bukkit.isPrimaryThread()) {
            return runAll(ready);
        }
        return false;
    }

    public static void runLater(Plugin plugin, List<String> commands, Consumer<Boolean> callback) {
        List<String> ready = prepare(commands);
        if (ready.isEmpty()) {
            callback.accept(true);
            return;
        }
        Bukkit.getGlobalRegionScheduler().run(plugin, task -> callback.accept(runAll(ready)));
    }

    private static List<String> prepare(List<String> commands) {
        List<String> ready = new ArrayList<>();
        if (commands == null) {
            return ready;
        }
        for (String command : commands) {
            if (command == null || command.isBlank()) {
                continue;
            }
            String parsed = command.startsWith("/") ? command.substring(1).trim() : command.trim();
            if (!parsed.isEmpty()) {
                ready.add(parsed);
            }
        }
        return ready;
    }

    private static boolean runAll(List<String> commands) {
        for (String command : commands) {
            try {
                if (!Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command)) {
                    return false;
                }
            } catch (Throwable failure) {
                return false;
            }
        }
        return true;
    }
}
