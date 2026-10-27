package net.mattwhyy.itemcooldowns.features.cooldown;

import java.util.List;
import java.util.Locale;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.potion.PotionType;

public final class ItemFingerprint {
    private final Material material;
    private final PotionType potion;
    private final Integer cmd;
    private final NamespacedKey itemModel;

    private ItemFingerprint(Material material, PotionType potion, Integer cmd, NamespacedKey itemModel) {
        this.material = material;
        this.potion = potion;
        this.cmd = cmd;
        this.itemModel = itemModel;
    }

    public static ItemFingerprint of(ItemStack stack) {
        if (stack == null || stack.getType().isAir()) {
            return null;
        }
        ItemMeta meta = stack.getItemMeta();
        PotionType potion = null;
        Integer cmd = null;
        NamespacedKey model = null;
        if (meta instanceof PotionMeta potionMeta && potionMeta.hasBasePotionType()) {
            potion = potionMeta.getBasePotionType();
        }
        if (meta != null) {
            cmd = readCmd(meta);
            if (meta.hasItemModel()) {
                model = meta.getItemModel();
            }
        }
        return new ItemFingerprint(stack.getType(), potion, cmd, model);
    }

    public static Integer readCmd(ItemMeta meta) {
        if (meta.hasCustomModelDataComponent()) {
            CustomModelDataComponent component = meta.getCustomModelDataComponent();
            List<Float> floats = component.getFloats();
            if (!floats.isEmpty()) {
                return Math.round(floats.getFirst());
            }
        }
        if (meta.hasCustomModelData()) {
            return meta.getCustomModelData();
        }
        return null;
    }

    public Material material() {
        return material;
    }

    public PotionType potion() {
        return potion;
    }

    public Integer cmd() {
        return cmd;
    }

    public NamespacedKey itemModel() {
        return itemModel;
    }

    public CooldownMatcher.Probe probe() {
        return new CooldownMatcher.Probe(
                material.name(),
                potion == null ? null : potion.name(),
                cmd,
                itemModel == null ? null : itemModel.asString());
    }

    public static PotionType potionOf(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        try {
            return PotionType.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    public static NamespacedKey modelOf(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return NamespacedKey.fromString(raw);
    }
}
