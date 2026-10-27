package net.mattwhyy.itemcooldowns.features.gui;

import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
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

public final class PresetMenu implements MenuHolder {
    private static final int SIZE = 36;
    private static final int BOOK = 4;
    private static final int BACK = 27;
    private static final int[] SLOTS = concat(GuiItems.rowSlots(1, 6), GuiItems.rowSlots(2, 6));

    private final ItemCooldownsPlugin plugin;
    private final Inventory inventory;
    private final List<CooldownPresets.Preset> presets;
    private boolean silent;

    private PresetMenu(ItemCooldownsPlugin plugin) {
        this.plugin = plugin;
        this.presets = CooldownPresets.all();
        this.inventory = Bukkit.createInventory(this, SIZE, Messages.text(Messages.TITLE_PRESETS));
        paint();
    }

    public static void open(ItemCooldownsPlugin plugin, Player player, boolean sound) {
        RuleListMenu.markCurrentSilent(player);
        PresetMenu menu = new PresetMenu(plugin);
        player.openInventory(menu.inventory);
        if (sound) {
            MenuSounds.open(player);
        }
    }

    private void paint() {
        GuiItems.fill(inventory, GuiItems.filler(plugin));
        inventory.setItem(BOOK, GuiItems.button(plugin, Material.BOOK, Messages.PRESETS, Messages.PRESETS_LORE));
        inventory.setItem(BACK, GuiItems.button(plugin, Material.ARROW, Messages.BACK));
        for (int i = 0; i < presets.size() && i < SLOTS.length; i++) {
            inventory.setItem(SLOTS[i], icon(presets.get(i)));
        }
    }

    private ItemStack icon(CooldownPresets.Preset preset) {
        List<Component> lore = new ArrayList<>();
        lore.add(timeLine(preset));
        if (preset.extraLoreKey() != null) {
            lore.add(Messages.text(preset.extraLoreKey()));
        }
        return GuiItems.named(plugin, preset.icon(), Messages.text(preset.nameKey()), lore, false);
    }

    private static Component timeLine(CooldownPresets.Preset preset) {
        if (preset.left() > 0 && preset.right() > 0) {
            return Messages.text(Messages.PRESET_TIME_BOTH, "seconds", String.valueOf(preset.left()));
        }
        if (preset.left() > 0) {
            return Messages.text(Messages.PRESET_TIME_LEFT, "seconds", String.valueOf(preset.left()));
        }
        return Messages.text(Messages.PRESET_TIME_RIGHT, "seconds", String.valueOf(preset.right()));
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public void onClick(Player player, InventoryClickEvent event) {
        int raw = event.getRawSlot();
        if (raw == BACK) {
            later(player, () -> RuleListMenu.open(plugin, player, 0, true));
            return;
        }
        int index = indexOf(raw);
        if (index < 0 || index >= presets.size()) {
            return;
        }
        CooldownPresets.Preset preset = presets.get(index);
        later(player, () -> apply(player, preset));
    }

    private void apply(Player player, CooldownPresets.Preset preset) {
        List<CooldownRule> rules = new ArrayList<>();
        for (ItemStack item : preset.items()) {
            CooldownRule rule = CooldownRule.fromItem(item, true, false, false, preset.left(), preset.right());
            if (rule != null) {
                rules.add(rule);
            }
        }
        int added = plugin.rules().addMissing(rules);
        if (added == 0) {
            MenuSounds.deny(player);
            Messages.actionBar(player, Messages.PRESET_EXISTS);
            return;
        }
        MenuSounds.save(player);
        Messages.actionBar(player, Messages.PRESET_ADDED, "count", String.valueOf(added));
        RuleListMenu.open(plugin, player, 0, false);
    }

    private static int indexOf(int slot) {
        for (int i = 0; i < SLOTS.length; i++) {
            if (SLOTS[i] == slot) {
                return i;
            }
        }
        return -1;
    }

    private static int[] concat(int[] first, int[] second) {
        int[] all = new int[first.length + second.length];
        System.arraycopy(first, 0, all, 0, first.length);
        System.arraycopy(second, 0, all, first.length, second.length);
        return all;
    }

    private void later(Player player, Runnable action) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!player.isOnline() || player.getOpenInventory().getTopInventory().getHolder() != this) {
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
