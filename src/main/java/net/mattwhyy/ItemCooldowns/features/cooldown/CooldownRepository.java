package net.mattwhyy.itemcooldowns.features.cooldown;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.mattwhyy.itemcooldowns.ItemCooldownsPlugin;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitTask;

public final class CooldownRepository {
    public static final int SCHEMA = 5;
    private static final long SAVE_DELAY = 40L;

    private final ItemCooldownsPlugin plugin;
    private final List<CooldownRule> rules = new ArrayList<>();
    private BukkitTask saveTask;

    public CooldownRepository(ItemCooldownsPlugin plugin) {
        this.plugin = plugin;
    }

    public List<CooldownRule> rules() {
        return List.copyOf(rules);
    }

    public CooldownRule get(int index) {
        if (index < 0 || index >= rules.size()) {
            return null;
        }
        return rules.get(index);
    }

    public CooldownRule best(ItemStack stack) {
        ItemFingerprint print = ItemFingerprint.of(stack);
        if (print == null) {
            return null;
        }
        CooldownMatcher.Probe probe = print.probe();
        CooldownRule best = null;
        int bestScore = -1;
        for (CooldownRule rule : rules) {
            if (!rule.active()) {
                continue;
            }
            int scored = rule.score(probe);
            if (scored > bestScore) {
                bestScore = scored;
                best = rule;
            }
        }
        return best;
    }

    public int indexOf(CooldownRule rule) {
        for (int i = 0; i < rules.size(); i++) {
            if (rules.get(i).sameIdentity(rule)) {
                return i;
            }
        }
        return -1;
    }

    public int indexOfFilter(CooldownMatcher.Filter filter) {
        for (int i = 0; i < rules.size(); i++) {
            CooldownRule rule = rules.get(i);
            if (rule.grouped()) {
                continue;
            }
            if (CooldownMatcher.sameFilter(rule.filter(), filter)) {
                return i;
            }
        }
        return -1;
    }

    public void add(CooldownRule rule) {
        rules.add(rule);
        scheduleSave();
    }

    public int addMissing(List<CooldownRule> incoming) {
        int added = 0;
        for (CooldownRule rule : incoming) {
            if (indexOfFilter(rule.filter()) >= 0) {
                continue;
            }
            rules.add(rule);
            added++;
        }
        if (added > 0) {
            scheduleSave();
        }
        return added;
    }

    public boolean enabled(ItemStack stack) {
        CooldownRule rule = CooldownRule.fromItem(stack, true, false, false, 0);
        return rule != null && indexOf(rule) >= 0;
    }

    public boolean enabled(CooldownRule rule) {
        return indexOf(rule) >= 0;
    }

    public boolean toggle(CooldownRule rule) {
        int existing = indexOf(rule);
        if (existing >= 0) {
            delete(existing);
            return false;
        }
        add(rule);
        return true;
    }

    public boolean toggle(ItemStack stack, int seconds) {
        CooldownRule rule = CooldownRule.fromItem(stack, true, false, false, seconds);
        if (rule == null) {
            return false;
        }
        return toggle(rule);
    }

    public void setActive(int index, boolean active) {
        CooldownRule old = get(index);
        if (old == null || old.active() == active) {
            return;
        }
        replace(index, old.withActive(active));
    }

    public void replace(int index, CooldownRule rule) {
        if (index < 0 || index >= rules.size()) {
            return;
        }
        rules.set(index, rule);
        scheduleSave();
    }

    public void delete(int index) {
        if (index < 0 || index >= rules.size()) {
            return;
        }
        rules.remove(index);
        scheduleSave();
    }

    public void clear() {
        rules.clear();
        scheduleSave();
    }

    public boolean load() {
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();
        migrate(config);
        plugin.reloadConfig();
        config = plugin.getConfig();
        rules.clear();
        ConfigurationSection section = config.getConfigurationSection("rules");
        if (section == null) {
            return true;
        }
        for (String key : section.getKeys(false)) {
            ConfigurationSection entry = section.getConfigurationSection(key);
            if (entry == null) {
                continue;
            }
            Material material = CooldownRule.materialOf(entry.getString("material"));
            if (material == null) {
                plugin.getLogger().warning("Skipped cooldown rule with bad material: " + entry.getString("material"));
                continue;
            }
            PotionType potion = ItemFingerprint.potionOf(entry.getString("potion"));
            Integer cmd = entry.contains("custom-model-data") ? entry.getInt("custom-model-data") : null;
            NamespacedKey model = ItemFingerprint.modelOf(entry.getString("item-model"));
            CooldownRule.Group group = CooldownRule.groupOf(entry.getString("group"));
            Set<String> enabled = new LinkedHashSet<>(entry.getStringList("enabled"));
            if (group != CooldownRule.Group.NONE && enabled.isEmpty()) {
                enabled.addAll(CooldownRule.groupKeys(group));
            }
            rules.add(new CooldownRule(
                    entry.getString("id"),
                    material,
                    potion,
                    cmd,
                    model,
                    entry.getInt("left"),
                    entry.getInt("right"),
                    group,
                    enabled,
                    entry.getBoolean("active", true)));
        }
        boolean stale = config.getInt("schema") < SCHEMA;
        if (stale) {
            rules.replaceAll(CooldownRule::normalized);
            saveNow();
        }
        return true;
    }

    public void saveNow() {
        if (saveTask != null) {
            saveTask.cancel();
            saveTask = null;
        }
        FileConfiguration config = plugin.getConfig();
        config.set("schema", SCHEMA);
        config.set("cooldowns", null);
        config.set("rules", null);
        int i = 0;
        for (CooldownRule rule : rules) {
            String path = "rules." + i++;
            config.set(path + ".id", rule.id());
            config.set(path + ".material", rule.material().name());
            config.set(path + ".potion", rule.potion() == null ? null : rule.potion().name());
            config.set(path + ".custom-model-data", rule.cmd());
            config.set(path + ".item-model", rule.itemModel() == null ? null : rule.itemModel().asString());
            config.set(path + ".left", rule.leftSeconds());
            config.set(path + ".right", rule.rightSeconds());
            config.set(path + ".group", rule.grouped() ? rule.group().name() : null);
            config.set(path + ".enabled", rule.grouped() ? List.copyOf(rule.enabled()) : null);
            config.set(path + ".active", rule.active());
        }
        plugin.saveConfig();
    }

    public void scheduleSave() {
        if (saveTask != null) {
            saveTask.cancel();
        }
        saveTask = plugin.getServer().getScheduler().runTaskLater(plugin, this::saveNow, SAVE_DELAY);
    }

    private void migrate(FileConfiguration config) {
        if (config.getInt("schema") >= SCHEMA) {
            return;
        }
        ConfigurationSection old = config.getConfigurationSection("cooldowns");
        if (old == null) {
            return;
        }
        List<CooldownRule> migrated = new ArrayList<>();
        for (String key : old.getKeys(false)) {
            Material material = CooldownRule.materialOf(key);
            if (material == null) {
                plugin.getLogger().warning("Could not migrate old cooldown: " + key);
                continue;
            }
            int seconds = Math.max(0, old.getInt(key));
            migrated.add(new CooldownRule(
                    null,
                    material,
                    null,
                    null,
                    null,
                    CooldownRule.leftOf(material, seconds),
                    CooldownRule.rightOf(material, seconds)));
        }
        rules.clear();
        rules.addAll(migrated);
        config.set("schema", SCHEMA);
        saveNow();
        plugin.getLogger().info("Migrated old material cooldowns");
    }
}
