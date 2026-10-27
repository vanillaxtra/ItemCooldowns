package net.mattwhyy.itemcooldowns.features.cooldown;

import java.util.Locale;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.potion.PotionType;

public final class CooldownRule {
    private final String id;
    private final Material material;
    private final PotionType potion;
    private final Integer cmd;
    private final NamespacedKey itemModel;
    private final int leftSeconds;
    private final int rightSeconds;

    public CooldownRule(
            String id,
            Material material,
            PotionType potion,
            Integer cmd,
            NamespacedKey itemModel,
            int leftSeconds,
            int rightSeconds) {
        this.id = id == null || id.isBlank() ? UUID.randomUUID().toString() : id;
        this.material = material;
        this.potion = potion;
        this.cmd = cmd;
        this.itemModel = itemModel;
        this.leftSeconds = Math.max(0, leftSeconds);
        this.rightSeconds = Math.max(0, rightSeconds);
    }

    public static CooldownRule fromItem(
            ItemStack stack,
            boolean matchPotion,
            boolean matchCmd,
            boolean matchModel,
            int leftSeconds,
            int rightSeconds) {
        ItemFingerprint print = ItemFingerprint.of(stack);
        if (print == null) {
            return null;
        }
        return new CooldownRule(
                UUID.randomUUID().toString(),
                print.material(),
                matchPotion ? print.potion() : null,
                matchCmd ? print.cmd() : null,
                matchModel ? print.itemModel() : null,
                leftSeconds,
                rightSeconds);
    }

    public String id() {
        return id;
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

    public int leftSeconds() {
        return leftSeconds;
    }

    public int rightSeconds() {
        return rightSeconds;
    }

    public int seconds(ClickSide side) {
        return side == ClickSide.LEFT ? leftSeconds : rightSeconds;
    }

    public boolean specific() {
        return potion != null || cmd != null || itemModel != null;
    }

    public CooldownMatcher.Filter filter() {
        return new CooldownMatcher.Filter(
                material.name(),
                potion == null ? null : potion.name(),
                cmd,
                itemModel == null ? null : itemModel.asString());
    }

    public ItemStack icon() {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        if (meta instanceof PotionMeta potionMeta && potion != null) {
            potionMeta.setBasePotionType(potion);
            meta = potionMeta;
        }
        if (meta != null && cmd != null) {
            CustomModelDataComponent component = meta.getCustomModelDataComponent();
            component.setFloats(java.util.List.of(cmd.floatValue()));
            meta.setCustomModelDataComponent(component);
        }
        if (meta != null && itemModel != null) {
            meta.setItemModel(itemModel);
        }
        if (meta != null) {
            stack.setItemMeta(meta);
        }
        return stack;
    }

    public static boolean meleeDefault(Material material) {
        String name = material.name();
        return name.endsWith("_SWORD")
                || name.endsWith("_AXE")
                || name.endsWith("_PICKAXE")
                || name.endsWith("_SHOVEL")
                || name.endsWith("_HOE")
                || name.contains("MACE")
                || name.endsWith("_SPEAR");
    }

    public static boolean bothDefault(Material material) {
        return material == Material.TRIDENT || material.name().endsWith("_SPEAR");
    }

    public static boolean rightOnly(Material material) {
        return material.isEdible()
                || material == Material.POTION
                || material == Material.SPLASH_POTION
                || material == Material.LINGERING_POTION
                || material == Material.MILK_BUCKET
                || material == Material.HONEY_BOTTLE
                || material == Material.ENDER_PEARL
                || material == Material.WIND_CHARGE
                || material == Material.FIREWORK_ROCKET
                || material == Material.EGG
                || material == Material.SNOWBALL
                || material == Material.EXPERIENCE_BOTTLE
                || material == Material.ENDER_EYE;
    }

    public static boolean leftOnly(Material material) {
        return meleeDefault(material) && !bothDefault(material);
    }

    public static boolean showLeft(Material material) {
        return !rightOnly(material);
    }

    public static boolean showRight(Material material) {
        return !leftOnly(material);
    }

    public static Material materialOf(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        return Material.matchMaterial(name.toUpperCase(Locale.ROOT));
    }
}
