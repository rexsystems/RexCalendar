package me.rexsystems.rexCalendar.config;

import java.util.List;

public final class RewardDefinition {

    private final double chance;
    private final List<String> commands;
    private final List<ItemReward> items;

    public RewardDefinition(double chance, List<String> commands, List<ItemReward> items) {
        this.chance = chance;
        this.commands = commands == null ? List.of() : List.copyOf(commands);
        this.items = items == null ? List.of() : List.copyOf(items);
    }

    public double chance() {
        return chance;
    }

    public List<String> commands() {
        return commands;
    }

    public List<ItemReward> items() {
        return items;
    }

    public boolean grantsAnything() {
        return !commands.isEmpty() || !items.isEmpty();
    }
}
