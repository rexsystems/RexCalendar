package me.rexsystems.rexCalendar.feedback;

import me.rexsystems.rexCalendar.RexCalendar;
import me.rexsystems.rexCalendar.config.LoadedConfig;
import me.rexsystems.rexCalendar.util.Placeholders;
import me.rexsystems.rexCalendar.util.SchedulerUtils;
import me.rexsystems.rexCalendar.util.TextParser;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ClaimFeedback {

    private final RexCalendar plugin;
    private final Set<String> unknownSounds = ConcurrentHashMap.newKeySet();
    private final Map<UUID, BossBar> bars = new ConcurrentHashMap<>();

    public ClaimFeedback(RexCalendar plugin) {
        this.plugin = plugin;
    }

    public void show(Player player, Map<String, String> tokens) {
        LoadedConfig config = plugin.getConfigManager().current();
        playSound(player, config);
        showTitle(player, config, tokens);
        showActionbar(player, config, tokens);
        showBossbar(player, config, tokens);
    }

    public void clear(Player player) {
        if (player == null) {
            return;
        }
        BossBar bar = bars.remove(player.getUniqueId());
        if (bar != null) {
            player.hideBossBar(bar);
        }
    }

    public void clear() {
        for (Map.Entry<UUID, BossBar> entry : bars.entrySet()) {
            Player player = plugin.getServer().getPlayer(entry.getKey());
            if (player != null) {
                player.hideBossBar(entry.getValue());
            }
        }
        bars.clear();
    }

    private void playSound(Player player, LoadedConfig config) {
        if (!config.soundEnabled()) {
            return;
        }
        String name = config.soundName();
        if (name == null || name.isBlank()) {
            return;
        }
        Sound sound;
        try {
            sound = Sound.valueOf(name.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            if (unknownSounds.add(name)) {
                plugin.getLogger().warning("Unknown sound '" + name + "'. Check sounds.sound in config.yml.");
            }
            return;
        }
        player.playSound(player.getLocation(), sound, config.soundVolume(), config.soundPitch());
    }

    private void showTitle(Player player, LoadedConfig config, Map<String, String> tokens) {
        if (!config.titleEnabled()) {
            return;
        }
        Component title = render(player, config.titleText(), tokens);
        Component subtitle = render(player, config.subtitleText(), tokens);
        if (Component.empty().equals(title) && Component.empty().equals(subtitle)) {
            return;
        }
        Title.Times times = Title.Times.times(
                Duration.ofMillis(config.titleFadeIn() * 50L),
                Duration.ofMillis(config.titleStay() * 50L),
                Duration.ofMillis(config.titleFadeOut() * 50L));
        player.showTitle(Title.title(title, subtitle, times));
    }

    private void showActionbar(Player player, LoadedConfig config, Map<String, String> tokens) {
        if (!config.actionbarEnabled()) {
            return;
        }
        Component line = render(player, config.actionbarText(), tokens);
        if (Component.empty().equals(line)) {
            return;
        }
        player.sendActionBar(line);
        int ticks = config.actionbarTicks();
        if (ticks > 0) {
            SchedulerUtils.runForPlayerLater(plugin, player, () -> player.sendActionBar(Component.empty()), ticks);
        }
    }

    private void showBossbar(Player player, LoadedConfig config, Map<String, String> tokens) {
        if (!config.bossbarEnabled()) {
            return;
        }
        Component line = render(player, config.bossbarText(), tokens);
        if (Component.empty().equals(line)) {
            return;
        }
        BossBar bar = BossBar.bossBar(line, 1.0f, color(config.bossbarColor()), overlay(config.bossbarStyle()));
        BossBar previous = bars.put(player.getUniqueId(), bar);
        if (previous != null) {
            player.hideBossBar(previous);
        }
        player.showBossBar(bar);
        long ticks = config.bossbarSeconds() * 20L;
        SchedulerUtils.runForPlayerLater(plugin, player, () -> {
            if (bars.remove(player.getUniqueId(), bar)) {
                player.hideBossBar(bar);
            }
        }, ticks);
    }

    private static Component render(Player player, String input, Map<String, String> tokens) {
        if (input == null || input.isBlank()) {
            return Component.empty();
        }
        return TextParser.parse(Placeholders.apply(player, TextParser.apply(input, tokens)));
    }

    private static BossBar.Color color(String raw) {
        if (raw == null || raw.isBlank()) {
            return BossBar.Color.YELLOW;
        }
        try {
            return BossBar.Color.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return BossBar.Color.YELLOW;
        }
    }

    private static BossBar.Overlay overlay(String raw) {
        if (raw == null) {
            return BossBar.Overlay.PROGRESS;
        }
        return switch (raw.trim().toUpperCase(Locale.ROOT)) {
            case "SEGMENTED_6", "NOTCHED_6" -> BossBar.Overlay.NOTCHED_6;
            case "SEGMENTED_10", "NOTCHED_10" -> BossBar.Overlay.NOTCHED_10;
            case "SEGMENTED_12", "NOTCHED_12" -> BossBar.Overlay.NOTCHED_12;
            case "SEGMENTED_20", "NOTCHED_20" -> BossBar.Overlay.NOTCHED_20;
            default -> BossBar.Overlay.PROGRESS;
        };
    }
}
