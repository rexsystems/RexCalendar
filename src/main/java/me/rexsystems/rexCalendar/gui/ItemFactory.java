package me.rexsystems.rexCalendar.gui;

import me.rexsystems.rexCalendar.RexCalendar;
import me.rexsystems.rexCalendar.config.ItemReward;
import me.rexsystems.rexCalendar.util.Placeholders;
import me.rexsystems.rexCalendar.util.TextParser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ItemFactory {

    private final RexCalendar plugin;

    public ItemFactory(RexCalendar plugin) {
        this.plugin = plugin;
    }

    public List<ItemStack> create(List<ItemReward> items, Player player, Map<String, String> tokens) {
        List<ItemStack> stacks = new ArrayList<>();
        if (items == null) {
            return stacks;
        }
        for (ItemReward item : items) {
            Material material = Material.matchMaterial(item.material());
            if (material == null || material.isAir()) {
                plugin.warnOnce("material:" + item.material(), "Unknown material '" + item.material() + "'.");
                continue;
            }
            int left = Math.max(1, item.amount());
            int max = Math.max(1, material.getMaxStackSize());
            while (left > 0) {
                int take = Math.min(max, left);
                ItemStack stack = new ItemStack(material, take);
                apply(stack, item.name(), item.lore(), item.customModelData(), player, tokens, false);
                stacks.add(stack);
                left -= take;
            }
        }
        return stacks;
    }

    public ItemStack display(String materialName, String fallback, String name, List<String> lore, int customModelData, Player player, Map<String, String> tokens) {
        Material material = Material.matchMaterial(materialName == null ? "" : materialName);
        if (material == null || material.isAir()) {
            material = Material.matchMaterial(fallback == null ? "" : fallback);
        }
        if (material == null || material.isAir()) {
            material = Material.PAPER;
        }
        ItemStack stack = new ItemStack(material);
        apply(stack, name, lore, customModelData, player, tokens, true);
        return stack;
    }

    public boolean fits(Player player, List<ItemStack> items) {
        if (items == null || items.isEmpty()) {
            return true;
        }
        ItemStack[] contents = player.getInventory().getStorageContents();
        Inventory dummy = Bukkit.createInventory(null, 36);
        ItemStack[] copy = new ItemStack[contents.length];
        for (int i = 0; i < contents.length; i++) {
            copy[i] = contents[i] == null ? null : contents[i].clone();
        }
        dummy.setContents(copy);
        for (ItemStack item : items) {
            if (!dummy.addItem(item.clone()).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public boolean give(Player player, List<ItemStack> items) {
        if (items == null || items.isEmpty()) {
            return true;
        }
        ItemStack[] clones = new ItemStack[items.size()];
        for (int i = 0; i < items.size(); i++) {
            clones[i] = items.get(i).clone();
        }
        return player.getInventory().addItem(clones).isEmpty();
    }

    public ItemStack[] snapshot(Player player) {
        ItemStack[] contents = player.getInventory().getStorageContents();
        ItemStack[] copy = new ItemStack[contents.length];
        for (int i = 0; i < contents.length; i++) {
            copy[i] = contents[i] == null ? null : contents[i].clone();
        }
        return copy;
    }

    public void restore(Player player, ItemStack[] snapshot) {
        if (snapshot == null) {
            return;
        }
        player.getInventory().setStorageContents(snapshot);
    }

    private void apply(ItemStack stack, String name, List<String> lore, int customModelData, Player player, Map<String, String> tokens, boolean hideBlank) {
        boolean named = name != null && !name.isBlank();
        boolean hasLore = lore != null && !lore.isEmpty();
        boolean model = customModelData > 0;
        if (!named && !hasLore && !model && !hideBlank) {
            return;
        }
        stack.editMeta(meta -> {
            if (named) {
                meta.displayName(component(player, name, tokens));
            } else if (hideBlank) {
                meta.displayName(Component.empty());
                meta.setHideTooltip(true);
            }
            if (hasLore) {
                List<Component> lines = new ArrayList<>();
                for (String line : lore) {
                    lines.add(component(player, line, tokens));
                }
                meta.lore(lines);
            }
            if (model) {
                meta.setCustomModelData(customModelData);
            }
        });
    }

    private Component component(Player player, String input, Map<String, String> tokens) {
        String applied = TextParser.apply(input, tokens);
        applied = Placeholders.apply(player, applied);
        return plain(TextParser.parse(applied));
    }

    // A custom name stays italic unless that decoration is cleared.
    private static Component plain(Component component) {
        if (component.decoration(TextDecoration.ITALIC) == TextDecoration.State.NOT_SET) {
            return component.decoration(TextDecoration.ITALIC, false);
        }
        return component;
    }
}
