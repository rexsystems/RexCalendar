package me.rexsystems.rexCalendar.gui;

import me.rexsystems.rexCalendar.RexCalendar;
import me.rexsystems.rexCalendar.claim.ClaimAccess;
import me.rexsystems.rexCalendar.claim.DayKeys;
import me.rexsystems.rexCalendar.config.DayDefinition;
import me.rexsystems.rexCalendar.config.DisplayItem;
import me.rexsystems.rexCalendar.config.LoadedConfig;
import me.rexsystems.rexCalendar.util.Placeholders;
import me.rexsystems.rexCalendar.util.TextParser;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class CalendarMenu {

    private final RexCalendar plugin;
    private final ItemFactory items;

    public CalendarMenu(RexCalendar plugin) {
        this.plugin = plugin;
        this.items = plugin.getClaimService().items();
    }

    public void open(Player player, int page) {
        LoadedConfig config = plugin.getConfigManager().current();
        LocalDate today = DayKeys.today(config.zone(), Instant.now());
        Set<String> claimed;
        try {
            claimed = plugin.getDatabase().claimedBetween(
                    player.getUniqueId(),
                    DayKeys.monthStart(today),
                    DayKeys.nextMonthStart(today));
        } catch (SQLException failure) {
            plugin.getLogger().warning("Could not read claims for " + player.getUniqueId() + ": " + failure.getMessage());
            claimed = Set.of();
        }
        int pages = Math.max(1, config.pages());
        int safe = Math.max(0, Math.min(page, pages - 1));
        player.openInventory(create(player, safe, claimed, today));
    }

    public ItemStack dayIcon(Player player, DayDefinition day, boolean claimed, LocalDate today, int page) {
        LoadedConfig config = plugin.getConfigManager().current();
        String date = DayKeys.keyForDoor(today, day.day());
        ClaimAccess access = date == null
                ? ClaimAccess.TOO_EARLY
                : config.missedDays().access(day.day(), today.getDayOfMonth());
        return dayIcon(player, day, claimed, access, date == null ? "" : date, page);
    }

    public ItemStack dayIcon(Player player, DayDefinition day, boolean claimed, ClaimAccess access, String date, int page) {
        LoadedConfig config = plugin.getConfigManager().current();
        String material = materialFor(config, day, claimed, access);
        String status = statusFor(config, claimed, access);
        String name = day.name().isBlank() ? config.dayName() : day.name();
        name = name.replace("{status}", status);
        List<String> source = day.lore().isEmpty() ? config.dayLore() : day.lore();
        List<String> lore = new ArrayList<>();
        for (String line : source) {
            lore.add(line.replace("{status}", status));
        }
        Map<String, String> tokens = tokens(player, day.day(), date, page);
        return items.display(material, "PAPER", name, lore, 0, player, tokens);
    }

    private Inventory create(Player player, int page, Set<String> claimed, LocalDate today) {
        LoadedConfig config = plugin.getConfigManager().current();
        CalendarHolder holder = new CalendarHolder(page);
        Map<String, String> tokens = tokens(player, today.getDayOfMonth(), today.toString(), page);
        Component title = TextParser.parse(Placeholders.apply(player, TextParser.apply(config.guiTitle(), tokens)));
        Inventory inventory = Bukkit.createInventory(holder, config.guiSlots(), title);
        holder.bind(inventory);
        ItemStack filler = items.display(config.fillerMaterial(), "GRAY_STAINED_GLASS_PANE", config.fillerName(), List.of(), 0, player, tokens);
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            inventory.setItem(slot, filler);
        }
        paintDecorations(inventory, config, player, tokens, page);
        for (DayDefinition day : config.days().values()) {
            if (day.slot() < 0 || day.slot() / config.guiSlots() != page) {
                continue;
            }
            String key = DayKeys.keyForDoor(today, day.day());
            boolean already = key != null && claimed.contains(key);
            inventory.setItem(day.slot() % config.guiSlots(), dayIcon(player, day, already, today, page));
        }
        paintNav(inventory, config.previous(), player, tokens);
        paintNav(inventory, config.next(), player, tokens);
        paintNav(inventory, config.closeButton(), player, tokens);
        return inventory;
    }

    private void paintDecorations(Inventory inventory, LoadedConfig config, Player player, Map<String, String> tokens, int page) {
        int size = inventory.getSize();
        for (DisplayItem item : config.decorations()) {
            if (item.slot() < 0 || item.slot() / size != page) {
                continue;
            }
            inventory.setItem(item.slot() % size, items.display(item.material(), "PAPER", item.name(), item.lore(), item.customModelData(), player, tokens));
        }
    }

    private void paintNav(Inventory inventory, DisplayItem item, Player player, Map<String, String> tokens) {
        if (item.slot() < 0 || item.slot() >= inventory.getSize()) {
            return;
        }
        inventory.setItem(item.slot(), items.display(item.material(), "ARROW", item.name(), item.lore(), item.customModelData(), player, tokens));
    }

    private static String materialFor(LoadedConfig config, DayDefinition day, boolean claimed, ClaimAccess access) {
        if (claimed) {
            return config.claimedMaterial();
        }
        return switch (access) {
            case OPEN -> day.material().isBlank() ? config.availableMaterial() : day.material();
            case MISSED -> config.missedMaterial();
            case TOO_EARLY -> config.lockedMaterial();
        };
    }

    private static String statusFor(LoadedConfig config, boolean claimed, ClaimAccess access) {
        if (claimed) {
            return config.status("claimed", "<gray>Claimed");
        }
        return switch (access) {
            case OPEN -> config.status("open", "<green>Click to claim");
            case MISSED -> config.status("missed", "<red>Missed");
            case TOO_EARLY -> config.status("locked", "<gray>Not open yet");
        };
    }

    private static Map<String, String> tokens(Player player, int day, String date, int page) {
        return Map.of(
                "player", player.getName(),
                "uuid", player.getUniqueId().toString(),
                "day", Integer.toString(day),
                "date", date == null ? "" : date,
                "page", Integer.toString(page + 1)
        );
    }
}
