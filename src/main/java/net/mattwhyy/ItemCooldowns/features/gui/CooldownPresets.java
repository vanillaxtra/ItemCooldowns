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

    public record Preset(String nameKey, ItemStack icon, int left, int right, CooldownRule.Group group, String extraLoreKey) {
        public CooldownRule rule() {
            if (group != CooldownRule.Group.NONE) {
                return CooldownRule.group(group, left, right);
            }
            return CooldownRule.fromItem(icon, true, false, false, left, right);
        }
    }

    public static List<Preset> all() {
        List<Preset> presets = new ArrayList<>();
        use(presets, Messages.PRESET_ENDER_PEARL, "ENDER_PEARL");
        both(presets, Messages.PRESET_TRIDENT, "TRIDENT");
        use(presets, Messages.PRESET_WIND_CHARGE, "WIND_CHARGE");
        melee(presets, Messages.PRESET_MACE, "MACE");
        group(presets, Messages.PRESET_SPEAR, Messages.PRESET_SPEAR_LORE, CooldownRule.Group.SPEAR, DEFAULT, DEFAULT);
        use(presets, Messages.PRESET_CHORUS, "CHORUS_FRUIT");
        use(presets, Messages.PRESET_FIREWORK, "FIREWORK_ROCKET");
        use(presets, Messages.PRESET_GOLDEN_APPLE, "GOLDEN_APPLE");
        use(presets, Messages.PRESET_ENCHANTED_APPLE, "ENCHANTED_GOLDEN_APPLE");
        group(presets, Messages.PRESET_TURTLE, Messages.PRESET_TURTLE_LORE, CooldownRule.Group.TURTLE_MASTER, 0, 10);
        group(presets, Messages.PRESET_HEALING, Messages.PRESET_HEALING_LORE, CooldownRule.Group.INSTANT_HEALTH, 0, DEFAULT);
        return presets;
    }

    private static void use(List<Preset> presets, String nameKey, String material) {
        add(presets, nameKey, material, 0, DEFAULT, CooldownRule.Group.NONE, null);
    }

    private static void melee(List<Preset> presets, String nameKey, String material) {
        add(presets, nameKey, material, DEFAULT, 0, CooldownRule.Group.NONE, null);
    }

    private static void both(List<Preset> presets, String nameKey, String material) {
        add(presets, nameKey, material, DEFAULT, DEFAULT, CooldownRule.Group.NONE, null);
    }

    private static void group(
            List<Preset> presets,
            String nameKey,
            String extra,
            CooldownRule.Group group,
            int left,
            int right) {
        CooldownRule rule = CooldownRule.group(group, left, right);
        presets.add(new Preset(nameKey, rule.icon(), left, right, group, extra));
    }

    private static void add(
            List<Preset> presets,
            String nameKey,
            String material,
            int left,
            int right,
            CooldownRule.Group group,
            String extra) {
        Material type = Material.matchMaterial(material);
        if (type == null || type.isAir()) {
            return;
        }
        presets.add(new Preset(nameKey, new ItemStack(type), left, right, group, extra));
    }
}
