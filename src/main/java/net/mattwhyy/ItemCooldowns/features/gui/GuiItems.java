package net.mattwhyy.itemcooldowns.features.gui;

import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.translation.GlobalTranslator;
import net.mattwhyy.itemcooldowns.ItemCooldownsPlugin;
import net.mattwhyy.itemcooldowns.Messages;
import net.mattwhyy.itemcooldowns.features.cooldown.CooldownRule;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public final class GuiItems {
    private GuiItems() {
    }

    public static ItemStack copy(ItemStack stack) {
        if (stack == null || stack.getType().isAir()) {
            return null;
        }
        return stack.clone();
    }

    public static ItemStack asTemplate(ItemCooldownsPlugin plugin, ItemStack stack) {
        ItemStack copy = copy(stack);
        if (copy == null) {
            return null;
        }
        copy.setAmount(1);
        ItemMeta meta = copy.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().remove(key(plugin));
            copy.setItemMeta(meta);
        }
        return copy;
    }

    public static boolean isTagged(ItemCooldownsPlugin plugin, ItemStack stack) {
        if (stack == null || stack.getType().isAir()) {
            return false;
        }
        ItemMeta meta = stack.getItemMeta();
        return meta != null && meta.getPersistentDataContainer().has(key(plugin), PersistentDataType.BYTE);
    }

    public static ItemStack button(ItemCooldownsPlugin plugin, Material material, String nameKey, String... loreKeys) {
        return button(plugin, material, false, nameKey, loreKeys);
    }

    public static ItemStack button(
            ItemCooldownsPlugin plugin,
            Material material,
            boolean glint,
            String nameKey,
            String... loreKeys) {
        List<Component> lore = new ArrayList<>();
        for (String loreKey : loreKeys) {
            lore.add(Messages.text(loreKey));
        }
        return named(plugin, new ItemStack(material), Messages.text(nameKey), lore, glint);
    }

    public static ItemStack named(
            ItemCooldownsPlugin plugin,
            ItemStack stack,
            Component name,
            List<Component> lore,
            boolean glint) {
        ItemStack item = stack.clone();
        item.setAmount(1);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }
        meta.displayName(name);
        if (!lore.isEmpty()) {
            meta.lore(lore);
        }
        if (glint) {
            meta.setEnchantmentGlintOverride(true);
        }
        return store(plugin, item, meta);
    }

    public static ItemStack ruleIcon(ItemCooldownsPlugin plugin, Player player, CooldownRule rule) {
        ItemStack icon = rule.icon();
        List<Component> lore = new ArrayList<>();
        addCooldownLore(lore, rule.material(), rule.seconds());
        if (rule.group().split()) {
            lore.add(Messages.text(
                    Messages.RULE_GROUP_COUNT,
                    "on",
                    String.valueOf(rule.enabled().size()),
                    "total",
                    String.valueOf(CooldownRule.groupKeys(rule.group()).size())));
        } else if (!rule.grouped() && rule.potion() != null) {
            lore.add(Messages.text(Messages.RULE_MATCH_POTION, "value", pretty(rule.potion().name())));
        }
        if (rule.cmd() != null) {
            lore.add(Messages.text(Messages.RULE_MATCH_CMD, "value", String.valueOf(rule.cmd())));
        }
        if (rule.itemModel() != null) {
            lore.add(Messages.text(Messages.RULE_MATCH_MODEL, "value", rule.itemModel().asString()));
        }
        Component name = rule.groupNameKey() == null ? shownName(player, icon) : Messages.text(rule.groupNameKey());
        return named(plugin, icon, name, lore, false);
    }

    public static void addCooldownLore(List<Component> lore, Material material, int seconds) {
        if (seconds <= 0) {
            lore.add(Messages.text(CooldownRule.interactable(material) ? Messages.COOLDOWN_OFF_USE : Messages.COOLDOWN_OFF_ATTACK));
            return;
        }
        lore.add(Messages.text(
                CooldownRule.interactable(material) ? Messages.COOLDOWN_USE : Messages.COOLDOWN_ATTACK,
                "seconds",
                String.valueOf(seconds)));
    }

    public static void fill(Inventory inventory, ItemStack filler) {
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            inventory.setItem(slot, filler.clone());
        }
    }

    public static ItemStack pane(ItemCooldownsPlugin plugin, Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setHideTooltip(true);
        return store(plugin, item, meta);
    }

    public static void frame(Inventory inventory, ItemCooldownsPlugin plugin) {
        frame(inventory, plugin, Material.GRAY_STAINED_GLASS_PANE);
    }

    public static void frame(Inventory inventory, ItemCooldownsPlugin plugin, Material pane) {
        ItemStack border = pane(plugin, pane);
        int size = inventory.getSize();
        int rows = size / 9;
        for (int slot = 0; slot < size; slot++) {
            int col = slot % 9;
            int row = slot / 9;
            if (row != 0 && row != rows - 1 && col != 0 && col != 8) {
                continue;
            }
            inventory.setItem(slot, border.clone());
        }
    }

    public static int[] innerSlots(int size) {
        int rows = size / 9;
        List<Integer> slots = new ArrayList<>();
        for (int slot = 0; slot < size; slot++) {
            int col = slot % 9;
            int row = slot / 9;
            if (row > 0 && row < rows - 1 && col > 0 && col < 8) {
                slots.add(slot);
            }
        }
        int[] inner = new int[slots.size()];
        for (int i = 0; i < slots.size(); i++) {
            inner[i] = slots.get(i);
        }
        return inner;
    }

    public static int[] rowSlots(int row, int count) {
        int start = row * 9 + (9 - count) / 2;
        int[] slots = new int[count];
        for (int i = 0; i < count; i++) {
            slots[i] = start + i;
        }
        return slots;
    }

    public static int[] rowsOf(int count, int... rows) {
        int[] all = new int[rows.length * count];
        int offset = 0;
        for (int row : rows) {
            int[] slots = rowSlots(row, count);
            System.arraycopy(slots, 0, all, offset, slots.length);
            offset += slots.length;
        }
        return all;
    }

    public static int indexOf(int[] slots, int slot) {
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] == slot) {
                return i;
            }
        }
        return -1;
    }

    public static ItemStack filler(ItemCooldownsPlugin plugin) {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.setHideTooltip(true);
        return store(plugin, item, meta);
    }

    public static void strip(ItemCooldownsPlugin plugin, Player player) {
        stripInventory(plugin, player.getInventory());
        if (isTagged(plugin, player.getItemOnCursor())) {
            player.setItemOnCursor(null);
        }
    }

    private static void stripInventory(ItemCooldownsPlugin plugin, Inventory inventory) {
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            if (isTagged(plugin, inventory.getItem(slot))) {
                inventory.setItem(slot, null);
            }
        }
    }

    public static boolean isAir(ItemStack stack) {
        return stack == null || stack.getType().isAir();
    }

    public static String seconds(int value) {
        return value <= 0 ? "off" : value + "s";
    }

    public static String pretty(String raw) {
        String[] parts = raw.toLowerCase().split("_");
        StringBuilder out = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            if (!out.isEmpty()) {
                out.append(' ');
            }
            out.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return out.toString();
    }

    public static Component shownName(Player player, ItemStack stack) {
        return plain(GlobalTranslator.render(stack.displayName(), player.locale()));
    }

    public static Component plain(Component component) {
        Component next = component
                .decoration(TextDecoration.ITALIC, false)
                .colorIfAbsent(NamedTextColor.WHITE);
        List<Component> children = new ArrayList<>(component.children().size());
        for (Component child : component.children()) {
            children.add(plain(child));
        }
        return next.children(children);
    }

    private static ItemStack store(ItemCooldownsPlugin plugin, ItemStack item, ItemMeta meta) {
        meta.getPersistentDataContainer().set(key(plugin), PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    private static NamespacedKey key(ItemCooldownsPlugin plugin) {
        return new NamespacedKey(plugin, "gui");
    }
}
