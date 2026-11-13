package net.mattwhyy.itemcooldowns.updates;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.sound.Sound.Source;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.mattwhyy.itemcooldowns.ItemCooldownsPlugin;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class UpdateChecker implements Listener {
    private static final String MODRINTH_SLUG = "itemcooldowns";
    private static final String PROJECT_TYPE = "plugin";
    private static final String USER_AGENT = "ItemCooldowns-UpdateChecker/1.0";
    private static final String PERMISSION = "itemcooldowns.update.notify";
    private static final int JOIN_DELAY_TICKS = 40;

    private final ItemCooldownsPlugin plugin;
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8)).build();
    private final Map<UUID, LocalDate> notified = new HashMap<>();
    private volatile String latestVersion;
    private volatile String latestUrl;
    private volatile boolean checked;

    public UpdateChecker(ItemCooldownsPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            check();
            if (updateAvailable()) {
                logBanner();
            }
        });
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission(PERMISSION) || notifiedToday(player)) {
            return;
        }
        if (checked) {
            if (updateAvailable()) {
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> notify(player), JOIN_DELAY_TICKS);
            }
            return;
        }
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            check();
            if (!updateAvailable()) {
                return;
            }
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> notify(player), JOIN_DELAY_TICKS);
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        notified.remove(event.getPlayer().getUniqueId());
    }

    private boolean notifiedToday(Player player) {
        return LocalDate.now().equals(notified.get(player.getUniqueId()));
    }

    private boolean updateAvailable() {
        String latest = latestVersion;
        return latest != null && Version.compare(latest, current()) > 0;
    }

    private String current() {
        return plugin.getPluginMeta().getVersion();
    }

    private void check() {
        try {
            URI uri = URI.create("https://api.modrinth.com/v2/project/" + MODRINTH_SLUG + "/version");
            HttpRequest request = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofSeconds(8))
                    .header("User-Agent", USER_AGENT)
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                plugin.getLogger().warning("Update check failed: HTTP " + response.statusCode());
                return;
            }
            pickNewest(JsonParser.parseString(response.body()).getAsJsonArray());
            checked = true;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        } catch (Exception exception) {
            plugin.getLogger().warning("Update check failed: " + exception.getMessage());
        }
    }

    private void pickNewest(JsonArray versions) {
        String bestRelease = null;
        String bestReleaseId = null;
        String bestAny = null;
        String bestAnyId = null;
        for (JsonElement element : versions) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject version = element.getAsJsonObject();
            if (!version.has("version_number") || !version.has("id")) {
                continue;
            }
            String number = version.get("version_number").getAsString();
            String id = version.get("id").getAsString();
            if (bestAny == null || Version.compare(number, bestAny) > 0) {
                bestAny = number;
                bestAnyId = id;
            }
            boolean release = !version.has("version_type")
                    || "release".equalsIgnoreCase(version.get("version_type").getAsString());
            if (release && (bestRelease == null || Version.compare(number, bestRelease) > 0)) {
                bestRelease = number;
                bestReleaseId = id;
            }
        }
        String number = bestRelease != null ? bestRelease : bestAny;
        String id = bestRelease != null ? bestReleaseId : bestAnyId;
        if (number == null) {
            return;
        }
        latestVersion = number;
        latestUrl = "https://modrinth.com/" + PROJECT_TYPE + "/" + MODRINTH_SLUG + "/version/" + id;
    }

    private void logBanner() {
        String line = "+------------------------------------------+";
        plugin.getLogger().info(line);
        plugin.getLogger().info("  ItemCooldowns update available!");
        plugin.getLogger().info("  Current: " + current() + "   Latest: " + latestVersion);
        plugin.getLogger().info("  " + latestUrl);
        plugin.getLogger().info(line);
    }

    private void notify(Player player) {
        if (!player.isOnline() || !updateAvailable()) {
            return;
        }
        LocalDate today = LocalDate.now();
        notified.values().removeIf(day -> day.isBefore(today));
        notified.put(player.getUniqueId(), today);

        Component bar = Component.text("━━━━━━━━ ItemCooldowns ━━━━━━━━", NamedTextColor.GOLD);
        Component download = Component.text("[Download on Modrinth]", NamedTextColor.GREEN, TextDecoration.BOLD)
                .clickEvent(ClickEvent.openUrl(latestUrl))
                .hoverEvent(HoverEvent.showText(Component.text("Open the download page", NamedTextColor.GRAY)));
        player.sendMessage(bar);
        player.sendMessage(Component.text("New update available!", NamedTextColor.YELLOW));
        player.sendMessage(Component.text("Current: ", NamedTextColor.GRAY)
                .append(Component.text(current(), NamedTextColor.RED)));
        player.sendMessage(Component.text("Latest: ", NamedTextColor.GRAY)
                .append(Component.text(latestVersion, NamedTextColor.GREEN)));
        player.sendMessage(download);
        player.sendMessage(bar);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.2f);
    }
}
