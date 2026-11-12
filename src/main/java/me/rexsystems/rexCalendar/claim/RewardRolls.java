package me.rexsystems.rexCalendar.claim;

import me.rexsystems.rexCalendar.config.RewardDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleSupplier;

public final class RewardRolls {

    private RewardRolls() {
    }

    public static List<RewardDefinition> roll(List<RewardDefinition> rewards, DoubleSupplier random) {
        List<RewardDefinition> won = new ArrayList<>();
        if (rewards == null || rewards.isEmpty()) {
            return won;
        }
        DoubleSupplier source = random == null ? () -> 0.0 : random;
        for (RewardDefinition reward : rewards) {
            if (reward == null) {
                continue;
            }
            double chance = reward.chance();
            if (chance >= 100.0) {
                won.add(reward);
                continue;
            }
            if (chance <= 0.0) {
                continue;
            }
            if (source.getAsDouble() * 100.0 < chance) {
                won.add(reward);
            }
        }
        return won;
    }
}
