package me.rexsystems.rexCalendar.config;

import java.util.List;

public final class DisplayItem {

    private final int slot;
    private final String material;
    private final String name;
    private final List<String> lore;
    private final int customModelData;

    public DisplayItem(int slot, String material, String name, List<String> lore, int customModelData) {
        this.slot = slot;
        this.material = material == null ? "" : material;
        this.name = name == null ? "" : name;
        this.lore = lore == null ? List.of() : List.copyOf(lore);
        this.customModelData = customModelData;
    }

    public int slot() {
        return slot;
    }

    public String material() {
        return material;
    }

    public String name() {
        return name;
    }

    public List<String> lore() {
        return lore;
    }

    public int customModelData() {
        return customModelData;
    }
}
