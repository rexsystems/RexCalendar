package me.rexsystems.rexCalendar.gui;

import me.rexsystems.rexCalendar.RexCalendar;
import me.rexsystems.rexCalendar.claim.ClaimOutcome;
import me.rexsystems.rexCalendar.claim.ClaimResult;
import me.rexsystems.rexCalendar.config.DayDefinition;
import me.rexsystems.rexCalendar.config.DisplayItem;
import me.rexsystems.rexCalendar.config.LoadedConfig;
import me.rexsystems.rexCalendar.util.Notices;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;

public final class CalendarListener implements Listener {

    private final RexCalendar plugin;

    public CalendarListener(RexCalendar plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        Inventory top = event.getView().getTopInventory();
        if (!(top.getHolder() instanceof CalendarHolder holder)) {
            return;
        }
        if (event.getClickedInventory() != top) {
            InventoryAction action = event.getAction();
            if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY || action == InventoryAction.COLLECT_TO_CURSOR) {
                event.setCancelled(true);
            }
            return;
        }
        event.setCancelled(true);
        LoadedConfig config = plugin.getConfigManager().current();
        int slot = event.getSlot();
        if (clicked(config.previous(), slot)) {
            if (holder.page() > 0) {
                plugin.openCalendar(player, holder.page() - 1);
            }
            return;
        }
        if (clicked(config.next(), slot)) {
            if (holder.page() + 1 < config.pages()) {
                plugin.openCalendar(player, holder.page() + 1);
            }
            return;
        }
        if (clicked(config.closeButton(), slot)) {
            player.closeInventory();
            return;
        }
        DayDefinition day = config.dayAt(holder.page(), slot);
        if (day == null) {
            return;
        }
        ClaimOutcome outcome = plugin.getClaimService().claim(player, day.day());
        if (outcome.result() == ClaimResult.PENDING) {
            return;
        }
        Map<String, String> tokens = Map.of(
                "player", player.getName(),
                "uuid", player.getUniqueId().toString(),
                "day", Integer.toString(outcome.day()),
                "date", outcome.date()
        );
        Notices.send(plugin, player, message(outcome.result()), tokens);
        if (outcome.stored() || outcome.result() == ClaimResult.ALREADY) {
            LocalDate today = LocalDate.ofInstant(Instant.now(), config.zone());
            event.getInventory().setItem(slot, plugin.getMenu().dayIcon(player, day, true, today, holder.page()));
        }
        if (outcome.stored() && (outcome.result() == ClaimResult.CLAIMED || outcome.result() == ClaimResult.NO_REWARD)) {
            plugin.getFeedback().show(player, tokens);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDrag(InventoryDragEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (!(top.getHolder() instanceof CalendarHolder)) {
            return;
        }
        int size = top.getSize();
        for (int slot : event.getRawSlots()) {
            if (slot < size) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getFeedback().clear(event.getPlayer());
    }

    private static boolean clicked(DisplayItem button, int slot) {
        return button.slot() >= 0 && button.slot() == slot;
    }

    private static String message(ClaimResult result) {
        return switch (result) {
            case CLAIMED -> "claim";
            case NO_REWARD -> "no-reward";
            case ALREADY -> "already-claimed";
            case TOO_EARLY -> "too-early";
            case MISSED -> "missed";
            case IP_LIMIT -> "ip-limit";
            case INVENTORY_FULL -> "inventory-full";
            case NO_PERMISSION -> "no-permission";
            case CLOSED_WORLD -> "world";
            case FAILED -> "claim-failed";
            case PENDING -> "claim";
        };
    }
}
