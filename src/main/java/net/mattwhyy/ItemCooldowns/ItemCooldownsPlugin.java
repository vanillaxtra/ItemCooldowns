package net.mattwhyy.itemcooldowns;

import net.mattwhyy.itemcooldowns.commands.GetCooldownCommand;
import net.mattwhyy.itemcooldowns.commands.ItemCooldownsCommand;
import net.mattwhyy.itemcooldowns.commands.ResetCooldownCommand;
import net.mattwhyy.itemcooldowns.commands.SetCooldownCommand;
import net.mattwhyy.itemcooldowns.features.cooldown.CooldownRepository;
import net.mattwhyy.itemcooldowns.features.cooldown.CooldownService;
import net.mattwhyy.itemcooldowns.features.gui.MenuHolder;
import net.mattwhyy.itemcooldowns.hooks.ModrinthUpdateChecker;
import net.mattwhyy.itemcooldowns.listeners.CommandSendListener;
import net.mattwhyy.itemcooldowns.listeners.EntityShootBowListener;
import net.mattwhyy.itemcooldowns.listeners.GuiClickListener;
import net.mattwhyy.itemcooldowns.listeners.GuiCloseListener;
import net.mattwhyy.itemcooldowns.listeners.GuiDragListener;
import net.mattwhyy.itemcooldowns.listeners.MeleeAttackListener;
import net.mattwhyy.itemcooldowns.listeners.PlayerDropItemListener;
import net.mattwhyy.itemcooldowns.listeners.PlayerElytraBoostListener;
import net.mattwhyy.itemcooldowns.listeners.PlayerInteractListener;
import net.mattwhyy.itemcooldowns.listeners.PlayerItemConsumeListener;
import net.mattwhyy.itemcooldowns.listeners.PlayerLaunchProjectileListener;
import net.mattwhyy.itemcooldowns.listeners.PlayerQuitListener;
import net.mattwhyy.itemcooldowns.listeners.PlayerRiptideListener;
import net.mattwhyy.itemcooldowns.listeners.ShieldBlockListener;
import net.mattwhyy.itemcooldowns.listeners.TotemResurrectListener;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class ItemCooldownsPlugin extends JavaPlugin {
    private final Messages messages = new Messages();
    private CooldownRepository rules;
    private CooldownService cooldowns;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        messages.load(this);
        rules = new CooldownRepository(this);
        rules.load();
        cooldowns = new CooldownService(this);
        var pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new GuiClickListener(this), this);
        pluginManager.registerEvents(new GuiDragListener(this), this);
        pluginManager.registerEvents(new GuiCloseListener(this), this);
        pluginManager.registerEvents(new PlayerQuitListener(this), this);
        pluginManager.registerEvents(new PlayerInteractListener(this), this);
        pluginManager.registerEvents(new PlayerItemConsumeListener(this), this);
        pluginManager.registerEvents(new EntityShootBowListener(this), this);
        pluginManager.registerEvents(new PlayerLaunchProjectileListener(this), this);
        pluginManager.registerEvents(new PlayerRiptideListener(this), this);
        pluginManager.registerEvents(new MeleeAttackListener(this), this);
        pluginManager.registerEvents(new ShieldBlockListener(this), this);
        pluginManager.registerEvents(new TotemResurrectListener(this), this);
        pluginManager.registerEvents(new PlayerElytraBoostListener(this), this);
        pluginManager.registerEvents(new PlayerDropItemListener(), this);
        pluginManager.registerEvents(new CommandSendListener(), this);
        bind("itemcooldowns", new ItemCooldownsCommand(this));
        bind("setcooldown", new SetCooldownCommand(this));
        bind("getcooldown", new GetCooldownCommand(this));
        bind("resetcooldown", new ResetCooldownCommand(this));
        startMetrics();
        ModrinthUpdateChecker.start(this);
    }

    private void startMetrics() {
        try {
            new org.bstats.bukkit.Metrics(this, 34985);
        } catch (NoClassDefFoundError | Exception exception) {
            getLogger().warning("Could not start bStats: " + exception.getMessage());
        }
    }

    public boolean reloadSettings() {
        messages.load(this);
        return rules.load();
    }

    @Override
    public void onDisable() {
        if (rules != null) {
            rules.saveNow();
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getOpenInventory().getTopInventory().getHolder() instanceof MenuHolder) {
                player.closeInventory();
            }
        }
        Bukkit.getScheduler().cancelTasks(this);
    }

    public CooldownRepository rules() {
        return rules;
    }

    public CooldownService cooldowns() {
        return cooldowns;
    }

    private void bind(String name, Object executor) {
        PluginCommand command = getCommand(name);
        if (command == null) {
            getLogger().severe(name + " command missing from plugin.yml");
            return;
        }
        command.setExecutor((org.bukkit.command.CommandExecutor) executor);
        if (executor instanceof org.bukkit.command.TabCompleter completer) {
            command.setTabCompleter(completer);
        }
    }
}
