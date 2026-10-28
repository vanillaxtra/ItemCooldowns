package net.mattwhyy.itemcooldowns.listeners;

import net.mattwhyy.itemcooldowns.ItemCooldownsPlugin;
import net.mattwhyy.itemcooldowns.features.gui.GuiItems;
import net.mattwhyy.itemcooldowns.features.gui.MenuHolder;
import net.mattwhyy.itemcooldowns.features.gui.MenuSounds;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

public final class GuiCloseListener implements Listener {
    private final ItemCooldownsPlugin plugin;

    public GuiCloseListener(ItemCooldownsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }
        if (!(event.getInventory().getHolder() instanceof MenuHolder holder)) {
            return;
        }
        holder.onClose(player);
        GuiItems.strip(plugin, player);
        if (holder.consumeSilent()) {
            return;
        }
        MenuSounds.close(player);
    }
}
