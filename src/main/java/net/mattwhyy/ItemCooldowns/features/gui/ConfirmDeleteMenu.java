package net.mattwhyy.itemcooldowns.features.gui;

import java.util.List;
import net.mattwhyy.itemcooldowns.ItemCooldownsPlugin;
import net.mattwhyy.itemcooldowns.Messages;
import net.mattwhyy.itemcooldowns.features.cooldown.CooldownRule;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public final class ConfirmDeleteMenu implements MenuHolder {
    private static final int SIZE = 27;
    private static final int ICON = 13;
    private static final int CANCEL = 18;

    private final ItemCooldownsPlugin plugin;
    private final Player viewer;
    private final Inventory inventory;
    private final int ruleIndex;
    private boolean holding;
    private boolean silent;

    private ConfirmDeleteMenu(ItemCooldownsPlugin plugin, Player viewer, int ruleIndex) {
        this.plugin = plugin;
        this.viewer = viewer;
        this.ruleIndex = ruleIndex;
        this.inventory = Bukkit.createInventory(this, SIZE, Messages.text(Messages.TITLE_CONFIRM));
        GuiItems.frame(inventory, plugin, Material.BLACK_STAINED_GLASS_PANE);
        inventory.setItem(CANCEL, GuiItems.button(plugin, Material.ARROW, Messages.CANCEL));
        CooldownRule rule = plugin.rules().get(ruleIndex);
        if (rule != null) {
            inventory.setItem(ICON, dropIcon(rule));
        }
    }

    public static void open(ItemCooldownsPlugin plugin, Player player, int ruleIndex, boolean sound) {
        RuleListMenu.markCurrentSilent(player);
        ConfirmDeleteMenu menu = new ConfirmDeleteMenu(plugin, player, ruleIndex);
        player.openInventory(menu.inventory);
        if (sound) {
            MenuSounds.confirm(player);
        }
    }

    private ItemStack dropIcon(CooldownRule rule) {
        ItemStack icon = rule.icon();
        return GuiItems.named(
                plugin,
                icon,
                rule.groupNameKey() == null ? GuiItems.shownName(viewer, icon) : Messages.text(rule.groupNameKey()),
                List.of(Messages.text(Messages.DROP_HINT)),
                false);
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
        if (isDrop(event, raw)) {
            dropOut(player);
            return;
        }
        if (raw == CANCEL) {
            cancel(player);
            return;
        }
        if (raw == ICON && !holding && inventory.getItem(ICON) != null) {
            holding = true;
            player.setItemOnCursor(inventory.getItem(ICON));
            inventory.setItem(ICON, null);
            return;
        }
        if (holding && raw == ICON) {
            inventory.setItem(ICON, player.getItemOnCursor());
            player.setItemOnCursor(null);
            holding = false;
        }
    }

    private boolean isDrop(InventoryClickEvent event, int raw) {
        ClickType click = event.getClick();
        boolean throwClick = click == ClickType.DROP
                || click == ClickType.CONTROL_DROP
                || click == ClickType.WINDOW_BORDER_LEFT
                || click == ClickType.WINDOW_BORDER_RIGHT
                || raw < 0;
        return throwClick && (holding || raw == ICON);
    }

    private void dropOut(Player player) {
        player.setItemOnCursor(null);
        holding = false;
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!stillOpen(player)) {
                return;
            }
            if (!player.hasPermission("itemcooldowns.admin")) {
                markSilent();
                MenuSounds.deny(player);
                Messages.actionBar(player, Messages.NO_PERMISSION);
                player.closeInventory();
                return;
            }
            if (plugin.rules().get(ruleIndex) == null) {
                MenuSounds.deny(player);
                return;
            }
            plugin.rules().delete(ruleIndex);
            MenuSounds.delete(player);
            Messages.actionBar(player, Messages.DELETED);
            RuleListMenu.open(plugin, player, 0, false);
        });
    }

    private void cancel(Player player) {
        CooldownRule rule = plugin.rules().get(ruleIndex);
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!stillOpen(player) || rule == null) {
                return;
            }
            player.setItemOnCursor(null);
            RuleEditorMenu.open(plugin, player, ruleIndex, rule, true);
        });
    }

    private boolean stillOpen(Player player) {
        return player.isOnline() && player.getOpenInventory().getTopInventory().getHolder() == this;
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
