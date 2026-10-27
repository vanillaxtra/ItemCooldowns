package net.mattwhyy.itemcooldowns.listeners;

import net.mattwhyy.itemcooldowns.ItemCooldownsPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PlayerQuitListener implements Listener {
    private final ItemCooldownsPlugin plugin;

    public PlayerQuitListener(ItemCooldownsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.cooldowns().forget(event.getPlayer().getUniqueId());
    }
}
