package me.rexsystems.rexCalendar.config;

import java.util.List;

public final class ItemReward {

    private final String material;
    private final int amount;
    private final String name;
    private final List<String> lore;
    private final int customModelData;

    public ItemReward(String material, int amount, String name, List<String> lore, int customModelData) {
        this.material = material == null ? "" : material;
        this.amount = amount;
        this.name = name == null ? "" : name;
        this.lore = lore == null ? List.of() : List.copyOf(lore);
        this.customModelData = customModelData;
    }

    public String material() {
        return material;
    }

    public int amount() {
        return amount;
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
