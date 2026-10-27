package net.mattwhyy.itemcooldowns.features.gui;

import net.mattwhyy.itemcooldowns.ItemCooldownsPlugin;
import net.mattwhyy.itemcooldowns.Messages;
import net.mattwhyy.itemcooldowns.features.cooldown.CooldownRule;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

public final class ConfirmDeleteMenu implements MenuHolder {
    private static final int SIZE = 27;
    private static final int CONFIRM = 11;
    private static final int ICON = 13;
    private static final int CANCEL = 15;

    private final ItemCooldownsPlugin plugin;
    private final Player viewer;
    private final Inventory inventory;
    private final int ruleIndex;
    private boolean silent;

    private ConfirmDeleteMenu(ItemCooldownsPlugin plugin, Player viewer, int ruleIndex) {
        this.plugin = plugin;
        this.viewer = viewer;
        this.ruleIndex = ruleIndex;
        this.inventory = Bukkit.createInventory(this, SIZE, Messages.text(Messages.TITLE_CONFIRM));
        var filler = GuiItems.filler(plugin);
        for (int slot = 0; slot < SIZE; slot++) {
            inventory.setItem(slot, filler.clone());
        }
        inventory.setItem(CONFIRM, GuiItems.button(plugin, Material.LIME_CONCRETE, Messages.CONFIRM));
        CooldownRule rule = plugin.rules().get(ruleIndex);
        if (rule != null) {
            inventory.setItem(ICON, GuiItems.ruleIcon(plugin, viewer, rule));
        }
        inventory.setItem(CANCEL, GuiItems.button(plugin, Material.RED_CONCRETE, Messages.CANCEL));
    }

    public static void open(ItemCooldownsPlugin plugin, Player player, int ruleIndex, boolean sound) {
        RuleListMenu.markCurrentSilent(player);
        ConfirmDeleteMenu menu = new ConfirmDeleteMenu(plugin, player, ruleIndex);
        player.openInventory(menu.inventory);
        if (sound) {
            MenuSounds.confirm(player);
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public void onClick(Player player, InventoryClickEvent event) {
        int raw = event.getRawSlot();
        if (raw == CANCEL) {
            CooldownRule rule = plugin.rules().get(ruleIndex);
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (!stillOpen(player) || rule == null) {
                    return;
                }
                RuleEditorMenu.open(
                        plugin,
                        player,
                        ruleIndex,
                        rule.icon(),
                        rule.leftSeconds(),
                        rule.rightSeconds(),
                        rule.potion() != null,
                        rule.cmd() != null,
                        rule.itemModel() != null,
                        true);
            });
            return;
        }
        if (raw != CONFIRM) {
            return;
        }
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

    private boolean stillOpen(Player player) {
        return player.isOnline() && player.getOpenInventory().getTopInventory().getHolder() == this;
    }

    @Override
    public void onDrag(Player player, InventoryDragEvent event) {
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
