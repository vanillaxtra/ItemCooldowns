package net.mattwhyy.itemcooldowns.listeners;

import net.mattwhyy.itemcooldowns.ItemCooldownsPlugin;
import net.mattwhyy.itemcooldowns.Messages;
import net.mattwhyy.itemcooldowns.features.gui.MenuHolder;
import net.mattwhyy.itemcooldowns.features.gui.MenuSounds;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public final class GuiClickListener implements Listener {
    private final ItemCooldownsPlugin plugin;

    public GuiClickListener(ItemCooldownsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (!(event.getView().getTopInventory().getHolder() instanceof MenuHolder holder)) {
            return;
        }
        event.setCancelled(true);
        if (!player.hasPermission("itemcooldowns.admin")) {
            holder.markSilent();
            MenuSounds.deny(player);
            Messages.actionBar(player, Messages.NO_PERMISSION);
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (player.isOnline()) {
                    player.closeInventory();
                }
            });
            return;
        }
        holder.onClick(player, event);
    }
}
