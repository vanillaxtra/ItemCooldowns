package net.mattwhyy.itemcooldowns.features.gui;

import java.util.ArrayList;
import java.util.List;
import net.mattwhyy.itemcooldowns.Messages;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

public final class CooldownPresets {
    private static final int DEFAULT = 5;
    private static final List<String> SPEARS = List.of(
            "WOODEN_SPEAR",
            "STONE_SPEAR",
            "COPPER_SPEAR",
            "IRON_SPEAR",
            "GOLDEN_SPEAR",
            "DIAMOND_SPEAR",
            "NETHERITE_SPEAR");
    private static final List<Material> BOTTLES = List.of(
            Material.POTION,
            Material.SPLASH_POTION,
            Material.LINGERING_POTION);

    private CooldownPresets() {
    }

    public record Preset(
            String nameKey,
            ItemStack icon,
            int left,
            int right,
            List<ItemStack> items,
            String extraLoreKey) {
    }

    public static List<Preset> all() {
        List<Preset> presets = new ArrayList<>();
        use(presets, Messages.PRESET_ENDER_PEARL, "ENDER_PEARL");
        both(presets, Messages.PRESET_TRIDENT, "TRIDENT");
        use(presets, Messages.PRESET_WIND_CHARGE, "WIND_CHARGE");
        melee(presets, Messages.PRESET_MACE, "MACE");
        spears(presets);
        use(presets, Messages.PRESET_CHORUS, "CHORUS_FRUIT");
        use(presets, Messages.PRESET_FIREWORK, "FIREWORK_ROCKET");
        use(presets, Messages.PRESET_GOLDEN_APPLE, "GOLDEN_APPLE");
        use(presets, Messages.PRESET_ENCHANTED_APPLE, "ENCHANTED_GOLDEN_APPLE");
        potions(
                presets,
                Messages.PRESET_TURTLE,
                Messages.PRESET_TURTLE_LORE,
                10,
                PotionType.TURTLE_MASTER,
                PotionType.LONG_TURTLE_MASTER,
                PotionType.STRONG_TURTLE_MASTER);
        potions(
                presets,
                Messages.PRESET_HEALING,
                Messages.PRESET_HEALING_LORE,
                DEFAULT,
                PotionType.STRONG_HEALING);
        return presets;
    }

    private static void use(List<Preset> presets, String nameKey, String material) {
        add(presets, nameKey, material, 0, DEFAULT, null);
    }

    private static void melee(List<Preset> presets, String nameKey, String material) {
        add(presets, nameKey, material, DEFAULT, 0, null);
    }

    private static void both(List<Preset> presets, String nameKey, String material) {
        add(presets, nameKey, material, DEFAULT, DEFAULT, null);
    }

    private static void add(List<Preset> presets, String nameKey, String material, int left, int right, String extra) {
        ItemStack item = stack(material);
        if (item == null) {
            return;
        }
        presets.add(new Preset(nameKey, item, left, right, List.of(item), extra));
    }

    private static void spears(List<Preset> presets) {
        List<ItemStack> items = new ArrayList<>();
        ItemStack icon = null;
        for (String name : SPEARS) {
            ItemStack spear = stack(name);
            if (spear == null) {
                continue;
            }
            if (icon == null) {
                icon = spear;
            }
            items.add(spear);
        }
        if (icon == null) {
            return;
        }
        presets.add(new Preset(Messages.PRESET_SPEAR, icon, DEFAULT, DEFAULT, List.copyOf(items), Messages.PRESET_SPEAR_LORE));
    }

    private static void potions(List<Preset> presets, String nameKey, String extra, int seconds, PotionType... types) {
        List<ItemStack> items = new ArrayList<>();
        ItemStack icon = null;
        for (PotionType type : types) {
            for (Material bottle : BOTTLES) {
                ItemStack potion = potion(bottle, type);
                if (potion == null) {
                    continue;
                }
                if (icon == null) {
                    icon = potion;
                }
                items.add(potion);
            }
        }
        if (icon == null) {
            return;
        }
        presets.add(new Preset(nameKey, icon, 0, seconds, List.copyOf(items), extra));
    }

    private static ItemStack stack(String name) {
        Material material = Material.matchMaterial(name);
        if (material == null || material.isAir()) {
            return null;
        }
        return new ItemStack(material);
    }

    private static ItemStack potion(Material bottle, PotionType type) {
        ItemStack stack = new ItemStack(bottle);
        if (!(stack.getItemMeta() instanceof PotionMeta meta)) {
            return null;
        }
        meta.setBasePotionType(type);
        stack.setItemMeta(meta);
        return stack;
    }
}
