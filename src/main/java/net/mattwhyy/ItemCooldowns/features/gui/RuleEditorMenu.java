package net.mattwhyy.itemcooldowns.features.gui;

import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.mattwhyy.itemcooldowns.ItemCooldownsPlugin;
import net.mattwhyy.itemcooldowns.Messages;
import net.mattwhyy.itemcooldowns.features.cooldown.CooldownRule;
import net.mattwhyy.itemcooldowns.features.cooldown.ItemFingerprint;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public final class RuleEditorMenu implements MenuHolder {
    private static final int SIZE = 45;
    private static final int BOOK = 4;
    private static final int ITEM = 13;
    private static final int TIME = 22;
    private static final int POTION = 29;
    private static final int CMD = 31;
    private static final int MODEL = 33;
    private static final int BACK = 36;
    private static final int DELETE = 37;
    private static final int MAX_SECONDS = 3600;

    private final ItemCooldownsPlugin plugin;
    private final Player viewer;
    private final Inventory inventory;
    private final CooldownRule.Group group;
    private int ruleIndex;
    private ItemStack template;
    private int seconds;
    private boolean matchPotion;
    private boolean matchCmd;
    private boolean matchModel;
    private int generation;
    private boolean silent;
    private boolean skipSave;

    private RuleEditorMenu(ItemCooldownsPlugin plugin, Player viewer, int ruleIndex, CooldownRule rule) {
        this.plugin = plugin;
        this.viewer = viewer;
        this.ruleIndex = ruleIndex;
        this.group = rule == null ? CooldownRule.Group.NONE : rule.group();
        this.template = rule == null ? null : GuiItems.asTemplate(plugin, rule.icon());
        this.seconds = rule == null ? 5 : Math.max(1, rule.seconds());
        this.matchPotion = rule != null && rule.potion() != null;
        this.matchCmd = rule != null && rule.cmd() != null;
        this.matchModel = rule != null && rule.itemModel() != null;
        this.inventory = Bukkit.createInventory(this, SIZE, Messages.text(Messages.TITLE_EDITOR));
        paint();
    }

    public static void open(ItemCooldownsPlugin plugin, Player player, int ruleIndex, CooldownRule rule, boolean sound) {
        RuleListMenu.markCurrentSilent(player);
        RuleEditorMenu menu = new RuleEditorMenu(plugin, player, ruleIndex, rule);
        player.openInventory(menu.inventory);
        if (sound) {
            MenuSounds.open(player);
        }
    }

    private void paint() {
        GuiItems.fill(inventory, GuiItems.filler(plugin));
        inventory.setItem(BOOK, GuiItems.button(
                plugin,
                Material.BOOK,
                Messages.EDITOR_BOOK,
                group == CooldownRule.Group.SPEAR
                        ? Messages.EDITOR_SPEAR_LORE
                        : grouped() ? Messages.EDITOR_GROUP_LORE : Messages.EDITOR_LORE));
        inventory.setItem(BACK, GuiItems.button(plugin, Material.ARROW, Messages.BACK));
        if (ruleIndex >= 0) {
            inventory.setItem(DELETE, deleteButton());
        }
        refresh();
    }

    private ItemStack deleteButton() {
        return GuiItems.button(plugin, Material.BARRIER, Messages.DELETE, Messages.DELETE_LORE);
    }

    private void refresh() {
        inventory.setItem(ITEM, itemButton());
        inventory.setItem(TIME, timeButton());
        if (grouped()) {
            inventory.setItem(POTION, GuiItems.filler(plugin));
            inventory.setItem(CMD, GuiItems.filler(plugin));
            inventory.setItem(MODEL, GuiItems.filler(plugin));
            return;
        }
        paintMatch(POTION, hasPotion(), matchPotion, Material.POTION, Messages.MATCH_POTION_ON, Messages.MATCH_POTION_OFF);
        paintMatch(CMD, hasCmd(), matchCmd, Material.ITEM_FRAME, Messages.MATCH_CMD_ON, Messages.MATCH_CMD_OFF);
        paintMatch(MODEL, hasModel(), matchModel, Material.GLOW_ITEM_FRAME, Messages.MATCH_MODEL_ON, Messages.MATCH_MODEL_OFF);
    }

    private ItemStack timeButton() {
        List<Component> lore = new ArrayList<>();
        if (template == null && !grouped()) {
            lore.add(Messages.text(Messages.COOLDOWN_UNSET));
        } else {
            GuiItems.addCooldownLore(lore, loreMaterial(), seconds);
        }
        lore.add(Messages.text(Messages.ADJUST_LEFT));
        lore.add(Messages.text(Messages.ADJUST_RIGHT));
        lore.add(Messages.text(Messages.ADJUST_SHIFT_LEFT));
        lore.add(Messages.text(Messages.ADJUST_SHIFT_RIGHT));
        ItemStack item = GuiItems.named(
                plugin,
                new ItemStack(Material.CLOCK),
                Messages.text(Messages.TIME),
                lore,
                seconds > 0);
        item.setAmount(Math.max(1, Math.min(64, Math.max(seconds, 1))));
        return item;
    }

    private ItemStack itemButton() {
        if (template == null) {
            return null;
        }
        Component name = grouped() ? Messages.text(groupNameKey()) : GuiItems.shownName(viewer, template);
        return GuiItems.named(plugin, template, name, List.of(), grouped());
    }

    private String groupNameKey() {
        return switch (group) {
            case SPEAR -> Messages.PRESET_SPEAR;
            case TURTLE_MASTER -> Messages.PRESET_TURTLE;
            case INSTANT_HEALTH -> Messages.PRESET_HEALING;
            case NONE -> Messages.ITEM_SLOT;
        };
    }

    private void paintMatch(int slot, boolean available, boolean on, Material material, String onKey, String offKey) {
        if (!available) {
            inventory.setItem(slot, GuiItems.filler(plugin));
            return;
        }
        inventory.setItem(slot, GuiItems.button(
                plugin,
                material,
                on,
                on ? onKey : offKey,
                on ? Messages.STATUS_ON : Messages.STATUS_OFF));
    }

    private boolean hasPotion() {
        ItemFingerprint print = ItemFingerprint.of(template);
        return print != null && print.potion() != null;
    }

    private boolean hasCmd() {
        ItemFingerprint print = ItemFingerprint.of(template);
        return print != null && print.cmd() != null;
    }

    private boolean hasModel() {
        ItemFingerprint print = ItemFingerprint.of(template);
        return print != null && print.itemModel() != null;
    }

    private boolean grouped() {
        return group != CooldownRule.Group.NONE;
    }

    private Material loreMaterial() {
        if (template != null) {
            return template.getType();
        }
        return switch (group) {
            case SPEAR -> CooldownRule.materialOf("NETHERITE_SPEAR") == null
                    ? Material.TRIDENT
                    : CooldownRule.materialOf("NETHERITE_SPEAR");
            case TURTLE_MASTER, INSTANT_HEALTH -> Material.POTION;
            case NONE -> Material.IRON_SWORD;
        };
    }

    private boolean canSave() {
        if (seconds <= 0) {
            return false;
        }
        return grouped() || template != null;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public void onClick(Player player, InventoryClickEvent event) {
        int raw = event.getRawSlot();
        if (isBarrier(player.getItemOnCursor()) && isThrow(event, raw)) {
            tryDropBarrier(player, player.getItemOnCursor());
            return;
        }
        if (raw >= SIZE) {
            if (isBarrier(player.getItemOnCursor())) {
                return;
            }
            if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY && !grouped()) {
                takeFromBottom(player, event);
                return;
            }
            MenuHolder.freePlayerInv(event);
            return;
        }
        if (raw == BACK) {
            later(player, () -> RuleListMenu.open(plugin, player, 0, true));
            return;
        }
        if (raw == DELETE && ruleIndex >= 0) {
            if (isThrow(event, raw)) {
                tryDropBarrier(player, inventory.getItem(DELETE));
                return;
            }
            if (isBarrier(player.getItemOnCursor())) {
                inventory.setItem(DELETE, player.getItemOnCursor());
                player.setItemOnCursor(null);
                return;
            }
            if (inventory.getItem(DELETE) != null) {
                player.setItemOnCursor(inventory.getItem(DELETE));
                inventory.setItem(DELETE, null);
            }
            return;
        }
        if (raw == TIME) {
            seconds = adjust(seconds, event.getClick());
            refresh();
            MenuSounds.click(player);
            return;
        }
        if (grouped()) {
            return;
        }
        if (raw == POTION && hasPotion()) {
            matchPotion = !matchPotion;
            refresh();
            playToggle(player, matchPotion);
            return;
        }
        if (raw == CMD && hasCmd()) {
            matchCmd = !matchCmd;
            refresh();
            playToggle(player, matchCmd);
            return;
        }
        if (raw == MODEL && hasModel()) {
            matchModel = !matchModel;
            refresh();
            playToggle(player, matchModel);
            return;
        }
        if (raw == ITEM) {
            swapItem(player);
        }
    }

    public boolean tryDropBarrier(Player player, ItemStack stack) {
        if (ruleIndex < 0 || !isBarrier(stack)) {
            return false;
        }
        player.setItemOnCursor(null);
        persist();
        skipSave = true;
        later(player, () -> ConfirmDeleteMenu.open(plugin, player, ruleIndex, true));
        return true;
    }

    private boolean isBarrier(ItemStack stack) {
        return stack != null && stack.getType() == Material.BARRIER && GuiItems.isTagged(plugin, stack);
    }

    private static boolean isThrow(InventoryClickEvent event, int raw) {
        ClickType click = event.getClick();
        return click == ClickType.DROP
                || click == ClickType.CONTROL_DROP
                || click == ClickType.WINDOW_BORDER_LEFT
                || click == ClickType.WINDOW_BORDER_RIGHT
                || raw < 0;
    }

    private void swapItem(Player player) {
        ItemStack cursor = player.getItemOnCursor();
        ItemStack incoming = GuiItems.asTemplate(plugin, cursor);
        ItemStack outgoing = template;
        if (incoming == null && outgoing == null) {
            return;
        }
        if (incoming != null && outgoing != null && cursor.getAmount() > 1) {
            MenuSounds.deny(player);
            return;
        }
        template = incoming;
        if (incoming == null) {
            player.setItemOnCursor(outgoing.clone());
            matchPotion = true;
            matchCmd = true;
            matchModel = true;
            MenuSounds.clear(player);
        } else {
            if (cursor.getAmount() <= 1) {
                player.setItemOnCursor(outgoing == null ? new ItemStack(Material.AIR) : outgoing.clone());
            } else {
                cursor.setAmount(cursor.getAmount() - 1);
                player.setItemOnCursor(cursor);
            }
            accept(template);
            MenuSounds.place(player);
        }
        refresh();
    }

    private void takeFromBottom(Player player, InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();
        ItemStack incoming = GuiItems.asTemplate(plugin, clicked);
        if (incoming == null) {
            return;
        }
        if (template != null && clicked.getAmount() > 1) {
            MenuSounds.deny(player);
            return;
        }
        if (clicked.getAmount() <= 1) {
            event.setCurrentItem(template == null ? null : template.clone());
        } else {
            clicked.setAmount(clicked.getAmount() - 1);
            event.setCurrentItem(clicked);
        }
        accept(incoming);
        refresh();
        MenuSounds.place(player);
    }

    private void accept(ItemStack incoming) {
        template = incoming;
        matchPotion = true;
        matchCmd = true;
        matchModel = true;
        if (seconds <= 0) {
            seconds = 5;
        }
    }

    private static int adjust(int current, ClickType click) {
        int delta = switch (click) {
            case LEFT -> 1;
            case RIGHT -> -1;
            case SHIFT_LEFT -> 5;
            case SHIFT_RIGHT -> -5;
            default -> 0;
        };
        return Math.max(0, Math.min(MAX_SECONDS, current + delta));
    }

    @Override
    public void onClose(Player player) {
        if (skipSave) {
            return;
        }
        persist();
    }

    private void persist() {
        if (!canSave()) {
            return;
        }
        CooldownRule rule = buildRule();
        if (rule == null) {
            return;
        }
        int existing = plugin.rules().indexOf(rule);
        if (existing >= 0 && existing != ruleIndex) {
            return;
        }
        if (ruleIndex >= 0) {
            CooldownRule old = plugin.rules().get(ruleIndex);
            if (old == null) {
                return;
            }
            plugin.rules().replace(ruleIndex, keepId(old, rule));
            return;
        }
        plugin.rules().add(rule);
        ruleIndex = plugin.rules().indexOf(rule);
    }

    private CooldownRule buildRule() {
        if (grouped()) {
            Material material = template == null ? Material.BARRIER : template.getType();
            return new CooldownRule(
                    null,
                    material,
                    template == null || ItemFingerprint.of(template) == null ? null : ItemFingerprint.of(template).potion(),
                    null,
                    null,
                    CooldownRule.leftOf(material, seconds),
                    CooldownRule.rightOf(material, seconds),
                    group,
                    new java.util.LinkedHashSet<>(CooldownRule.groupKeys(group)));
        }
        return CooldownRule.fromItem(
                template,
                matchPotion && hasPotion(),
                matchCmd && hasCmd(),
                matchModel && hasModel(),
                seconds);
    }

    private static CooldownRule keepId(CooldownRule old, CooldownRule next) {
        return new CooldownRule(
                old.id(),
                next.material(),
                next.potion(),
                next.cmd(),
                next.itemModel(),
                next.leftSeconds(),
                next.rightSeconds(),
                next.group(),
                next.enabled(),
                old.active());
    }

    private void later(Player player, Runnable action) {
        int generation = nextGeneration();
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (generation != this.generation || !player.isOnline()) {
                return;
            }
            if (player.getOpenInventory().getTopInventory().getHolder() != this) {
                return;
            }
            if (!player.hasPermission("itemcooldowns.admin")) {
                markSilent();
                MenuSounds.deny(player);
                Messages.actionBar(player, Messages.NO_PERMISSION);
                player.closeInventory();
                return;
            }
            action.run();
        });
    }

    private static void playToggle(Player player, boolean on) {
        if (on) {
            MenuSounds.toggleOn(player);
        } else {
            MenuSounds.toggleOff(player);
        }
    }

    private int nextGeneration() {
        return ++generation;
    }

    @Override
    public void onDrag(Player player, InventoryDragEvent event) {
        MenuHolder.freePlayerDrag(event);
    }

    @Override
    public void markSilent() {
        silent = true;
    }

    @Override
    public boolean consumeSilent() {
        boolean was = silent;
        silent = false;
        return was;
    }
}
