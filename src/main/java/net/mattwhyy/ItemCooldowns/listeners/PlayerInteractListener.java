package net.mattwhyy.itemcooldowns.listeners;

import net.mattwhyy.itemcooldowns.ItemCooldownsPlugin;
import net.mattwhyy.itemcooldowns.features.cooldown.ClickSide;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public final class PlayerInteractListener implements Listener {
    private final ItemCooldownsPlugin plugin;

    public PlayerInteractListener(ItemCooldownsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (event.getHand() != EquipmentSlot.HAND && event.getHand() != EquipmentSlot.OFF_HAND) {
            return;
        }
        Player player = event.getPlayer();
        ItemStack stack = event.getItem();
        if (stack == null || stack.getType().isAir() || deferred(player, stack.getType())) {
            return;
        }
        if (!plugin.cooldowns().use(player, stack, ClickSide.RIGHT)) {
            event.setCancelled(true);
        }
    }

    private static boolean deferred(Player player, Material material) {
        if (material.isEdible()
                || material == Material.POTION
                || material == Material.MILK_BUCKET
                || material == Material.HONEY_BOTTLE
                || material == Material.BOW
                || material == Material.CROSSBOW
                || material == Material.TRIDENT
                || material == Material.SHIELD
                || material == Material.ENDER_PEARL
                || material == Material.EGG
                || material == Material.SNOWBALL
                || material == Material.EXPERIENCE_BOTTLE
                || material == Material.SPLASH_POTION
                || material == Material.LINGERING_POTION) {
            return true;
        }
        if (material == Material.WIND_CHARGE) {
            return true;
        }
        return material == Material.FIREWORK_ROCKET && player.isGliding();
    }
}
