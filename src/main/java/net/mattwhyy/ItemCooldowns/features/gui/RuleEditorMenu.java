package net.mattwhyy.itemcooldowns.features.gui;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
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
    private static final int LEFT = 19;
    private static final int ITEM = 22;
    private static final int RIGHT = 25;
    private static final int POTION = 29;
    private static final int CMD = 31;
    private static final int MODEL = 33;
    private static final int BACK = 36;
    private static final int SAVE = 40;
    private static final int DELETE = 44;
    private static final int MAX_SECONDS = 3600;
    private static final int[] TOGGLES = GuiItems.rowsOf(7, 2, 3);

    private final ItemCooldownsPlugin plugin;
    private final Player viewer;
    private final Inventory inventory;
    private final int ruleIndex;
    private final CooldownRule.Group group;
    private final List<String> options;
    private final Set<String> enabled;
    private ItemStack template;
    private int leftSeconds;
    private int rightSeconds;
    private boolean matchPotion;
    private boolean matchCmd;
    private boolean matchModel;
    private int generation;
    private boolean silent;

    private RuleEditorMenu(ItemCooldownsPlugin plugin, Player viewer, int ruleIndex, CooldownRule rule) {
        this.plugin = plugin;
        this.viewer = viewer;
        this.ruleIndex = ruleIndex;
        this.group = rule == null ? CooldownRule.Group.NONE : rule.group();
        this.options = group == CooldownRule.Group.NONE ? List.of() : CooldownRule.groupKeys(group);
        this.enabled = new LinkedHashSet<>(rule == null || !rule.grouped() ? options : rule.enabled());
        this.template = rule == null ? null : GuiItems.asTemplate(plugin, rule.icon());
        this.leftSeconds = rule == null ? 0 : rule.leftSeconds();
        this.rightSeconds = rule == null ? 0 : rule.rightSeconds();
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

    public static void open(
            ItemCooldownsPlugin plugin,
            Player player,
            int ruleIndex,
            ItemStack template,
            int leftSeconds,
            int rightSeconds,
            boolean matchPotion,
            boolean matchCmd,
            boolean matchModel,
            boolean sound) {
        CooldownRule rule = CooldownRule.fromItem(template, matchPotion, matchCmd, matchModel, leftSeconds, rightSeconds);
        if (rule == null && template == null) {
            open(plugin, player, ruleIndex, null, sound);
            return;
        }
        if (rule == null) {
            rule = new CooldownRule(null, template.getType(), null, null, null, leftSeconds, rightSeconds);
        }
        open(plugin, player, ruleIndex, rule, sound);
    }

    private void paint() {
        GuiItems.fill(inventory, GuiItems.filler(plugin));
        inventory.setItem(BOOK, GuiItems.button(
                plugin,
                Material.BOOK,
                Messages.EDITOR_BOOK,
                grouped() ? Messages.EDITOR_GROUP_LORE : Messages.EDITOR_LORE));
        inventory.setItem(BACK, GuiItems.button(plugin, Material.ARROW, Messages.BACK));
        if (ruleIndex >= 0) {
            inventory.setItem(DELETE, GuiItems.button(plugin, Material.BARRIER, Messages.DELETE));
        }
        refresh();
    }

    private void refresh() {
        inventory.setItem(LEFT, showLeft() ? sideButton(Material.IRON_SWORD, Messages.LEFT, leftSeconds) : GuiItems.filler(plugin));
        inventory.setItem(RIGHT, showRight() ? sideButton(Material.ENDER_PEARL, Messages.RIGHT, rightSeconds) : GuiItems.filler(plugin));
        inventory.setItem(ITEM, itemButton());
        inventory.setItem(SAVE, GuiItems.button(plugin, Material.EMERALD, canSave(), Messages.SAVE));
        if (grouped()) {
            paintToggles();
            return;
        }
        paintMatch(POTION, hasPotion(), matchPotion, Material.POTION, Messages.MATCH_POTION_ON, Messages.MATCH_POTION_OFF);
        paintMatch(CMD, hasCmd(), matchCmd, Material.ITEM_FRAME, Messages.MATCH_CMD_ON, Messages.MATCH_CMD_OFF);
        paintMatch(MODEL, hasModel(), matchModel, Material.GLOW_ITEM_FRAME, Messages.MATCH_MODEL_ON, Messages.MATCH_MODEL_OFF);
    }

    private void paintToggles() {
        for (int i = 0; i < TOGGLES.length; i++) {
            if (i >= options.size()) {
                inventory.setItem(TOGGLES[i], GuiItems.filler(plugin));
                continue;
            }
            String key = options.get(i);
            ItemStack icon = CooldownRule.optionIcon(key);
            if (icon == null) {
                inventory.setItem(TOGGLES[i], GuiItems.filler(plugin));
                continue;
            }
            boolean on = enabled.contains(key);
            List<Component> lore = new ArrayList<>();
            lore.add(Messages.text(on ? Messages.SECONDS_ON : Messages.SECONDS_OFF, "seconds", String.valueOf(Math.max(leftSeconds, rightSeconds))));
            inventory.setItem(TOGGLES[i], GuiItems.named(plugin, icon, GuiItems.shownName(viewer, icon), lore, on));
        }
    }

    private ItemStack sideButton(Material material, String nameKey, int seconds) {
        List<Component> lore = new ArrayList<>();
        lore.add(Messages.text(seconds <= 0 ? Messages.SECONDS_OFF : Messages.SECONDS_ON, "seconds", String.valueOf(seconds)));
        lore.add(Messages.text(Messages.ADJUST_HINT));
        return GuiItems.named(plugin, new ItemStack(material), Messages.text(nameKey), lore, seconds > 0);
    }

    private ItemStack itemButton() {
        if (template == null) {
            return GuiItems.button(plugin, Material.LIGHT_GRAY_STAINED_GLASS_PANE, Messages.ITEM_SLOT, Messages.ITEM_SLOT_EMPTY);
        }
        List<Component> lore = new ArrayList<>();
        lore.add(Messages.text(grouped() ? Messages.GROUP_SELECT_ALL : Messages.ITEM_SLOT));
        Component name = grouped() ? Messages.text(groupNameKey()) : GuiItems.shownName(viewer, template);
        return GuiItems.named(plugin, template, name, lore, grouped());
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
        inventory.setItem(slot, GuiItems.button(plugin, material, on, on ? onKey : offKey));
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

    private boolean canSave() {
        if (leftSeconds <= 0 && rightSeconds <= 0) {
            return false;
        }
        if (grouped()) {
            return !enabled.isEmpty();
        }
        return template != null;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public void onClick(Player player, InventoryClickEvent event) {
        int raw = event.getRawSlot();
        if (raw >= SIZE) {
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
        if (raw == SAVE) {
            trySave(player);
            return;
        }
        if (raw == DELETE && ruleIndex >= 0) {
            later(player, () -> ConfirmDeleteMenu.open(plugin, player, ruleIndex, true));
            return;
        }
        if (raw == LEFT && showLeft()) {
            leftSeconds = adjust(leftSeconds, event.getClick());
            refresh();
            MenuSounds.click(player);
            return;
        }
        if (raw == RIGHT && showRight()) {
            rightSeconds = adjust(rightSeconds, event.getClick());
            refresh();
            MenuSounds.click(player);
            return;
        }
        if (grouped()) {
            if (raw == ITEM) {
                enabled.clear();
                enabled.addAll(options);
                refresh();
                MenuSounds.toggleOn(player);
                return;
            }
            int option = GuiItems.indexOf(TOGGLES, raw);
            if (option >= 0 && option < options.size()) {
                toggleOption(player, options.get(option));
            }
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

    private void toggleOption(Player player, String key) {
        if (enabled.contains(key)) {
            enabled.remove(key);
            refresh();
            MenuSounds.toggleOff(player);
            return;
        }
        enabled.addAll(options);
        refresh();
        MenuSounds.toggleOn(player);
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
        applySideDefaults(incoming.getType());
    }

    private void applySideDefaults(Material material) {
        if (CooldownRule.rightOnly(material)) {
            leftSeconds = 0;
            if (rightSeconds <= 0) {
                rightSeconds = 5;
            }
            return;
        }
        if (CooldownRule.leftOnly(material)) {
            rightSeconds = 0;
            if (leftSeconds <= 0) {
                leftSeconds = 5;
            }
        }
    }

    private boolean showLeft() {
        if (grouped()) {
            return group == CooldownRule.Group.SPEAR;
        }
        return template == null || CooldownRule.showLeft(template.getType());
    }

    private boolean showRight() {
        if (grouped()) {
            return true;
        }
        return template == null || CooldownRule.showRight(template.getType());
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

    private void trySave(Player player) {
        if (!grouped() && template == null) {
            MenuSounds.deny(player);
            Messages.actionBar(player, Messages.NEED_ITEM);
            return;
        }
        if (leftSeconds <= 0 && rightSeconds <= 0) {
            MenuSounds.deny(player);
            Messages.actionBar(player, Messages.NEED_TIME);
            return;
        }
        if (grouped() && enabled.isEmpty()) {
            MenuSounds.deny(player);
            Messages.actionBar(player, Messages.NEED_ONE_TYPE);
            return;
        }
        CooldownRule rule = buildRule();
        if (rule == null) {
            MenuSounds.deny(player);
            Messages.actionBar(player, Messages.NEED_ITEM);
            return;
        }
        int existing = plugin.rules().indexOf(rule);
        if (existing >= 0 && existing != ruleIndex) {
            MenuSounds.deny(player);
            Messages.actionBar(player, Messages.DUPLICATE);
            return;
        }
        later(player, () -> {
            if (ruleIndex >= 0) {
                CooldownRule old = plugin.rules().get(ruleIndex);
                if (old == null) {
                    MenuSounds.deny(player);
                    return;
                }
                plugin.rules().replace(ruleIndex, keepId(old, rule));
            } else {
                plugin.rules().add(rule);
            }
            MenuSounds.save(player);
            Messages.actionBar(player, Messages.SAVED);
            RuleListMenu.open(plugin, player, 0, false);
        });
    }

    private CooldownRule buildRule() {
        if (grouped()) {
            return new CooldownRule(
                    null,
                    template == null ? Material.BARRIER : template.getType(),
                    template == null ? null : ItemFingerprint.of(template) == null ? null : ItemFingerprint.of(template).potion(),
                    null,
                    null,
                    leftSeconds,
                    rightSeconds,
                    group,
                    enabled);
        }
        return CooldownRule.fromItem(
                template,
                matchPotion && hasPotion(),
                matchCmd && hasCmd(),
                matchModel && hasModel(),
                leftSeconds,
                rightSeconds);
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
                next.enabled());
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
