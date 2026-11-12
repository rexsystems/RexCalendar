package me.rexsystems.rexCalendar.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public final class CalendarHolder implements InventoryHolder {

    private final int page;
    private Inventory inventory;

    public CalendarHolder(int page) {
        this.page = page;
    }

    public int page() {
        return page;
    }

    public void bind(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public @NotNull Inventory getInventory() {
        if (inventory == null) {
            throw new IllegalStateException("Calendar inventory is not bound");
        }
        return inventory;
    }
}
