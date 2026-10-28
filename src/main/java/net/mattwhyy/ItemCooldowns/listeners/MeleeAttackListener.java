package net.mattwhyy.itemcooldowns.listeners;

import net.mattwhyy.itemcooldowns.ItemCooldownsPlugin;
import net.mattwhyy.itemcooldowns.features.cooldown.ClickSide;
import net.mattwhyy.itemcooldowns.features.cooldown.CooldownRule;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public final class MeleeAttackListener implements Listener {
    private final ItemCooldownsPlugin plugin;

    public MeleeAttackListener(ItemCooldownsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) {
            return;
        }
        if (CooldownRule.spear(player.getInventory().getItemInMainHand().getType())) {
            return;
        }
        if (!plugin.cooldowns().use(player, player.getInventory().getItemInMainHand(), ClickSide.LEFT)) {
            event.setCancelled(true);
        }
    }
}
