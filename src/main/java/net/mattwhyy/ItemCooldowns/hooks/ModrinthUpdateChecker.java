package net.mattwhyy.itemcooldowns.hooks;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import net.mattwhyy.itemcooldowns.ItemCooldownsPlugin;

public final class ModrinthUpdateChecker {
    private static final String PROJECT = "itemcooldowns";
    private static final String PAGE = "https://modrinth.com/plugin/itemcooldowns";
    private static final URI VERSIONS = URI.create("https://api.modrinth.com/v2/project/" + PROJECT + "/version");

    private ModrinthUpdateChecker() {
    }

    public static void start(ItemCooldownsPlugin plugin) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> check(plugin));
    }

    private static void check(ItemCooldownsPlugin plugin) {
        String current = plugin.getPluginMeta().getVersion();
        try {
            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8)).build();
            HttpRequest request = HttpRequest.newBuilder(VERSIONS)
                    .timeout(Duration.ofSeconds(8))
                    .header("User-Agent", "ItemCooldowns/" + current + " (" + PAGE + ")")
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                plugin.getLogger().warning("Modrinth update check failed: HTTP " + response.statusCode());
                return;
            }
            String latest = latestRelease(JsonParser.parseString(response.body()).getAsJsonArray());
            if (latest == null) {
                return;
            }
            if (compare(latest, current) > 0) {
                plugin.getLogger().info("Update available: " + latest + " (you have " + current + "). " + PAGE);
            }
        } catch (Exception exception) {
            plugin.getLogger().warning("Modrinth update check failed: " + exception.getMessage());
        }
    }

    private static String latestRelease(JsonArray versions) {
        String fallback = null;
        for (JsonElement element : versions) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject version = element.getAsJsonObject();
            if (!version.has("version_number")) {
                continue;
            }
            String number = version.get("version_number").getAsString();
            if (fallback == null) {
                fallback = number;
            }
            if (!version.has("version_type") || "release".equalsIgnoreCase(version.get("version_type").getAsString())) {
                return number;
            }
        }
        return fallback;
    }

    static int compare(String left, String right) {
        String[] a = digits(left);
        String[] b = digits(right);
        int n = Math.max(a.length, b.length);
        for (int i = 0; i < n; i++) {
            int av = i < a.length ? parse(a[i]) : 0;
            int bv = i < b.length ? parse(b[i]) : 0;
            if (av != bv) {
                return Integer.compare(av, bv);
            }
        }
        return 0;
    }

    private static String[] digits(String raw) {
        return raw.split("[^0-9]+");
    }

    private static int parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return 0;
        }
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }
}
