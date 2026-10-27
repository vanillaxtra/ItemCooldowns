package net.mattwhyy.itemcooldowns.listeners;

import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent;
import net.mattwhyy.itemcooldowns.ItemCooldownsPlugin;
import net.mattwhyy.itemcooldowns.features.cooldown.ClickSide;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public final class PlayerElytraBoostListener implements Listener {
    private final ItemCooldownsPlugin plugin;

    public PlayerElytraBoostListener(ItemCooldownsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBoost(PlayerElytraBoostEvent event) {
        if (!plugin.cooldowns().use(event.getPlayer(), event.getItemStack(), ClickSide.RIGHT)) {
            event.setCancelled(true);
        }
    }
}
