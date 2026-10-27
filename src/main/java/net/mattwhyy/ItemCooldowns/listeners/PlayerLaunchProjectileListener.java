package net.mattwhyy.itemcooldowns.listeners;

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import net.mattwhyy.itemcooldowns.ItemCooldownsPlugin;
import net.mattwhyy.itemcooldowns.features.cooldown.ClickSide;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public final class PlayerLaunchProjectileListener implements Listener {
    private final ItemCooldownsPlugin plugin;

    public PlayerLaunchProjectileListener(ItemCooldownsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onLaunch(PlayerLaunchProjectileEvent event) {
        if (!plugin.cooldowns().use(event.getPlayer(), event.getItemStack(), ClickSide.RIGHT)) {
            event.setCancelled(true);
        }
    }
}
