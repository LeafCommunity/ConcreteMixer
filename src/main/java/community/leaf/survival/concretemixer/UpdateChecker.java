/*
 * Copyright © 2022-2023, RezzedUp and Contributors <https://github.com/LeafCommunity/ConcreteMixer>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package community.leaf.survival.concretemixer;

import com.github.zafarkhaja.semver.Version;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.uwyn.urlencoder.UrlEncoder;
import community.leaf.evergreen.bukkit.versions.MinecraftVersion;
import community.leaf.survival.concretemixer.util.Strings;
import community.leaf.survival.concretemixer.util.Versions;
import community.leaf.tasks.TaskContext;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.scheduler.BukkitTask;
import pl.tlinkowski.annotation.basic.NullOr;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class UpdateChecker
{
    public static final String MODRINTH_PROJECT_ID = "ufdoTeFB";
    
    private final ConcreteMixerPlugin plugin;
    private final HttpClient client;
    private final String userAgent;
    private final URI modrinthProjectVersionsUri;
    
    private @NullOr TaskContext<BukkitTask> task;
    private volatile @NullOr Version latestAvailableVersion;
    
    public UpdateChecker(ConcreteMixerPlugin plugin)
    {
        this.plugin = plugin;
        this.client = HttpClient.newHttpClient();
        
        this.userAgent = "%s/%s (Modrinth: %s) %s/%s".formatted(
            plugin.getName(), plugin.version(), MODRINTH_PROJECT_ID,
            plugin.getServer().getName(), plugin.getServer().getVersion()
        );
        
        String fullGameVersion = MinecraftVersion.server().toString();
        String majorGameVersion = fullGameVersion.substring(0, fullGameVersion.lastIndexOf('.'));
        this.modrinthProjectVersionsUri = modrinthProjectVersions(List.of(fullGameVersion, majorGameVersion));
        
        reload();
    }
    
    public boolean isRunningUpdateCheckTask() { return task != null && !task.isCancelled(); }
    
    public Optional<Version> latestAvailableVersion() { return Optional.ofNullable(latestAvailableVersion); }
    
    public Optional<Version> latestUpdateVersion() { return latestAvailableVersion().filter(plugin.version()::lessThan); }
    
    public boolean isOutdated() { return latestUpdateVersion().isPresent(); }
    
    public void end()
    {
        if (task != null) { task.cancel(); }
    }
    
    public void reload()
    {
        if (plugin.config().getOrDefault(Config.UPDATES))
        {
            if (task == null || task.isCancelled())
            {
                int duration = ThreadLocalRandom.current().nextInt(6, 12);
                this.task = plugin.async().delay(10).ticks().every(duration).hours().run(this::checkForUpdates);
            }
        }
        else
        {
            latestAvailableVersion = null;
            end();
        }
    }
    
    private URI modrinthProjectVersions(List<String> supportedVersions)
    {
        Map<String, String> params = new LinkedHashMap<>();
        
        params.put(
            "game_versions",
            supportedVersions.stream()
                .map(ver -> "\"" + ver + "\"")
                .collect(Collectors.joining(",", "[", "]"))
        );
        
        String url = "https://api.modrinth.com/v2/project/%s/version?%s".formatted(
            MODRINTH_PROJECT_ID,
            params.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + UrlEncoder.encode(entry.getValue(), "[,]"))
                .collect(Collectors.joining("&"))
        );
        
        return URI.create(url);
    }
    
    private void checkForUpdates()
    {
        HttpRequest request =
            HttpRequest.newBuilder(modrinthProjectVersionsUri)
                .setHeader("User-Agent", userAgent)
                .build();
        
        try
        {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) { return; }
            
            JsonArray json = JsonParser.parseString(response.body()).getAsJsonArray();
            @NullOr Version latest = null;
            
            for (JsonElement element : json)
            {
                if (!element.isJsonObject()) { continue; }
                JsonObject object = element.getAsJsonObject();
                
                JsonElement versionType = object.get("version_type");
                if (!versionType.isJsonPrimitive()) { continue; }
                if (!"release".equalsIgnoreCase(versionType.getAsString())) { continue; }
                
                JsonElement versionNumber = object.get("version_number");
                if (!versionNumber.isJsonPrimitive()) { continue; }
                
                @NullOr Version version = Versions.parse(versionNumber.getAsString()).orElse(null);
                if (version == null) { continue; }
                
                if (latest == null || latest.lessThan(version))
                {
                    latest = version;
                }
            }
            
            this.latestAvailableVersion = latest;
        }
        catch (Exception ignored) {}
    
        plugin.sync().run(this::notifyIfUpdateAvailable);
    }
    
    private void print(String text)
    {
        plugin.getServer().getConsoleSender().sendMessage("[%s] %s".formatted(plugin.getName(), text));
    }
    
    public void notifyIfUpdateAvailable()
    {
        latestUpdateVersion().ifPresent(version ->
        {
            String notification = Strings.colorful(
                "&6An update is available &e@&f https://modrinth.com/plugin/%s/version/%s"
                    .formatted(MODRINTH_PROJECT_ID, version)
            );
            
            String bar = "-".repeat(ChatColor.stripColor(notification).length());
            String boundary = Strings.colorful("&6" + bar);
            
            print(boundary);
            print(notification);
            print(boundary);
        });
    }
}
