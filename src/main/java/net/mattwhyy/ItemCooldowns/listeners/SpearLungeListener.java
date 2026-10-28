package net.mattwhyy.itemcooldowns.listeners;

import io.papermc.paper.event.player.PlayerArmSwingEvent;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.keys.EnchantmentKeys;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.mattwhyy.itemcooldowns.ItemCooldownsPlugin;
import net.mattwhyy.itemcooldowns.features.cooldown.ClickSide;
import net.mattwhyy.itemcooldowns.features.cooldown.CooldownRule;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

public final class SpearLungeListener implements Listener {
    private static final int SWAP_LOCK_TICKS = 10;
    private static final int CHARGE_RESET_PERIOD = 4;

    private final ItemCooldownsPlugin plugin;
    private final Map<UUID, Integer> handledTick = new HashMap<>();
    private final Map<UUID, Long> swapLockUntil = new HashMap<>();
    private final Map<UUID, BukkitTask> chargeReset = new HashMap<>();

    public SpearLungeListener(ItemCooldownsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSwing(PlayerArmSwingEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        tryLunge(event.getPlayer(), event.getPlayer().getInventory().getItemInMainHand());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action != Action.LEFT_CLICK_AIR && action != Action.LEFT_CLICK_BLOCK) {
            return;
        }
        ItemStack stack = event.getItem();
        if (stack == null) {
            stack = event.getPlayer().getInventory().getItemInMainHand();
        }
        tryLunge(event.getPlayer(), stack);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player) || !swapLocked(player)) {
            return;
        }
        if (!CooldownRule.spear(player.getInventory().getItemInMainHand().getType())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        if (swapLocked(player)) {
            event.setCancelled(true);
            return;
        }
        ItemStack from = player.getInventory().getItem(event.getPreviousSlot());
        ItemStack to = player.getInventory().getItem(event.getNewSlot());
        if (guarded(from) || guarded(to)) {
            markSwap(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onOffhand(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        if (swapLocked(player)) {
            event.setCancelled(true);
            return;
        }
        if (guarded(event.getMainHandItem()) || guarded(event.getOffHandItem())) {
            markSwap(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (swapLocked(player)) {
            event.setCancelled(true);
            return;
        }
        ItemStack before = player.getInventory().getItemInMainHand();
        if (!guarded(before) && !guarded(event.getCurrentItem()) && !guarded(event.getCursor())) {
            return;
        }
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (!player.isOnline()) {
                return;
            }
            ItemStack after = player.getInventory().getItemInMainHand();
            if ((guarded(before) || guarded(after)) && !same(before, after)) {
                markSwap(player);
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (swapLocked(player) && spear(event.getItemDrop().getItemStack())) {
            event.setCancelled(true);
            return;
        }
        if (guarded(event.getItemDrop().getItemStack())) {
            markSwap(player);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        forget(event.getPlayer().getUniqueId());
    }

    private void tryLunge(Player player, ItemStack stack) {
        if (!lungeAttempt(player, stack) || !guarded(stack)) {
            return;
        }
        int tick = plugin.getServer().getCurrentTick();
        Integer last = handledTick.put(player.getUniqueId(), tick);
        if (last != null && last == tick) {
            return;
        }
        if (!plugin.cooldowns().use(player, stack, ClickSide.LUNGE)) {
            player.resetCooldown();
            return;
        }
        lockSwap(player);
        startChargeReset(player);
    }

    private boolean lungeAttempt(Player player, ItemStack stack) {
        if (stack == null || !CooldownRule.spear(stack.getType()) || !hasLunge(stack)) {
            return false;
        }
        return player.getAttackCooldown() >= 0.95f;
    }

    private void markSwap(Player player) {
        player.resetCooldown();
    }

    private void lockSwap(Player player) {
        long until = System.currentTimeMillis() + SWAP_LOCK_TICKS * 50L;
        swapLockUntil.put(player.getUniqueId(), until);
    }

    private boolean swapLocked(Player player) {
        Long until = swapLockUntil.get(player.getUniqueId());
        return until != null && until > System.currentTimeMillis();
    }

    private void startChargeReset(Player player) {
        cancelChargeReset(player.getUniqueId());
        BukkitTask task = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (!player.isOnline()) {
                cancelChargeReset(player.getUniqueId());
                return;
            }
            ItemStack hand = player.getInventory().getItemInMainHand();
            if (!CooldownRule.spear(hand.getType())) {
                return;
            }
            if (plugin.cooldowns().allow(player, hand, ClickSide.LUNGE)) {
                cancelChargeReset(player.getUniqueId());
                return;
            }
            player.resetCooldown();
        }, CHARGE_RESET_PERIOD, CHARGE_RESET_PERIOD);
        chargeReset.put(player.getUniqueId(), task);
    }

    private void cancelChargeReset(UUID playerId) {
        BukkitTask task = chargeReset.remove(playerId);
        if (task != null) {
            task.cancel();
        }
    }

    private void forget(UUID playerId) {
        handledTick.remove(playerId);
        swapLockUntil.remove(playerId);
        cancelChargeReset(playerId);
    }

    private boolean guarded(ItemStack stack) {
        if (!spear(stack)) {
            return false;
        }
        CooldownRule rule = plugin.rules().best(stack);
        return rule != null && rule.seconds(ClickSide.LUNGE) > 0;
    }

    private static boolean spear(ItemStack stack) {
        return stack != null && CooldownRule.spear(stack.getType());
    }

    private static boolean same(ItemStack left, ItemStack right) {
        if (left == null || right == null) {
            return left == right;
        }
        return left.isSimilar(right);
    }

    private static boolean hasLunge(ItemStack stack) {
        Enchantment lunge = RegistryAccess.registryAccess()
                .getRegistry(RegistryKey.ENCHANTMENT)
                .get(EnchantmentKeys.LUNGE);
        return lunge != null && stack.getEnchantmentLevel(lunge) > 0;
    }
}
