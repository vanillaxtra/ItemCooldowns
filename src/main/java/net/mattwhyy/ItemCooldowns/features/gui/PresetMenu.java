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
    private static final int[] SLOTS = GuiItems.rowsOf(6, 1, 2);

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
        refresh();
    }

    private void refresh() {
        for (int i = 0; i < SLOTS.length; i++) {
            if (i < presets.size()) {
                inventory.setItem(SLOTS[i], icon(presets.get(i)));
            }
        }
    }

    private ItemStack icon(CooldownPresets.Preset preset) {
        CooldownRule rule = preset.rule();
        boolean on = rule != null && plugin.rules().enabled(rule);
        List<Component> lore = new ArrayList<>();
        lore.add(on ? timeLine(preset) : Messages.text(Messages.SECONDS_OFF));
        if (preset.extraLoreKey() != null) {
            lore.add(Messages.text(preset.extraLoreKey()));
        }
        return GuiItems.named(plugin, preset.icon(), Messages.text(preset.nameKey()), lore, on);
    }

    static Component timeLine(CooldownPresets.Preset preset) {
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
        if (raw >= SIZE) {
            MenuHolder.freePlayerInv(event);
            return;
        }
        if (raw == BACK) {
            later(player, () -> RuleListMenu.open(plugin, player, 0, true));
            return;
        }
        int index = GuiItems.indexOf(SLOTS, raw);
        if (index < 0 || index >= presets.size()) {
            return;
        }
        CooldownRule rule = presets.get(index).rule();
        if (rule == null) {
            return;
        }
        boolean on = plugin.rules().toggle(rule);
        refresh();
        if (on) {
            MenuSounds.toggleOn(player);
        } else {
            MenuSounds.toggleOff(player);
        }
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
