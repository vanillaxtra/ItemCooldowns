package net.mattwhyy.itemcooldowns;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class Messages {
    public static final String PLAYERS_ONLY = "command.players-only";
    public static final String NO_PERMISSION = "command.no-permission";
    public static final String RELOADED = "command.reloaded";
    public static final String USAGE = "command.usage";
    public static final String SET_USAGE = "command.set-usage";
    public static final String GET_USAGE = "command.get-usage";
    public static final String RESET_USAGE = "command.reset-usage";
    public static final String SET_OK = "command.set-ok";
    public static final String GET_OK = "command.get-ok";
    public static final String GET_NONE = "command.get-none";
    public static final String RESET_ONE = "command.reset-one";
    public static final String RESET_ALL = "command.reset-all";
    public static final String BAD_ITEM = "command.bad-item";
    public static final String TITLE_LIST = "gui.title.list";
    public static final String TITLE_EDITOR = "gui.title.editor";
    public static final String TITLE_PRESETS = "gui.title.presets";
    public static final String TITLE_CONFIRM = "gui.title.confirm";
    public static final String HELP_NAME = "gui.help-name";
    public static final String LIST_LORE = "gui.list-lore";
    public static final String PRESETS = "gui.presets";
    public static final String PRESETS_LORE = "gui.presets-lore";
    public static final String ADD = "gui.add";
    public static final String PREVIOUS = "gui.previous";
    public static final String NEXT = "gui.next";
    public static final String BACK = "gui.back";
    public static final String SAVE = "gui.save";
    public static final String DELETE = "gui.delete";
    public static final String CONFIRM = "gui.confirm";
    public static final String CANCEL = "gui.cancel";
    public static final String SAVED = "gui.saved";
    public static final String DELETED = "gui.deleted";
    public static final String NEED_ITEM = "gui.need-item";
    public static final String NEED_TIME = "gui.need-time";
    public static final String DUPLICATE = "gui.duplicate";
    public static final String EDITOR_BOOK = "gui.editor-book";
    public static final String EDITOR_LORE = "gui.editor-lore";
    public static final String EDITOR_GROUP_LORE = "gui.editor-group-lore";
    public static final String GROUP_SELECT_ALL = "gui.group-select-all";
    public static final String NEED_ONE_TYPE = "gui.need-one-type";
    public static final String RULE_GROUP_COUNT = "gui.rule-group-count";
    public static final String ITEM_SLOT = "gui.item-slot";
    public static final String ITEM_SLOT_EMPTY = "gui.item-slot-empty";
    public static final String TIME = "gui.time";
    public static final String COOLDOWN_USE = "gui.cooldown-use";
    public static final String COOLDOWN_ATTACK = "gui.cooldown-attack";
    public static final String COOLDOWN_OFF_USE = "gui.cooldown-off-use";
    public static final String COOLDOWN_OFF_ATTACK = "gui.cooldown-off-attack";
    public static final String COOLDOWN_UNSET = "gui.cooldown-unset";
    public static final String DROP_HINT = "gui.drop-hint";
    public static final String LEFT = "gui.left";
    public static final String RIGHT = "gui.right";
    public static final String SECONDS_OFF = "gui.seconds-off";
    public static final String SECONDS_ON = "gui.seconds-on";
    public static final String ADJUST_HINT = "gui.adjust-hint";
    public static final String MATCH_POTION_ON = "gui.match-potion-on";
    public static final String MATCH_POTION_OFF = "gui.match-potion-off";
    public static final String MATCH_CMD_ON = "gui.match-cmd-on";
    public static final String MATCH_CMD_OFF = "gui.match-cmd-off";
    public static final String MATCH_MODEL_ON = "gui.match-model-on";
    public static final String MATCH_MODEL_OFF = "gui.match-model-off";
    public static final String RULE_TIME = "gui.rule-time";
    public static final String RULE_LEFT = "gui.rule-left";
    public static final String RULE_RIGHT = "gui.rule-right";
    public static final String RULE_MATCH_ALL = "gui.rule-match-all";
    public static final String RULE_MATCH_POTION = "gui.rule-match-potion";
    public static final String RULE_MATCH_CMD = "gui.rule-match-cmd";
    public static final String RULE_MATCH_MODEL = "gui.rule-match-model";
    public static final String PRESET_LORE = "gui.preset-lore";
    public static final String PRESET_TIME = "gui.preset-time";
    public static final String PRESET_TIME_RIGHT = "gui.preset-time-right";
    public static final String PRESET_TIME_LEFT = "gui.preset-time-left";
    public static final String PRESET_TIME_BOTH = "gui.preset-time-both";
    public static final String PRESET_ENDER_PEARL = "gui.preset-name.ender-pearl";
    public static final String PRESET_TRIDENT = "gui.preset-name.trident";
    public static final String PRESET_WIND_CHARGE = "gui.preset-name.wind-charge";
    public static final String PRESET_MACE = "gui.preset-name.mace";
    public static final String PRESET_SPEAR = "gui.preset-name.spear";
    public static final String PRESET_SPEAR_LORE = "gui.preset-name.spear-lore";
    public static final String PRESET_CHORUS = "gui.preset-name.chorus";
    public static final String PRESET_FIREWORK = "gui.preset-name.firework";
    public static final String PRESET_GOLDEN_APPLE = "gui.preset-name.golden-apple";
    public static final String PRESET_ENCHANTED_APPLE = "gui.preset-name.enchanted-apple";
    public static final String PRESET_TURTLE = "gui.preset-name.turtle";
    public static final String PRESET_TURTLE_LORE = "gui.preset-name.turtle-lore";
    public static final String PRESET_HEALING = "gui.preset-name.healing";
    public static final String PRESET_HEALING_LORE = "gui.preset-name.healing-lore";
    public static final String PRESET_POTIONS = "gui.preset-name.potions";
    public static final String PRESET_POTIONS_LORE = "gui.preset-name.potions-lore";
    public static final String PRESET_CATEGORY_COUNT = "gui.preset-category-count";
    public static final String CATEGORY_LORE = "gui.category-lore";
    public static final String PRESET_ADDED = "gui.preset-added";
    public static final String PRESET_EXISTS = "gui.preset-exists";
    public static final String ON_COOLDOWN = "gui.on-cooldown";

    private static volatile FileConfiguration file = new YamlConfiguration();

    public void load(JavaPlugin plugin) {
        File target = new File(plugin.getDataFolder(), "messages.yml");
        int bundled = bundledVersion(plugin);
        if (!target.exists() || version(target) < bundled) {
            plugin.saveResource("messages.yml", true);
            plugin.getLogger().info("updated messages.yml");
        }
        YamlConfiguration loaded = new YamlConfiguration();
        try {
            loaded.load(target);
            file = loaded;
        } catch (Exception exception) {
            plugin.getLogger().warning("could not read messages.yml, keeping the previous text");
        }
    }

    private static int bundledVersion(JavaPlugin plugin) {
        try (InputStream in = plugin.getResource("messages.yml")) {
            if (in == null) {
                return 0;
            }
            YamlConfiguration bundled = YamlConfiguration.loadConfiguration(new InputStreamReader(in, StandardCharsets.UTF_8));
            return bundled.getInt("version");
        } catch (Exception exception) {
            return 0;
        }
    }

    private static int version(File target) {
        YamlConfiguration loaded = new YamlConfiguration();
        try {
            loaded.load(target);
        } catch (Exception exception) {
            return 0;
        }
        return loaded.getInt("version");
    }

    public static Component text(String path, String... replacements) {
        String raw = file.getString(path);
        if (raw == null) {
            raw = path;
        }
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            raw = raw.replace("{" + replacements[i] + "}", replacements[i + 1]);
        }
        try {
            return readable(MiniMessage.miniMessage().deserialize("<!italic>" + raw));
        } catch (Exception exception) {
            return Component.text(raw).decoration(TextDecoration.ITALIC, false);
        }
    }

    private static Component readable(Component component) {
        return bake(component, Style.empty());
    }

    private static Component bake(Component component, Style parent) {
        Style style = component.style().merge(parent, Style.Merge.Strategy.IF_ABSENT_ON_TARGET);
        if (style.color() == null) {
            style = style.color(NamedTextColor.GRAY);
        }
        if (style.decoration(TextDecoration.ITALIC) == TextDecoration.State.NOT_SET) {
            style = style.decoration(TextDecoration.ITALIC, false);
        }
        List<Component> children = new ArrayList<>(component.children().size());
        for (Component child : component.children()) {
            children.add(bake(child, style));
        }
        return component.style(style).children(children);
    }

    public static void actionBar(Player player, String path, String... replacements) {
        player.sendActionBar(text(path, replacements));
    }
}
