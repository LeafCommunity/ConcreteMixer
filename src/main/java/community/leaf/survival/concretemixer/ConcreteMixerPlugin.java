/*
 * Copyright © 2022, RezzedUp and Contributors <https://github.com/LeafCommunity/ConcreteMixer>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package community.leaf.survival.concretemixer;

import com.github.zafarkhaja.semver.Version;
import community.leaf.eventful.bukkit.BukkitEventSource;
import community.leaf.tasks.bukkit.BukkitTaskSource;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import pl.tlinkowski.annotation.basic.NullOr;

import java.nio.file.Path;

public class ConcreteMixerPlugin extends JavaPlugin implements BukkitEventSource, BukkitTaskSource
{
    private @NullOr Version version;
    private @NullOr Path directory;
    private @NullOr Config config;
    private @NullOr PermissionHandler permissions;
    private @NullOr EffectHandler effects;
    
    @Override
    public void onEnable()
    {
        this.version = Version.valueOf(getDescription().getVersion());
        this.directory = getDataFolder().toPath();
        this.config = new Config(this);
        this.effects = new EffectHandler(config);
        this.permissions = new PermissionHandler(this);
        
        events().register(new CauldronPowderDropListener(this));
        
        if (config.getOrDefault(Config.METRICS))
        {
            new Metrics(this, 15590);
        }
    }
    
    private static <T> T initialized(@NullOr T thing)
    {
        if (thing != null) { return thing; }
        throw new IllegalStateException("Not initialized.");
    }
    
    @Override
    public Plugin plugin() { return this; }
    
    public Version version() { return initialized(version); }
    
    public Path directory() { return initialized(directory); }
    
    public Config config() { return initialized(config); }
    
    public EffectHandler effects() { return initialized(effects); }
    
    public PermissionHandler permissions() { return initialized(permissions); }
}
