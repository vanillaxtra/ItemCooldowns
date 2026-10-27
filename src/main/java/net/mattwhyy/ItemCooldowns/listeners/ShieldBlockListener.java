package net.mattwhyy.itemcooldowns.listeners;

import net.mattwhyy.itemcooldowns.ItemCooldownsPlugin;
import net.mattwhyy.itemcooldowns.features.cooldown.ClickSide;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public final class ShieldBlockListener implements Listener {
    private final ItemCooldownsPlugin plugin;

    public ShieldBlockListener(ItemCooldownsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlock(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player player) || !player.isBlocking()) {
            return;
        }
        ItemStack shield = player.getInventory().getItemInOffHand();
        if (shield.getType() != Material.SHIELD) {
            shield = player.getInventory().getItemInMainHand();
        }
        if (shield.getType() != Material.SHIELD) {
            return;
        }
        plugin.cooldowns().use(player, shield, ClickSide.RIGHT);
    }
}
