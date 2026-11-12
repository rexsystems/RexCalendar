package me.rexsystems.rexCalendar.claim;

import me.rexsystems.rexCalendar.RexCalendar;
import me.rexsystems.rexCalendar.config.DayDefinition;
import me.rexsystems.rexCalendar.config.ItemReward;
import me.rexsystems.rexCalendar.config.LoadedConfig;
import me.rexsystems.rexCalendar.config.RewardDefinition;
import me.rexsystems.rexCalendar.command.ConsoleCommands;
import me.rexsystems.rexCalendar.gui.ItemFactory;
import me.rexsystems.rexCalendar.storage.ClaimWrite;
import me.rexsystems.rexCalendar.gui.CalendarHolder;
import me.rexsystems.rexCalendar.util.Notices;
import me.rexsystems.rexCalendar.util.SchedulerUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public final class ClaimService {

    private final RexCalendar plugin;
    private final ItemFactory items;

    public ClaimService(RexCalendar plugin) {
        this.plugin = plugin;
        this.items = new ItemFactory(plugin);
    }

    public ItemFactory items() {
        return items;
    }

    public ClaimOutcome claim(Player player, int day) {
        LoadedConfig config = plugin.getConfigManager().current();
        if (player.getWorld() == null || !config.worldAllowed(player.getWorld().getName())) {
            return ClaimOutcome.of(ClaimResult.CLOSED_WORLD, false, day, "");
        }
        boolean admin = player.hasPermission("rexcalendar.admin");
        if (!admin && !player.hasPermission("rexcalendar.use")) {
            return ClaimOutcome.of(ClaimResult.NO_PERMISSION, false, day, "");
        }
        LocalDate today = DayKeys.today(config.zone(), Instant.now());
        String dayKey = DayKeys.keyForDoor(today, day);
        if (dayKey == null) {
            return ClaimOutcome.of(ClaimResult.TOO_EARLY, false, day, "");
        }
        ClaimAccess access = config.missedDays().access(day, today.getDayOfMonth());
        if (access == ClaimAccess.TOO_EARLY) {
            return ClaimOutcome.of(ClaimResult.TOO_EARLY, false, day, dayKey);
        }
        if (access == ClaimAccess.MISSED && !admin) {
            return ClaimOutcome.of(ClaimResult.MISSED, false, day, dayKey);
        }
        if (!admin && !player.hasPermission("rexcalendar.day." + day)) {
            return ClaimOutcome.of(ClaimResult.NO_PERMISSION, false, day, dayKey);
        }
        DayDefinition definition = config.day(day);
        if (definition == null || definition.rewards().isEmpty()) {
            return ClaimOutcome.of(ClaimResult.NO_REWARD, false, day, dayKey);
        }

        String playerId = player.getUniqueId().toString();
        if (!plugin.getClaimGate().begin(playerId, dayKey)) {
            return ClaimOutcome.of(ClaimResult.ALREADY, false, day, dayKey);
        }

        boolean stored = false;
        ItemStack[] snapshot = null;
        try {
            if (plugin.getDatabase().hasClaim(player.getUniqueId(), dayKey)) {
                return ClaimOutcome.of(ClaimResult.ALREADY, false, day, dayKey);
            }
            List<RewardDefinition> won = RewardRolls.roll(definition.rewards(), ThreadLocalRandom.current()::nextDouble);
            Map<String, String> tokens = tokens(player, day, dayKey);
            List<ItemReward> wonItems = new ArrayList<>();
            List<String> commands = new ArrayList<>();
            for (RewardDefinition reward : won) {
                wonItems.addAll(reward.items());
                for (String command : reward.commands()) {
                    commands.add(applyCommand(command, player, day, dayKey));
                }
            }
            List<ItemStack> stacks = items.create(wonItems, player, tokens);
            if (!stacks.isEmpty() && !items.fits(player, stacks)) {
                return ClaimOutcome.of(ClaimResult.INVENTORY_FULL, false, day, dayKey);
            }
            String hash = null;
            int limit = config.ipLimit();
            if (limit > 0 && !admin) {
                hash = addressHash(player);
            }
            ClaimWrite write = plugin.getDatabase().insert(
                    player.getUniqueId(),
                    dayKey,
                    hash,
                    admin || hash == null ? 0 : limit);
            if (write == ClaimWrite.DUPLICATE) {
                return ClaimOutcome.of(ClaimResult.ALREADY, false, day, dayKey);
            }
            if (write == ClaimWrite.IP_LIMIT) {
                return ClaimOutcome.of(ClaimResult.IP_LIMIT, false, day, dayKey);
            }
            if (write != ClaimWrite.STORED) {
                return ClaimOutcome.of(ClaimResult.FAILED, false, day, dayKey);
            }
            stored = true;
            snapshot = items.snapshot(player);
            if (!stacks.isEmpty() && !items.give(player, stacks)) {
                items.restore(player, snapshot);
                plugin.getDatabase().delete(player.getUniqueId(), dayKey);
                stored = false;
                return ClaimOutcome.of(ClaimResult.INVENTORY_FULL, false, day, dayKey);
            }
            if (!commands.isEmpty() && !org.bukkit.Bukkit.isPrimaryThread()) {
                ItemStack[] kept = snapshot;
                scheduleCommands(player, day, dayKey, playerId, kept, stacks.isEmpty(), commands);
                return ClaimOutcome.of(ClaimResult.PENDING, true, day, dayKey);
            }
            if (!ConsoleCommands.run(plugin, commands)) {
                items.restore(player, snapshot);
                plugin.getDatabase().delete(player.getUniqueId(), dayKey);
                stored = false;
                return ClaimOutcome.of(ClaimResult.FAILED, false, day, dayKey);
            }
            if (stacks.isEmpty() && commands.isEmpty()) {
                return ClaimOutcome.of(ClaimResult.NO_REWARD, true, day, dayKey);
            }
            return ClaimOutcome.of(ClaimResult.CLAIMED, true, day, dayKey);
        } catch (RuntimeException | java.sql.SQLException failure) {
            plugin.getLogger().warning("Reward failed for " + player.getUniqueId() + " on " + dayKey + ": " + failure.getMessage());
            if (stored && snapshot != null) {
                items.restore(player, snapshot);
            }
            if (stored) {
                try {
                    plugin.getDatabase().delete(player.getUniqueId(), dayKey);
                    stored = false;
                } catch (java.sql.SQLException rollback) {
                    plugin.getLogger().warning("Claim row remains for " + player.getUniqueId() + " on " + dayKey);
                }
            }
            return ClaimOutcome.of(ClaimResult.FAILED, stored, day, dayKey);
        } finally {
            if (!stored) {
                plugin.getClaimGate().abort(playerId, dayKey);
            }
        }
    }

    private void scheduleCommands(Player player, int day, String dayKey, String playerId, ItemStack[] snapshot, boolean noItems, List<String> commands) {
        ConsoleCommands.runLater(plugin, commands, ok -> {
            if (!ok) {
                try {
                    plugin.getDatabase().delete(player.getUniqueId(), dayKey);
                } catch (java.sql.SQLException failure) {
                    plugin.getLogger().warning("Claim row remains for " + player.getUniqueId() + " on " + dayKey);
                }
                plugin.getClaimGate().abort(playerId, dayKey);
            }
            SchedulerUtils.runForPlayer(plugin, player, () -> {
                if (!player.isOnline()) {
                    return;
                }
                Map<String, String> tokens = tokens(player, day, dayKey);
                if (!ok) {
                    if (snapshot != null) {
                        items.restore(player, snapshot);
                    }
                    Notices.send(plugin, player, "claim-failed", tokens);
                    paint(player, day, false);
                    return;
                }
                Notices.send(plugin, player, noItems ? "no-reward" : "claim", tokens);
                plugin.getFeedback().show(player, tokens);
                paint(player, day, true);
            });
        });
    }

    private void paint(Player player, int day, boolean claimed) {
        if (!player.isOnline()) {
            return;
        }
        Inventory top = player.getOpenInventory().getTopInventory();
        if (!(top.getHolder() instanceof CalendarHolder holder)) {
            return;
        }
        LoadedConfig config = plugin.getConfigManager().current();
        DayDefinition definition = config.day(day);
        if (definition == null || definition.slot() < 0 || config.guiSlots() <= 0) {
            return;
        }
        int page = definition.slot() / config.guiSlots();
        int local = definition.slot() % config.guiSlots();
        if (page != holder.page() || local >= top.getSize()) {
            return;
        }
        LocalDate today = DayKeys.today(config.zone(), Instant.now());
        top.setItem(local, plugin.getMenu().dayIcon(player, definition, claimed, today, page));
    }

    private static Map<String, String> tokens(Player player, int day, String date) {
        return Map.of(
                "player", player.getName(),
                "uuid", player.getUniqueId().toString(),
                "day", Integer.toString(day),
                "date", date
        );
    }

    private static String applyCommand(String command, Player player, int day, String date) {
        return command
                .replace("{player}", player.getName())
                .replace("{uuid}", player.getUniqueId().toString())
                .replace("{day}", Integer.toString(day))
                .replace("{date}", date);
    }

    private static String addressHash(Player player) {
        InetSocketAddress socket = player.getAddress();
        if (socket == null) {
            return null;
        }
        InetAddress address = socket.getAddress();
        if (address == null) {
            return null;
        }
        String host = address.getHostAddress();
        if (host == null || host.isEmpty()) {
            return null;
        }
        return IpHashes.sha256Hex(host);
    }
}
