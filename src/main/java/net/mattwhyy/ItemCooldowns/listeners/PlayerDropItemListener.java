package net.mattwhyy.itemcooldowns.listeners;

import net.mattwhyy.itemcooldowns.features.gui.RuleEditorMenu;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

public final class PlayerDropItemListener implements Listener {
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (!(player.getOpenInventory().getTopInventory().getHolder() instanceof RuleEditorMenu menu)) {
            return;
        }
        if (!menu.tryDropBarrier(player, event.getItemDrop().getItemStack())) {
            return;
        }
        event.setCancelled(true);
        event.getItemDrop().remove();
    }
}
