package net.mattwhyy.itemcooldowns.features.gui;

import java.util.List;
import net.mattwhyy.itemcooldowns.ItemCooldownsPlugin;
import net.mattwhyy.itemcooldowns.Messages;
import net.mattwhyy.itemcooldowns.features.cooldown.CooldownRule;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public final class RuleListMenu implements MenuHolder {
    public static final int SIZE = 54;
    private static final int BOOK = 4;
    private static final int PREVIOUS = 45;
    private static final int NEXT = 52;
    private static final int PRESETS = 53;
    private static final int[] INNER = GuiItems.innerSlots(SIZE);
    private static final int PAGE_SIZE = INNER.length;

    private final ItemCooldownsPlugin plugin;
    private final Player viewer;
    private final Inventory inventory;
    private final int page;
    private boolean silent;

    private RuleListMenu(ItemCooldownsPlugin plugin, Player viewer, int page) {
        this.plugin = plugin;
        this.viewer = viewer;
        this.page = page;
        this.inventory = Bukkit.createInventory(this, SIZE, Messages.text(Messages.TITLE_LIST));
        paint();
    }

    public static void open(ItemCooldownsPlugin plugin, Player player, int page, boolean sound) {
        markCurrentSilent(player);
        RuleListMenu menu = new RuleListMenu(plugin, player, clamp(plugin, page));
        player.openInventory(menu.inventory);
        if (sound) {
            MenuSounds.open(player);
        }
    }

    private static int clamp(ItemCooldownsPlugin plugin, int page) {
        int last = lastPage(plugin.rules().rules().size());
        if (page < 0) {
            return 0;
        }
        return Math.min(page, last);
    }

    private static int lastPage(int rules) {
        int entries = rules + 1;
        return Math.max(0, (entries - 1) / PAGE_SIZE);
    }

    private static boolean hasNext(int rules, int page) {
        return (page + 1) * PAGE_SIZE < rules + 1;
    }

    static void markCurrentSilent(Player player) {
        if (player.getOpenInventory().getTopInventory().getHolder() instanceof MenuHolder holder) {
            holder.markSilent();
        }
    }

    private void paint() {
        GuiItems.frame(inventory, plugin);
        inventory.setItem(BOOK, GuiItems.button(
                plugin,
                Material.BOOK,
                Messages.HELP_NAME,
                Messages.LIST_LORE,
                Messages.LIST_TOGGLE,
                Messages.LIST_ADD));
        inventory.setItem(PRESETS, GuiItems.button(plugin, Material.CHEST, Messages.PRESETS, Messages.PRESETS_LORE));
        List<CooldownRule> rules = plugin.rules().rules();
        int start = page * PAGE_SIZE;
        for (int offset = 0; offset < PAGE_SIZE; offset++) {
            int entry = start + offset;
            int slot = INNER[offset];
            if (entry < rules.size()) {
                inventory.setItem(slot, GuiItems.ruleIcon(plugin, viewer, rules.get(entry)));
            } else if (entry == rules.size()) {
                inventory.setItem(slot, GuiItems.button(plugin, Material.EMERALD, true, Messages.ADD));
            } else {
                inventory.setItem(slot, null);
            }
        }
        if (page > 0) {
            inventory.setItem(PREVIOUS, GuiItems.button(plugin, Material.ARROW, Messages.PREVIOUS));
        }
        if (hasNext(rules.size(), page)) {
            inventory.setItem(NEXT, GuiItems.button(plugin, Material.ARROW, Messages.NEXT));
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public void onClick(Player player, InventoryClickEvent event) {
        int raw = event.getRawSlot();
        if (raw >= SIZE) {
            MenuHolder.freePlayerInv(event);
            return;
        }
        if (raw < 0) {
            return;
        }
        if (raw == PRESETS) {
            later(player, () -> PresetMenu.open(plugin, player, true));
            return;
        }
        int rules = plugin.rules().rules().size();
        if (raw == PREVIOUS && page > 0) {
            later(player, () -> {
                RuleListMenu.open(plugin, player, page - 1, false);
                MenuSounds.page(player);
            });
            return;
        }
        if (raw == NEXT && hasNext(rules, page)) {
            later(player, () -> {
                RuleListMenu.open(plugin, player, page + 1, false);
                MenuSounds.page(player);
            });
            return;
        }
        int offset = indexOf(raw);
        if (offset < 0) {
            return;
        }
        int entry = page * PAGE_SIZE + offset;
        if (entry == rules) {
            ItemStack held = GuiItems.asTemplate(plugin, player.getInventory().getItemInMainHand());
            CooldownRule draft = held == null ? null : CooldownRule.fromItem(held, true, true, true, 5);
            later(player, () -> RuleEditorMenu.open(plugin, player, -1, draft, true));
            return;
        }
        if (entry > rules) {
            return;
        }
        CooldownRule rule = plugin.rules().get(entry);
        if (event.getClick().isRightClick()) {
            plugin.rules().setActive(entry, !rule.active());
            paint();
            if (plugin.rules().get(entry).active()) {
                MenuSounds.toggleOn(player);
            } else {
                MenuSounds.toggleOff(player);
            }
            return;
        }
        later(player, () -> RuleEditorMenu.open(plugin, player, entry, rule, true));
    }

    private static int indexOf(int slot) {
        for (int i = 0; i < INNER.length; i++) {
            if (INNER[i] == slot) {
                return i;
            }
        }
        return -1;
    }

    private void later(Player player, Runnable action) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!player.isOnline()) {
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
