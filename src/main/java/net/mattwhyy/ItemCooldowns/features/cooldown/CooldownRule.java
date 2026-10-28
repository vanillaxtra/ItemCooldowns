package net.mattwhyy.itemcooldowns.features.cooldown;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import net.mattwhyy.itemcooldowns.Messages;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.potion.PotionType;

public final class CooldownRule {
    public enum Group {
        NONE,
        SPEAR,
        TURTLE_MASTER,
        INSTANT_HEALTH;

        public boolean locked() {
            return this != NONE;
        }
    }

    private static final List<String> SPEAR_KEYS = List.of(
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

    private final String id;
    private final Material material;
    private final PotionType potion;
    private final Integer cmd;
    private final NamespacedKey itemModel;
    private final int leftSeconds;
    private final int rightSeconds;
    private final Group group;
    private final Set<String> enabled;
    private final boolean active;

    public CooldownRule(
            String id,
            Material material,
            PotionType potion,
            Integer cmd,
            NamespacedKey itemModel,
            int leftSeconds,
            int rightSeconds) {
        this(id, material, potion, cmd, itemModel, leftSeconds, rightSeconds, Group.NONE, Set.of());
    }

    public CooldownRule(
            String id,
            Material material,
            PotionType potion,
            Integer cmd,
            NamespacedKey itemModel,
            int leftSeconds,
            int rightSeconds,
            Group group,
            Set<String> enabled) {
        this(id, material, potion, cmd, itemModel, leftSeconds, rightSeconds, group, enabled, true);
    }

    public CooldownRule(
            String id,
            Material material,
            PotionType potion,
            Integer cmd,
            NamespacedKey itemModel,
            int leftSeconds,
            int rightSeconds,
            Group group,
            Set<String> enabled,
            boolean active) {
        this.id = id == null || id.isBlank() ? UUID.randomUUID().toString() : id;
        this.material = material;
        this.potion = potion;
        this.cmd = cmd;
        this.itemModel = itemModel;
        this.leftSeconds = Math.max(0, leftSeconds);
        this.rightSeconds = Math.max(0, rightSeconds);
        this.group = group == null ? Group.NONE : group;
        this.enabled = this.group == Group.NONE
                ? Set.of()
                : this.group.locked() ? Set.copyOf(groupKeys(this.group)) : Set.copyOf(enabled);
        this.active = active;
    }

    public static CooldownRule fromItem(
            ItemStack stack,
            boolean matchPotion,
            boolean matchCmd,
            boolean matchModel,
            int seconds) {
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
                leftOf(print.material(), seconds),
                rightOf(print.material(), seconds));
    }

    public static CooldownRule group(Group group, int seconds) {
        Material icon = groupIcon(group);
        return new CooldownRule(
                null,
                icon,
                groupPotion(group),
                null,
                null,
                leftOf(icon, seconds),
                rightOf(icon, seconds),
                group,
                new LinkedHashSet<>(groupKeys(group)));
    }

    public static boolean spear(Material material) {
        return material.name().endsWith("_SPEAR");
    }

    public static boolean lungeOnly(Material material) {
        return spear(material);
    }

    public static boolean interactable(Material material) {
        return !leftOnly(material) && !lungeOnly(material);
    }

    public static int leftOf(Material material, int seconds) {
        if (lungeOnly(material) || interactable(material)) {
            return 0;
        }
        return seconds;
    }

    public static int rightOf(Material material, int seconds) {
        if (lungeOnly(material) || interactable(material)) {
            return seconds;
        }
        return 0;
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

    public Group group() {
        return group;
    }

    public Set<String> enabled() {
        return enabled;
    }

    public boolean active() {
        return active;
    }

    public CooldownRule withActive(boolean active) {
        return new CooldownRule(
                id, material, potion, cmd, itemModel, leftSeconds, rightSeconds, group, enabled, active);
    }

    public int seconds() {
        if (lungeOnly(material) || group == Group.SPEAR) {
            return rightSeconds > 0 ? rightSeconds : leftSeconds;
        }
        if (interactable(material)) {
            return rightSeconds > 0 ? rightSeconds : leftSeconds;
        }
        return leftSeconds > 0 ? leftSeconds : rightSeconds;
    }

    public int seconds(ClickSide side) {
        if (lungeOnly(material) || group == Group.SPEAR) {
            return side == ClickSide.LUNGE ? seconds() : 0;
        }
        return side == ClickSide.LEFT ? leftSeconds : rightSeconds;
    }

    public CooldownRule normalized() {
        int value = seconds();
        return new CooldownRule(
                id,
                material,
                potion,
                cmd,
                itemModel,
                leftOf(material, value),
                rightOf(material, value),
                group,
                enabled,
                active);
    }

    public boolean grouped() {
        return group != Group.NONE;
    }

    public boolean specific() {
        return grouped() || potion != null || cmd != null || itemModel != null;
    }

    public CooldownMatcher.Filter filter() {
        return new CooldownMatcher.Filter(
                material.name(),
                potion == null ? null : potion.name(),
                cmd,
                itemModel == null ? null : itemModel.asString());
    }

    public List<CooldownMatcher.Filter> options() {
        if (!grouped()) {
            return List.of(filter());
        }
        List<CooldownMatcher.Filter> options = new ArrayList<>();
        for (String key : enabled) {
            options.add(parseKey(key));
        }
        return options;
    }

    public int score(CooldownMatcher.Probe probe) {
        return CooldownMatcher.scoreAny(options(), probe);
    }

    public boolean sameIdentity(CooldownRule other) {
        if (other == null) {
            return false;
        }
        if (grouped() || other.grouped()) {
            return group == other.group;
        }
        return CooldownMatcher.sameFilter(filter(), other.filter());
    }

    public String groupNameKey() {
        return switch (group) {
            case SPEAR -> Messages.PRESET_SPEAR;
            case TURTLE_MASTER -> Messages.PRESET_TURTLE;
            case INSTANT_HEALTH -> Messages.PRESET_HEALING;
            case NONE -> null;
        };
    }

    public static List<String> groupKeys(Group group) {
        return switch (group) {
            case SPEAR -> availableSpears();
            case TURTLE_MASTER -> potionKeys(
                    PotionType.TURTLE_MASTER,
                    PotionType.LONG_TURTLE_MASTER,
                    PotionType.STRONG_TURTLE_MASTER);
            case INSTANT_HEALTH -> potionKeys(PotionType.HEALING, PotionType.STRONG_HEALING);
            case NONE -> List.of();
        };
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
            component.setFloats(List.of(cmd.floatValue()));
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

    public static ItemStack optionIcon(String key) {
        CooldownMatcher.Filter filter = parseKey(key);
        Material material = materialOf(filter.material());
        if (material == null) {
            return null;
        }
        ItemStack stack = new ItemStack(material);
        PotionType type = ItemFingerprint.potionOf(filter.potion());
        if (type != null && stack.getItemMeta() instanceof PotionMeta meta) {
            meta.setBasePotionType(type);
            stack.setItemMeta(meta);
        }
        return stack;
    }

    public static CooldownMatcher.Filter parseKey(String key) {
        int split = key.indexOf(':');
        if (split < 0) {
            return new CooldownMatcher.Filter(key, null, null, null);
        }
        return new CooldownMatcher.Filter(key.substring(0, split), key.substring(split + 1), null, null);
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
        return material == Material.TRIDENT;
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
                || material == Material.ENDER_EYE
                || material == Material.TOTEM_OF_UNDYING;
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

    public static Group groupOf(String raw) {
        if (raw == null || raw.isBlank()) {
            return Group.NONE;
        }
        try {
            return Group.valueOf(raw.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return Group.NONE;
        }
    }

    private static Material groupIcon(Group group) {
        return switch (group) {
            case SPEAR -> spearIcon();
            case TURTLE_MASTER, INSTANT_HEALTH -> Material.POTION;
            case NONE -> Material.BARRIER;
        };
    }

    private static PotionType groupPotion(Group group) {
        return switch (group) {
            case TURTLE_MASTER -> PotionType.TURTLE_MASTER;
            case INSTANT_HEALTH -> PotionType.STRONG_HEALING;
            default -> null;
        };
    }

    private static Material spearIcon() {
        Material netherite = materialOf("NETHERITE_SPEAR");
        if (netherite != null) {
            return netherite;
        }
        for (String key : SPEAR_KEYS) {
            Material material = materialOf(key);
            if (material != null) {
                return material;
            }
        }
        return Material.TRIDENT;
    }

    private static List<String> availableSpears() {
        List<String> keys = new ArrayList<>();
        for (String key : SPEAR_KEYS) {
            if (materialOf(key) != null) {
                keys.add(key);
            }
        }
        return keys;
    }

    private static List<String> potionKeys(PotionType... types) {
        List<String> keys = new ArrayList<>();
        for (PotionType type : types) {
            for (Material bottle : BOTTLES) {
                keys.add(bottle.name() + ":" + type.name());
            }
        }
        return keys;
    }
}
