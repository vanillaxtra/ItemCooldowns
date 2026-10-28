package net.mattwhyy.itemcooldowns.features.gui;

import java.util.ArrayList;
import java.util.List;
import net.mattwhyy.itemcooldowns.Messages;
import net.mattwhyy.itemcooldowns.features.cooldown.CooldownRule;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public final class CooldownPresets {
    private static final int DEFAULT = 5;

    private CooldownPresets() {
    }

    public record Preset(String nameKey, ItemStack icon, int seconds, CooldownRule.Group group, String extraLoreKey) {
        public CooldownRule rule() {
            if (group != CooldownRule.Group.NONE) {
                return CooldownRule.group(group, seconds);
            }
            return CooldownRule.fromItem(icon, true, false, false, seconds);
        }
    }

    public static List<Preset> all() {
        List<Preset> presets = new ArrayList<>();
        add(presets, Messages.PRESET_ENDER_PEARL, "ENDER_PEARL", DEFAULT, null);
        add(presets, Messages.PRESET_TRIDENT, "TRIDENT", DEFAULT, null);
        add(presets, Messages.PRESET_WIND_CHARGE, "WIND_CHARGE", DEFAULT, null);
        add(presets, Messages.PRESET_MACE, "MACE", DEFAULT, null);
        group(presets, Messages.PRESET_SPEAR, Messages.PRESET_SPEAR_LORE, CooldownRule.Group.SPEAR, DEFAULT);
        add(presets, Messages.PRESET_CHORUS, "CHORUS_FRUIT", DEFAULT, null);
        add(presets, Messages.PRESET_FIREWORK, "FIREWORK_ROCKET", DEFAULT, null);
        add(presets, Messages.PRESET_GOLDEN_APPLE, "GOLDEN_APPLE", DEFAULT, null);
        add(presets, Messages.PRESET_ENCHANTED_APPLE, "ENCHANTED_GOLDEN_APPLE", DEFAULT, null);
        add(presets, Messages.PRESET_TOTEM, "TOTEM_OF_UNDYING", DEFAULT, null);
        group(presets, Messages.PRESET_TURTLE, Messages.PRESET_TURTLE_LORE, CooldownRule.Group.TURTLE_MASTER, 10);
        group(presets, Messages.PRESET_HEALING, Messages.PRESET_HEALING_LORE, CooldownRule.Group.INSTANT_HEALTH, DEFAULT);
        return presets;
    }

    private static void group(
            List<Preset> presets,
            String nameKey,
            String extra,
            CooldownRule.Group group,
            int seconds) {
        CooldownRule rule = CooldownRule.group(group, seconds);
        presets.add(new Preset(nameKey, rule.icon(), seconds, group, extra));
    }

    private static void add(List<Preset> presets, String nameKey, String material, int seconds, String extra) {
        Material type = Material.matchMaterial(material);
        if (type == null || type.isAir()) {
            return;
        }
        presets.add(new Preset(nameKey, new ItemStack(type), seconds, CooldownRule.Group.NONE, extra));
    }
}
