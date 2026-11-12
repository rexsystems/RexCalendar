package me.rexsystems.rexCalendar.config;

import java.util.List;

public final class DayDefinition {

    private final int day;
    private final int slot;
    private final String material;
    private final String name;
    private final List<String> lore;
    private final List<RewardDefinition> rewards;

    public DayDefinition(int day, int slot, String material, String name, List<String> lore, List<RewardDefinition> rewards) {
        this.day = day;
        this.slot = slot;
        this.material = material == null ? "" : material;
        this.name = name == null ? "" : name;
        this.lore = lore == null ? List.of() : List.copyOf(lore);
        this.rewards = rewards == null ? List.of() : List.copyOf(rewards);
    }

    public int day() {
        return day;
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

    public List<RewardDefinition> rewards() {
        return rewards;
    }
}
