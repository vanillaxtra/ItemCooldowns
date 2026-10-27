package net.mattwhyy.itemcooldowns.listeners;

import net.mattwhyy.itemcooldowns.ItemCooldownsPlugin;
import net.mattwhyy.itemcooldowns.features.cooldown.ClickSide;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;

public final class EntityShootBowListener implements Listener {
    private final ItemCooldownsPlugin plugin;

    public EntityShootBowListener(ItemCooldownsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onShoot(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (!plugin.cooldowns().use(player, event.getBow(), ClickSide.RIGHT)) {
            event.setCancelled(true);
        }
    }
}
